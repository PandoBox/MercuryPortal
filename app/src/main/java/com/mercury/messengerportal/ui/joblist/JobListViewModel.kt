package com.mercury.messengerportal.ui.joblist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mercury.messengerportal.data.repository.AuthRepository
import com.mercury.messengerportal.data.repository.JobRepository
import com.mercury.messengerportal.domain.model.Job
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JobListUiState(
    val jobs: List<Job> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class JobListViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val jobRepository: JobRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JobListUiState())
    val uiState: StateFlow<JobListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentMessenger.filterNotNull().collect { messenger ->
                jobRepository.observeJobs(messenger.id).collect { jobs ->
                    _uiState.update { it.copy(jobs = jobs, isLoading = false) }
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
}
