package com.mercury.messengerportal.ui.jobdetail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mercury.messengerportal.data.repository.JobRepository
import com.mercury.messengerportal.domain.model.Job
import com.mercury.messengerportal.domain.model.JobStatus
import com.mercury.messengerportal.domain.model.JobStatusLog
import com.mercury.messengerportal.util.LocationHelper
import com.mercury.messengerportal.util.PilotConfig
import com.mercury.messengerportal.util.isNetworkAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * [Pilot-config] Represents the two choices shown in the arrival location picker dialog.
 * Remove when ARRIVAL_LOCATION_PICKER_ENABLED is turned off.
 */
enum class ArrivalLocationChoice {
    CURRENT_GPS,   // Use real-time GPS (or spoofed if FAKE_LOCATION_ENABLED)
    JOB_DESTINATION // Use the job's expected lat/lng (the destination coordinates)
}

data class JobDetailUiState(
    val job: Job? = null,
    val statusLogs: List<JobStatusLog> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    /** Set when a status transition requires camera — caller should navigate to Camera screen */
    val pendingCameraLogId: String? = null,
    val pendingNextStatus: JobStatus? = null,
    /** GPS data captured before navigating to camera, so it can be used when camera returns */
    val pendingLatitude: Double? = null,
    val pendingLongitude: Double? = null,
    val pendingLocationAddress: String? = null,
    /**
     * True when we've returned from Camera and are ready to commit the pending status.
     * JobDetailScreen observes this and calls viewModel.commitAfterCamera().
     */
    val pendingCommit: Boolean = false,
    /**
     * [Pilot-config] Set when ARRIVED transition is triggered and
     * ARRIVAL_LOCATION_PICKER_ENABLED = true. UI should show picker dialog.
     */
    val showArrivalLocationPicker: Boolean = false,
    val showReassignDialog: Boolean = false,
    val reassignNote: String = "",
    val showEditNoteDialog: Boolean = false,
    val editNoteText: String = "",
    /** Set when departing to next job — caller should navigate to that job's detail screen */
    val navigateToJobId: String? = null,
    val showDelayDialog: Boolean = false,
    val delayReason: String = "",
    /** Set to true when job status just changed to COMPLETED or DELAYED in this session */
    val justStatusChanged: Boolean = false
)

@HiltViewModel
class JobDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val jobRepository: JobRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JobDetailUiState())
    val uiState: StateFlow<JobDetailUiState> = _uiState.asStateFlow()

    fun loadJob(jobId: String) {
        viewModelScope.launch {
            jobRepository.observeJob(jobId).collect { job ->
                _uiState.update { state ->
                    // Only reset justStatusChanged if loading a NEW job, not when the current job updates
                    val isNewJob = state.job?.id != jobId
                    state.copy(
                        job = job,
                        isLoading = false,
                        justStatusChanged = if (isNewJob) false else state.justStatusChanged
                    )
                }
            }
        }
        viewModelScope.launch {
            jobRepository.observeStatusLogs(jobId).collect { logs ->
                _uiState.update { it.copy(statusLogs = logs) }
            }
        }
    }

    /**
     * Handles the primary action button tap.
     *
     * [Pilot-config] When ARRIVAL_LOCATION_PICKER_ENABLED is true and the next status
     * is ARRIVED, we pause and show a location picker dialog instead of immediately
     * committing the GPS stamp. The user's choice is forwarded to [onArrivalLocationChosen].
     *
     * Normal flow:
     * - If next status requires photo: navigate to Camera.
     * - Otherwise: auto-stamp GPS + update status directly.
     */
    fun onPrimaryAction() {
        val job = _uiState.value.job ?: return
        val nextStatus = job.status.nextAction() ?: return

        // [Pilot-config] Intercept transition and show picker dialog for location.
        if (PilotConfig.ARRIVAL_LOCATION_PICKER_ENABLED && (nextStatus == JobStatus.COMPLETED || nextStatus == JobStatus.ARRIVED)) {
            _uiState.update { it.copy(showArrivalLocationPicker = true) }
            return
        }

        commitStatusUpdate(job, nextStatus)
    }

    /**
     * [Pilot-config] Called when the user picks a location in the arrival picker dialog.
     * CURRENT_GPS  → standard flow (uses spoofed location if FAKE_LOCATION_ENABLED).
     * JOB_DESTINATION → stamps the job's own lat/lng as the arrival location.
     */
    fun onArrivalLocationChosen(choice: ArrivalLocationChoice) {
        _uiState.update { it.copy(showArrivalLocationPicker = false) }
        val job = _uiState.value.job ?: return
        val nextStatus = job.status.nextAction() ?: return

        when (choice) {
            ArrivalLocationChoice.CURRENT_GPS -> commitStatusUpdate(job, nextStatus)
            ArrivalLocationChoice.JOB_DESTINATION -> {
                // Stamp with the job's own destination coordinates instead of live GPS.
                viewModelScope.launch {
                    try {
                        val address = runCatching {
                            LocationHelper.reverseGeocode(context, job.latitude, job.longitude)
                        }.getOrNull()
                        val isOnline = context.isNetworkAvailable()
                        val justChanged = nextStatus == JobStatus.COMPLETED || nextStatus == JobStatus.DELAYED
                        _uiState.update { it.copy(isLoading = false, justStatusChanged = justChanged) }
                        jobRepository.updateJobStatus(
                            job = job,
                            newStatus = nextStatus,
                            latitude = job.latitude,
                            longitude = job.longitude,
                            locationAddress = address,
                            photoUrl = null,
                            isNetworkAvailable = isOnline
                        )
                    } catch (e: Exception) {
                        _uiState.update { it.copy(isLoading = false, error = e.message) }
                    }
                }
            }
        }
    }

    /**
     * [Pilot Test] Bypasses the camera requirement for a specific status transition.
     * Stamps current GPS and proceeds directly.
     */
    fun onBypassCameraForStatus(job: Job, nextStatus: JobStatus) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // For pilot bypass, use zero coordinates if GPS fails
                val location = try {
                    LocationHelper.getCurrentLocation(context)
                } catch (e: Exception) {
                    null
                }
                
                val lat = location?.latitude ?: 0.0
                val lng = location?.longitude ?: 0.0
                val address = location?.let {
                    runCatching { LocationHelper.reverseGeocode(context, it.latitude, it.longitude) }.getOrNull()
                }

                val isOnline = context.isNetworkAvailable()
                val justChanged = nextStatus == JobStatus.COMPLETED || nextStatus == JobStatus.DELAYED
                _uiState.update { it.copy(isLoading = false, justStatusChanged = justChanged) }
                jobRepository.updateJobStatus(
                    job = job,
                    newStatus = nextStatus,
                    latitude = lat,
                    longitude = lng,
                    locationAddress = address,
                    photoUrl = "[Pilot Test] Bypassed Camera",
                    isNetworkAvailable = isOnline
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onDismissArrivalPicker() {
        _uiState.update { it.copy(showArrivalLocationPicker = false) }
    }

    private fun commitStatusUpdate(job: Job, nextStatus: JobStatus) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val location = LocationHelper.getCurrentLocation(context)

                // Resolve a human-readable Google address alongside the raw coordinates.
                // Non-fatal: a null result still allows the status update to proceed.
                val address = runCatching {
                    LocationHelper.reverseGeocode(context, location.latitude, location.longitude)
                }.getOrNull()

                if (nextStatus.requiresPhoto()) {
                    // Signal UI to open Camera — photo is required before status is committed
                    val logId = UUID.randomUUID().toString()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pendingCameraLogId = logId,
                            pendingNextStatus = nextStatus,
                            pendingLatitude = location.latitude,
                            pendingLongitude = location.longitude,
                            pendingLocationAddress = address
                        )
                    }
                } else {
                    // No photo needed — update status directly
                    val isOnline = context.isNetworkAvailable()
                    val justChanged = nextStatus == JobStatus.COMPLETED || nextStatus == JobStatus.DELAYED
                    _uiState.update { it.copy(isLoading = false, justStatusChanged = justChanged) }
                    jobRepository.updateJobStatus(
                        job = job,
                        newStatus = nextStatus,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        locationAddress = address,
                        photoUrl = null,
                        isNetworkAvailable = isOnline
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /**
     * Called when CameraScreen returns (photo captured or skipped).
     * Commits the pending status update using the lat/lng/address captured before navigation.
     * [photoUrl] is null when the skip button was used.
     */
    fun commitAfterCamera(photoUrl: String?) {
        val job = _uiState.value.job ?: return
        val nextStatus = _uiState.value.pendingNextStatus ?: return
        val lat = _uiState.value.pendingLatitude ?: return
        val lng = _uiState.value.pendingLongitude ?: return
        val address = _uiState.value.pendingLocationAddress

        // Clear pending state immediately to avoid re-trigger
        _uiState.update {
            it.copy(
                pendingCameraLogId = null,
                pendingNextStatus = null,
                pendingLatitude = null,
                pendingLongitude = null,
                pendingLocationAddress = null,
                pendingCommit = false,
                isLoading = true
            )
        }

        viewModelScope.launch {
            try {
                val isOnline = context.isNetworkAvailable()
                val justChanged = nextStatus == JobStatus.COMPLETED || nextStatus == JobStatus.DELAYED
                _uiState.update { it.copy(isLoading = false, justStatusChanged = justChanged) }
                jobRepository.updateJobStatus(
                    job = job,
                    newStatus = nextStatus,
                    latitude = lat,
                    longitude = lng,
                    locationAddress = address,
                    photoUrl = photoUrl,
                    isNetworkAvailable = isOnline
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onCameraNavigated() {
        _uiState.update { it.copy(pendingCameraLogId = null) }
    }

    fun onShowReassignDialog() {
        _uiState.update { it.copy(showReassignDialog = true) }
    }

    fun onReassignNoteChange(note: String) {
        _uiState.update { it.copy(reassignNote = note) }
    }

    fun onConfirmReassign() {
        val job = _uiState.value.job ?: return
        val note = _uiState.value.reassignNote
        viewModelScope.launch {
            jobRepository.requestReassign(job.id, note)
            _uiState.update { it.copy(showReassignDialog = false, reassignNote = "") }
        }
    }

    fun onDismissReassign() {
        _uiState.update { it.copy(showReassignDialog = false, reassignNote = "") }
    }

    fun onShowEditNote() {
        _uiState.update { it.copy(showEditNoteDialog = true, editNoteText = it.job?.messengerRemark ?: "") }
    }

    fun onEditNoteTextChange(text: String) {
        _uiState.update { it.copy(editNoteText = text) }
    }

    fun onConfirmEditNote() {
        val job = _uiState.value.job ?: return
        val text = _uiState.value.editNoteText
        viewModelScope.launch {
            jobRepository.updateMessengerRemark(job.id, text)
            _uiState.update { it.copy(showEditNoteDialog = false) }
        }
    }

    fun onDismissEditNote() {
        _uiState.update { it.copy(showEditNoteDialog = false) }
    }

    fun onDepartNextJob() {
        val job = _uiState.value.job ?: return
        if (job.status != JobStatus.COMPLETED && job.status != JobStatus.DELAYED) return
        if (!_uiState.value.justStatusChanged) return

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                val location = LocationHelper.getCurrentLocation(context)
                val address = runCatching {
                    LocationHelper.reverseGeocode(context, location.latitude, location.longitude)
                }.getOrNull()
                val isOnline = context.isNetworkAvailable()

                // Record DEPART_HQ if not already recorded today
                jobRepository.insertHqEvent("HQ_EVENT", "DEPART_HQ", location.latitude, location.longitude, address)

                jobRepository.observeJobs(job.messengerId).first().let { jobs ->
                    val nextJob = jobs.find { it.sequenceOrder == job.sequenceOrder + 1 && it.status == JobStatus.ASSIGNED }
                    if (nextJob != null) {
                        jobRepository.updateJobStatus(
                            job = nextJob,
                            newStatus = JobStatus.DEPARTED,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            locationAddress = address,
                            photoUrl = null,
                            isNetworkAvailable = isOnline
                        )
                        _uiState.update { it.copy(isLoading = false, navigateToJobId = nextJob.id) }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "No next job available") }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onNavigatedToNextJob() {
        _uiState.update { it.copy(navigateToJobId = null) }
    }

    private suspend fun checkAndStartNextJob(completedJob: Job, lat: Double, lng: Double, address: String?, isOnline: Boolean) {
        if (completedJob.status != JobStatus.COMPLETED) return

        try {
            // Record DEPART_HQ if not already recorded today
            jobRepository.insertHqEvent("HQ_EVENT", "DEPART_HQ", lat, lng, address)

            jobRepository.observeJobs(completedJob.messengerId).first().let { jobs ->
                val nextJob = jobs.find { it.sequenceOrder == completedJob.sequenceOrder + 1 && it.status == JobStatus.ASSIGNED }
                if (nextJob != null) {
                    jobRepository.updateJobStatus(
                        job = nextJob,
                        newStatus = JobStatus.DEPARTED,
                        latitude = lat,
                        longitude = lng,
                        locationAddress = address,
                        photoUrl = null,
                        isNetworkAvailable = isOnline
                    )
                }
            }
        } catch (e: Exception) {
            // Ignore auto-start failures
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onShowDelayDialog() {
        _uiState.update { it.copy(showDelayDialog = true, delayReason = "") }
    }

    fun onDelayReasonChange(reason: String) {
        _uiState.update { it.copy(delayReason = reason) }
    }

    fun onConfirmDelay() {
        val job = _uiState.value.job ?: return
        val reason = _uiState.value.delayReason.trim()
        if (reason.isBlank()) return

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = false, showDelayDialog = false, delayReason = "", justStatusChanged = true) }
                // Update job status to DELAYED
                jobRepository.updateJobStatus(
                    job = job,
                    newStatus = JobStatus.DELAYED,
                    latitude = null,
                    longitude = null,
                    locationAddress = null,
                    photoUrl = null,
                    isNetworkAvailable = true
                )
                // Add delay reason to messenger remark
                jobRepository.updateMessengerRemark(job.id, "Delay Reason: $reason")
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onDismissDelayDialog() {
        _uiState.update { it.copy(showDelayDialog = false, delayReason = "") }
    }
}
