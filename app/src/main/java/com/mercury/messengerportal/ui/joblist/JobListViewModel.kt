package com.mercury.messengerportal.ui.joblist

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mercury.messengerportal.data.repository.AuthRepository
import com.mercury.messengerportal.data.repository.JobRepository
import com.mercury.messengerportal.domain.model.DeliverySession
import com.mercury.messengerportal.domain.model.Job
import com.mercury.messengerportal.domain.model.JobStatus
import com.mercury.messengerportal.util.LocationHelper
import com.mercury.messengerportal.util.RouteOptimizer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JobListUiState(
    val jobs: List<Job> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showRouteDialog: Boolean = false,
    val suggestedOrder: List<Job> = emptyList(),
    val searchQuery: String = "",
    val statusFilter: JobStatus? = null,
    val sessionFilter: DeliverySession? = null
)

@HiltViewModel
class JobListViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val jobRepository: JobRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(JobListUiState())
    val uiState: StateFlow<JobListUiState> = _uiState.asStateFlow()

    // Private flows for filter state
    private val _searchQuery = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow<JobStatus?>(null)
    private val _sessionFilter = MutableStateFlow<DeliverySession?>(null)
    private val _allJobs = MutableStateFlow<List<Job>>(emptyList())

    init {
        viewModelScope.launch {
            authRepository.currentMessenger.filterNotNull().collect { messenger ->
                // Combine all job streams with filters
                combine(
                    jobRepository.observeJobs(messenger.id),
                    _searchQuery,
                    _statusFilter,
                    _sessionFilter
                ) { jobs, query, statusFilter, sessionFilter ->
                    // Apply filters
                    jobs.filter { job ->
                        // Search filter
                        val matchesSearch = if (query.isBlank()) {
                            true
                        } else {
                            val lowerQuery = query.lowercase()
                            job.title.lowercase().contains(lowerQuery) ||
                            job.locationName.lowercase().contains(lowerQuery) ||
                            job.locationAddress.lowercase().contains(lowerQuery) ||
                            job.receiverName.lowercase().contains(lowerQuery) ||
                            job.refNo.lowercase().contains(lowerQuery)
                        }

                        // Status filter
                        val matchesStatus = statusFilter == null || job.status == statusFilter

                        // Session filter
                        val matchesSession = sessionFilter == null || job.deliverySession == sessionFilter

                        matchesSearch && matchesStatus && matchesSession
                    }
                }.collect { filteredJobs ->
                    _uiState.update { it.copy(jobs = filteredJobs, isLoading = false) }
                }
            }
        }
    }

    /**
     * Moves a job up or down in the sequence.
     * Swaps sequenceOrder with adjacent job.
     */
    fun moveJobUp(job: Job) {
        val jobs = _uiState.value.jobs.sortedBy { it.sequenceOrder }
        val index = jobs.indexOfFirst { it.id == job.id }
        if (index <= 0) return
        val swapTarget = jobs[index - 1]
        swapOrder(job, swapTarget)
    }

    fun moveJobDown(job: Job) {
        val jobs = _uiState.value.jobs.sortedBy { it.sequenceOrder }
        val index = jobs.indexOfFirst { it.id == job.id }
        if (index < 0 || index >= jobs.size - 1) return
        val swapTarget = jobs[index + 1]
        swapOrder(job, swapTarget)
    }

    private fun swapOrder(a: Job, b: Job) {
        viewModelScope.launch {
            jobRepository.reorderJob(a.id, b.sequenceOrder)
            jobRepository.reorderJob(b.id, a.sequenceOrder)
        }
    }

    fun moveJob(fromIndex: Int, toIndex: Int) {
        val currentJobs = _uiState.value.jobs.toMutableList()
        if (fromIndex !in currentJobs.indices || toIndex !in currentJobs.indices) return

        val item = currentJobs.removeAt(fromIndex)
        currentJobs.add(toIndex, item)

        // Update local state immediately for smooth UI
        _uiState.update { it.copy(jobs = currentJobs) }

        // Update sequence orders in repository
        viewModelScope.launch {
            currentJobs.forEachIndexed { index, job ->
                val newOrder = index + 1
                // Check against current list's sequenceOrder
                if (job.sequenceOrder != newOrder) {
                    jobRepository.reorderJob(job.id, newOrder)
                }
            }
        }
    }

    /**
     * Suggests an optimal job delivery route using the current device location.
     * Computes nearest-neighbor order and shows a confirmation dialog.
     */
    fun suggestOptimalRoute() {
        viewModelScope.launch {
            try {
                val location = LocationHelper.getCurrentLocation(context)
                val activeJobs = _uiState.value.jobs
                val suggestedOrder = RouteOptimizer.suggestOrder(
                    location.latitude,
                    location.longitude,
                    activeJobs
                )
                _uiState.update { it.copy(showRouteDialog = true, suggestedOrder = suggestedOrder) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to get location: ${e.message}") }
            }
        }
    }

    /**
     * Applies the suggested route order to all jobs.
     * Updates sequenceOrder for each job based on the suggested order.
     */
    fun confirmRouteOptimization() {
        viewModelScope.launch {
            val suggestedOrder = _uiState.value.suggestedOrder
            suggestedOrder.forEachIndexed { index, job ->
                val newOrder = index + 1
                if (job.sequenceOrder != newOrder) {
                    jobRepository.reorderJob(job.id, newOrder)
                }
            }
            dismissRouteDialog()
        }
    }

    /**
     * Closes the route optimization dialog without applying changes.
     */
    fun dismissRouteDialog() {
        _uiState.update { it.copy(showRouteDialog = false, suggestedOrder = emptyList()) }
    }

    /**
     * Updates the search query and filters jobs accordingly.
     */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    /**
     * Updates the status filter.
     * Pass null to show all statuses.
     */
    fun onStatusFilterSelected(status: JobStatus?) {
        _statusFilter.value = status
        _uiState.update { it.copy(statusFilter = status) }
    }

    /**
     * Updates the delivery session filter.
     * Pass null to show all sessions.
     */
    fun onSessionFilterSelected(session: DeliverySession?) {
        _sessionFilter.value = session
        _uiState.update { it.copy(sessionFilter = session) }
    }

    /**
     * Clears all filters and search.
     */
    fun clearFilters() {
        _searchQuery.value = ""
        _statusFilter.value = null
        _sessionFilter.value = null
        _uiState.update { it.copy(searchQuery = "", statusFilter = null, sessionFilter = null) }
    }
}
