package com.mercury.messengerportal.ui.dayclosing

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mercury.messengerportal.data.repository.AuthRepository
import com.mercury.messengerportal.data.repository.JobRepository
import com.mercury.messengerportal.domain.model.DayLog
import com.mercury.messengerportal.domain.model.GpsEvent
import com.mercury.messengerportal.util.LocationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DayClosingUiState(
    val dayLog: DayLog? = null,
    val jobs: List<com.mercury.messengerportal.domain.model.Job> = emptyList(),
    val gpsTrail: List<GpsEvent> = emptyList(),
    val isLoading: Boolean = true,
    val isClosing: Boolean = false,
    val isClosed: Boolean = false,
    val error: String? = null,
    /** Pending jobs waiting for reason input */
    val pendingJobsNeedingReason: List<com.mercury.messengerportal.domain.model.Job> = emptyList(),
    /** Current job being asked for reason */
    val currentJobForReason: com.mercury.messengerportal.domain.model.Job? = null,
    /** Reasons collected for pending jobs */
    val jobReasons: Map<String, String> = emptyMap(),
    /** [Pilot-Test] Show location picker dialog for clock out */
    val showClockOutLocationPicker: Boolean = false,
    /** [Pilot-Test] Selected location for clock out (lat, lng, address) */
    val selectedClockOutLocation: Triple<Double, Double, String>? = null
)

@HiltViewModel
class DayClosingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val jobRepository: JobRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DayClosingUiState())
    val uiState: StateFlow<DayClosingUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            authRepository.currentMessenger.filterNotNull().collect { messenger ->
                // Observe today's log
                launch {
                    jobRepository.observeTodayLog(messenger.id).collect { log ->
                        _uiState.update { it.copy(dayLog = log, isLoading = false) }
                    }
                }
                // Observe jobs
                launch {
                    jobRepository.observeJobs(messenger.id).collect { jobs ->
                        _uiState.update { it.copy(jobs = jobs) }
                    }
                }
                // Build GPS trail (one-shot)
                launch {
                    val trail = jobRepository.buildGpsTrail(messenger.id)
                    _uiState.update { it.copy(gpsTrail = trail) }
                }
            }
        }
    }

    fun closeDay(reason: String? = null) {
        android.util.Log.d("DayClosingVM", "closeDay called")
        viewModelScope.launch {
            val messenger = authRepository.currentMessenger.firstOrNull() ?: return@launch

            // Find all pending jobs that need reasons
            val pendingJobs = _uiState.value.jobs.filter { it.status != com.mercury.messengerportal.domain.model.JobStatus.COMPLETED && it.status != com.mercury.messengerportal.domain.model.JobStatus.DELAYED }

            android.util.Log.d("DayClosingVM", "closeDay: found ${pendingJobs.size} pending jobs")
            if (pendingJobs.isNotEmpty()) {
                // Start collecting reasons one by one
                android.util.Log.d("DayClosingVM", "closeDay: showing reason dialog")
                _uiState.update { it.copy(pendingJobsNeedingReason = pendingJobs, currentJobForReason = pendingJobs.first()) }
            } else {
                // No pending jobs, show location picker for clock out [Pilot-Test]
                android.util.Log.d("DayClosingVM", "closeDay: showing location picker")
                _uiState.update { it.copy(isClosing = true, showClockOutLocationPicker = true) }
            }
        }
    }

    /** [Pilot-Test] User selected a location for clock out */
    fun selectClockOutLocation(lat: Double, lng: Double, address: String) {
        android.util.Log.d("DayClosingVM", "selectClockOutLocation called with lat=$lat, lng=$lng, address=$address")
        viewModelScope.launch {
            val messenger = authRepository.currentMessenger.firstOrNull() ?: return@launch
            android.util.Log.d("DayClosingVM", "Got messenger: ${messenger.name}")
            _uiState.update { it.copy(showClockOutLocationPicker = false, selectedClockOutLocation = Triple(lat, lng, address)) }
            android.util.Log.d("DayClosingVM", "Updated state to hide picker and set location")
            performDayClose(messenger)
            android.util.Log.d("DayClosingVM", "performDayClose completed")
        }
    }

    /** [Pilot-Test] Cancel location picker */
    fun cancelLocationPicker() {
        _uiState.update { it.copy(isClosing = false, showClockOutLocationPicker = false) }
    }

    fun addReasonForJob(jobId: String, reason: String) {
        val updatedReasons = _uiState.value.jobReasons.toMutableMap()
        updatedReasons[jobId] = reason

        val pendingJobs = _uiState.value.pendingJobsNeedingReason
        val currentIndex = pendingJobs.indexOfFirst { it.id == jobId }
        val currentJob = pendingJobs.getOrNull(currentIndex)

        viewModelScope.launch {
            try {
                // Update job status to DELAYED immediately
                currentJob?.let { job ->
                    jobRepository.updateJobStatus(
                        job = job,
                        newStatus = com.mercury.messengerportal.domain.model.JobStatus.DELAYED,
                        latitude = null,
                        longitude = null,
                        locationAddress = null,
                        photoUrl = null,
                        isNetworkAvailable = true
                    )
                    // Add remark
                    if (reason.isNotBlank()) {
                        jobRepository.updateMessengerRemark(job.id, "Pending Reason: $reason\n${job.messengerRemark}")
                    }
                }

                if (currentIndex >= pendingJobs.size - 1) {
                    // All jobs have reasons, close dialog and return to Day Summary
                    android.util.Log.d("DayClosingVM", "All pending jobs processed, closing reason dialog")
                    _uiState.update { it.copy(pendingJobsNeedingReason = emptyList(), currentJobForReason = null, jobReasons = emptyMap()) }
                } else {
                    // Move to next job
                    val nextJob = pendingJobs[currentIndex + 1]
                    _uiState.update { it.copy(jobReasons = updatedReasons, currentJobForReason = nextJob) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, pendingJobsNeedingReason = emptyList(), currentJobForReason = null) }
            }
        }
    }

    fun skipReasonForJob(jobId: String) {
        addReasonForJob(jobId, "")
    }

    private suspend fun performDayCloseWithReasons(jobReasons: Map<String, String>) {
        val messenger = authRepository.currentMessenger.firstOrNull() ?: return
        _uiState.update { it.copy(isClosing = true, error = null) }
        try {
            val pendingJobs = _uiState.value.jobs.filter { it.status != com.mercury.messengerportal.domain.model.JobStatus.COMPLETED && it.status != com.mercury.messengerportal.domain.model.JobStatus.DELAYED }
            for (job in pendingJobs) {
                val reason = jobReasons[job.id] ?: ""
                jobRepository.updateJobStatus(
                    job = job,
                    newStatus = com.mercury.messengerportal.domain.model.JobStatus.DELAYED,
                    latitude = null,
                    longitude = null,
                    locationAddress = null,
                    photoUrl = null,
                    isNetworkAvailable = true
                )
                if (reason.isNotBlank()) {
                    jobRepository.updateMessengerRemark(job.id, "Pending Reason: $reason\n${job.messengerRemark}")
                }
            }

            performDayCloseInternal(messenger)
        } catch (e: Exception) {
            _uiState.update { it.copy(isClosing = false, error = e.message, pendingJobsNeedingReason = emptyList(), currentJobForReason = null) }
        }
    }

    private suspend fun performDayClose(messenger: com.mercury.messengerportal.domain.model.Messenger) {
        _uiState.update { it.copy(isClosing = true, error = null) }
        try {
            performDayCloseInternal(messenger)
        } catch (e: Exception) {
            _uiState.update { it.copy(isClosing = false, error = e.message, pendingJobsNeedingReason = emptyList(), currentJobForReason = null) }
        }
    }

    /** [Pilot-Test] Uses location picker. In production, fetch live GPS location. */
    private suspend fun performDayCloseInternal(messenger: com.mercury.messengerportal.domain.model.Messenger) {
        try {
            // [Pilot-Test] Use selected location from picker, fallback to current location
            val (lat, lng, address) = _uiState.value.selectedClockOutLocation
                ?: run {
                    val location = LocationHelper.getCurrentLocation(context)
                    val addr = runCatching {
                        LocationHelper.reverseGeocode(context, location.latitude, location.longitude)
                    }.getOrNull()
                    Triple(location.latitude, location.longitude, addr)
                }

            android.util.Log.d("DayClosingVM", "Starting clock out with lat=$lat, lng=$lng, address=$address")
            jobRepository.clockOut(messenger.id, lat, lng)
            android.util.Log.d("DayClosingVM", "Clock out completed")
            jobRepository.insertHqEvent("HQ_EVENT", "CLOCK_OUT", lat, lng, address)
            android.util.Log.d("DayClosingVM", "HQ_EVENT inserted, setting isClosed=true")
            _uiState.update { it.copy(isClosing = false, isClosed = true, pendingJobsNeedingReason = emptyList(), currentJobForReason = null, selectedClockOutLocation = null) }
            android.util.Log.d("DayClosingVM", "Clock out successful, state updated")
        } catch (e: Exception) {
            android.util.Log.e("DayClosingVM", "Clock out failed: ${e.message}", e)
            _uiState.update { it.copy(isClosing = false, error = "Clock out failed: ${e.message}", pendingJobsNeedingReason = emptyList(), currentJobForReason = null) }
        }
    }

    fun cancelClosing() {
        _uiState.update { it.copy(pendingJobsNeedingReason = emptyList(), currentJobForReason = null, jobReasons = emptyMap()) }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
