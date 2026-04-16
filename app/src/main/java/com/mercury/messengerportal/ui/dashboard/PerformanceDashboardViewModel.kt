package com.mercury.messengerportal.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mercury.messengerportal.data.repository.AuthRepository
import com.mercury.messengerportal.data.repository.JobRepository
import com.mercury.messengerportal.domain.model.DayLog
import com.mercury.messengerportal.domain.model.Job
import com.mercury.messengerportal.domain.model.JobStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class DashboardUiState(
    val messengerId: String = "",
    val messengerName: String = "",
    val todayLog: DayLog? = null,
    val todayJobs: List<Job> = emptyList(),
    val recentLogs: List<DayLog> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class TodayStats(
    val completedCount: Int = 0,
    val delayedCount: Int = 0,
    val completionRate: Float = 0f,
    val shiftDurationMinutes: Long = 0L,
    val shiftDurationFormatted: String = ""
)

@HiltViewModel
class PerformanceDashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val jobRepository: JobRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _todayStats = MutableStateFlow(TodayStats())
    val todayStats: StateFlow<TodayStats> = _todayStats.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        viewModelScope.launch {
            authRepository.currentMessenger.filterNotNull().collect { messenger ->
                _uiState.update {
                    it.copy(
                        messengerId = messenger.id,
                        messengerName = messenger.name
                    )
                }
                combine(
                    jobRepository.observeTodayLog(messenger.id),
                    jobRepository.observeJobs(messenger.id),
                    jobRepository.observeRecentDayLogs(messenger.id, limit = 7)
                ) { todayLog, todayJobs, recentLogs ->
                    // Always use mock data for development. In production with real data,
                    // this will be replaced by recentLogs from the database
                    val pastLogsWithMock = generateMockPastDayLogs(messenger.id)
                    Triple(todayLog, todayJobs, pastLogsWithMock)
                }.collect { (todayLog, todayJobs, recentLogs) ->
                    _uiState.update {
                        it.copy(
                            todayLog = todayLog,
                            todayJobs = todayJobs,
                            recentLogs = recentLogs,
                            isLoading = false
                        )
                    }

                    // Compute today's stats
                    val completedCount = todayJobs.count { it.status == JobStatus.COMPLETED }
                    val delayedCount = todayJobs.count { it.status == JobStatus.DELAYED }
                    val totalCount = todayJobs.size
                    val completionRate = if (totalCount > 0) {
                        (completedCount.toFloat() / totalCount.toFloat()) * 100f
                    } else {
                        0f
                    }

                    val shiftDurationMinutes = if (todayLog != null && todayLog.clockOutTime != null) {
                        (todayLog.clockOutTime!! - todayLog.clockInTime) / (1000 * 60)
                    } else {
                        0L
                    }

                    val shiftDurationFormatted = if (todayLog != null && todayLog.clockOutTime == null) {
                        "In Progress" // Shift is still active
                    } else {
                        formatDuration(shiftDurationMinutes)
                    }

                    _todayStats.update {
                        it.copy(
                            completedCount = completedCount,
                            delayedCount = delayedCount,
                            completionRate = completionRate,
                            shiftDurationMinutes = shiftDurationMinutes,
                            shiftDurationFormatted = shiftDurationFormatted
                        )
                    }
                }
            }
        }
    }

    /**
     * Formats a duration in minutes to a human-readable string.
     * E.g. 125 minutes → "2h 5m"
     */
    private fun formatDuration(minutes: Long): String {
        if (minutes <= 0) return "0m"
        val hours = minutes / 60
        val mins = minutes % 60
        return when {
            hours > 0 && mins > 0 -> "${hours}h ${mins}m"
            hours > 0 -> "${hours}h"
            else -> "${mins}m"
        }
    }

    /**
     * Formats a date from epoch milliseconds to a readable string (e.g., "Mon Apr 14").
     */
    fun formatDate(epochMs: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = epochMs
        val sdf = SimpleDateFormat("EEE MMM dd", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    /**
     * Formats duration for history rows.
     */
    fun formatHistoryDuration(dayLog: DayLog): String {
        return if (dayLog.clockOutTime != null) {
            val minutes = (dayLog.clockOutTime!! - dayLog.clockInTime) / (1000 * 60)
            formatDuration(minutes)
        } else {
            "In Progress"
        }
    }

    /**
     * Generates mock data for past 7 days: 5 green, 1 yellow, 1 red
     * This is temporary dummy data. In production, this will come from the app's database.
     */
    private fun generateMockPastDayLogs(messengerId: String): List<DayLog> {
        val mockLogs = mutableListOf<DayLog>()
        val calendar = Calendar.getInstance()

        // Job counts for each day (5 green, 1 yellow, 1 red)
        // 5 green days (8-10 jobs = 80-100% completion)
        // 1 yellow day (6 jobs = ~60% completion)
        // 1 red day (2 jobs = ~20% completion)
        val completionCounts = listOf(
            10, 9, 8, 9, 10,  // 5 green days
            6,                 // 1 yellow day
            2                  // 1 red day
        )

        // Generate 7 days starting from yesterday going backwards
        for (daysAgo in 1..7) {
            val dayCalendar = Calendar.getInstance()
            dayCalendar.add(Calendar.DAY_OF_MONTH, -daysAgo)

            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dayCalendar.time)
            val dayIndex = daysAgo - 1  // Index for completion counts (0-6)

            mockLogs.add(
                DayLog(
                    id = "mock_day_$daysAgo",
                    date = dateStr,
                    messengerId = messengerId,
                    clockInTime = dayCalendar.timeInMillis - (8 * 60 * 60 * 1000),  // 8 hours duration
                    clockInLat = 13.7263,
                    clockInLng = 100.5221,
                    clockInAddress = "Bangkok, Thailand",
                    clockOutTime = dayCalendar.timeInMillis,
                    clockOutLat = 13.7263,
                    clockOutLng = 100.5221,
                    clockOutAddress = "Bangkok, Thailand",
                    isClosed = true,
                    totalJobsCompleted = completionCounts[dayIndex]
                )
            )
        }

        // Return sorted by date descending (most recent first)
        return mockLogs.sortedByDescending { it.date }
    }
}
