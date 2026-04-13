package com.mercury.messengerportal.ui.home

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mercury.messengerportal.data.repository.AuthRepository
import com.mercury.messengerportal.data.repository.JobRepository
import com.mercury.messengerportal.domain.model.DayLog
import com.mercury.messengerportal.domain.model.Job
import com.mercury.messengerportal.domain.model.Messenger
import com.mercury.messengerportal.util.LocationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val messenger: Messenger? = null,
    val todayLog: DayLog? = null,
    val jobs: List<Job> = emptyList(),
    val clockInLocation: Location? = null,
    val isAtHq: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isClockedIn: Boolean get() = todayLog != null && !todayLog.isClosed
    val completedCount: Int get() = jobs.count { it.status.name == "COMPLETED" }
    val pendingCount: Int get() = jobs.count { it.status.name == "ASSIGNED" }
    val inProgressCount: Int get() = jobs.count {
        it.status.name in listOf("DEPARTED", "ARRIVED")
    }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val jobRepository: JobRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            authRepository.currentMessenger.collect { messenger ->
                if (messenger == null) return@collect
                _uiState.update { it.copy(messenger = messenger) }

                // Initialize HQ status and seed dummy jobs
                val prefs = context.getSharedPreferences("mercury_prefs", Context.MODE_PRIVATE)
                _uiState.update { it.copy(isAtHq = prefs.getBoolean("isAtHq", true)) }
                jobRepository.seedDummyJobsIfEmpty(messenger.id)

                // Observe jobs
                launch {
                    jobRepository.observeJobs(messenger.id).collect { jobs ->
                        _uiState.update { it.copy(jobs = jobs, isLoading = false) }
                    }
                }

                // Observe today's day log (clock-in status)
                launch {
                    jobRepository.observeTodayLog(messenger.id).collect { log ->
                        _uiState.update { it.copy(todayLog = log) }
                    }
                }
            }
        }
    }

    fun clockIn() {
        val messenger = _uiState.value.messenger ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val location = LocationHelper.getCurrentLocation(context)
                jobRepository.clockIn(messenger.id, location.latitude, location.longitude)
                _uiState.update { it.copy(isLoading = false, clockInLocation = location) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun departFirstJob(onNavigateToJob: (String) -> Unit) {
        val firstPendingJob = _uiState.value.jobs.firstOrNull { it.status.name != "COMPLETED" }
        if (firstPendingJob == null) {
            _uiState.update { it.copy(error = "No pending jobs to depart for.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val loc = LocationHelper.getCurrentLocation(context)
                val address = runCatching {
                    LocationHelper.reverseGeocode(context, loc.latitude, loc.longitude)
                }.getOrNull()

                jobRepository.insertHqEvent("HQ_EVENT", "DEPART_HQ", loc.latitude, loc.longitude, address)

                // Only transition if status is ASSIGNED
                if (firstPendingJob.status.name == "ASSIGNED") {
                    jobRepository.updateJobStatus(
                        job = firstPendingJob,
                        newStatus = com.mercury.messengerportal.domain.model.JobStatus.DEPARTED,
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        locationAddress = address,
                        photoUrl = null,
                        isNetworkAvailable = true
                    )
                }
                context.getSharedPreferences("mercury_prefs", Context.MODE_PRIVATE)
                       .edit().putBoolean("isAtHq", false).apply()
                _uiState.update { it.copy(isLoading = false, isAtHq = false) }
                onNavigateToJob(firstPendingJob.id)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun arriveHq() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Fetch location just to log it
                val loc = LocationHelper.getCurrentLocation(context)
                val address = runCatching {
                    LocationHelper.reverseGeocode(context, loc.latitude, loc.longitude)
                }.getOrNull()
                
                jobRepository.insertHqEvent("HQ_EVENT", "ARRIVE_HQ", loc.latitude, loc.longitude, address)
                
                context.getSharedPreferences("mercury_prefs", Context.MODE_PRIVATE)
                       .edit().putBoolean("isAtHq", true).apply()
                _uiState.update { it.copy(isLoading = false, isAtHq = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Ensure GPS is active to arrive HQ") }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    /** [Pilot/Dev] Reset all progress for today */
    fun resetTodayProgress() {
        val messenger = _uiState.value.messenger ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                jobRepository.resetTodayProgress(messenger.id)
                context.getSharedPreferences("mercury_prefs", Context.MODE_PRIVATE)
                       .edit().putBoolean("isAtHq", true).apply()
                _uiState.update { it.copy(isAtHq = true) }
                // loadData() will observe changes automatically
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Reset failed: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
