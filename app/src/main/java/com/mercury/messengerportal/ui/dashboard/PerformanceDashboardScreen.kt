package com.mercury.messengerportal.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mercury.messengerportal.domain.model.DayLog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceDashboardScreen(
    onBack: () -> Unit,
    viewModel: PerformanceDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val todayStats by viewModel.todayStats.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "${state.messengerId} - ${state.messengerName}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF8A95A6)
                        )
                        Text(
                            "TODAY",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1F2E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF1A1F2E)
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF1A1F2E)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // TODAY'S PERFORMANCE CARD (Whoop style)
            item {
                TodayPerformanceCard(todayStats = todayStats, dayLog = state.todayLog)
            }

            // PAST 7 DAYS SECTION
            // Note: Currently showing dummy data (5 green, 1 yellow, 1 red days)
            // In production, this will be populated from the app's local database
            if (state.recentLogs.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Past 7 Days",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            state.recentLogs.forEach { dayLog ->
                                PastDayCard(dayLog = dayLog, viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Today's performance card - Whoop style with large circular progress
 */
@Composable
private fun TodayPerformanceCard(
    todayStats: TodayStats,
    dayLog: DayLog?
) {
    val completionRate = todayStats.completionRate
    val statusText = if (dayLog?.isClosed == true) "Completed" else "In Progress"

    // Determine progress color based on completion rate
    val progressColor = when {
        completionRate >= 90f -> Color(0xFF00D97E)  // Green - Excellent
        completionRate >= 70f -> Color(0xFF00D9FF)  // Cyan/Blue - Good
        completionRate >= 50f -> Color(0xFFFFB81C)  // Orange - Okay
        else -> Color(0xFFFF6B6B)                    // Red - Poor
    }

    val insightMessage = when {
        completionRate >= 90f -> "Excellent pace! You're crushing it today. 🎯"
        completionRate >= 70f -> "Great work! Keep up the momentum. 💪"
        completionRate >= 50f -> "Steady progress. You've got this! 📈"
        else -> "Focus on the remaining deliveries. Keep pushing! 🚀"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF252D3D))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top label "MERCURY"
        Text(
            "MERCURY",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF8A95A6),
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            letterSpacing = 1.sp
        )

        // Large Circular Progress (Whoop style - 200dp)
        Box(
            modifier = Modifier.size(180.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background circle
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(180.dp),
                color = Color(0xFF3A4557),
                strokeWidth = 9.dp,
                strokeCap = StrokeCap.Round,
                trackColor = Color.Transparent
            )

            // Progress circle
            CircularProgressIndicator(
                progress = { completionRate / 100f },
                modifier = Modifier.size(180.dp),
                color = progressColor,
                strokeWidth = 9.dp,
                strokeCap = StrokeCap.Round,
                trackColor = Color.Transparent
            )

            // Center text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    "${completionRate.toInt()}%",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Completion",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8A95A6),
                    fontSize = 11.sp
                )
            }
        }

        // Status text
        Text(
            statusText,
            style = MaterialTheme.typography.bodySmall,
            color = if (statusText == "Completed") progressColor else Color(0xFF8A95A6),
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp
        )

        // Metrics breakdown (Whoop style)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1F2634))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Completed
            MetricRow(
                icon = "✓",
                label = "Completed",
                value = "${todayStats.completedCount}",
                valueColor = Color(0xFF00D97E),
                fontSize = 10.sp
            )

            Divider(color = Color(0xFF3A4557), thickness = 0.5.dp)

            // Duration
            MetricRow(
                icon = "⏱",
                label = "Duration",
                value = todayStats.shiftDurationFormatted,
                valueColor = Color.White,
                fontSize = 10.sp
            )

            Divider(color = Color(0xFF3A4557), thickness = 0.5.dp)

            // Delayed
            MetricRow(
                icon = "⚠",
                label = "Delayed",
                value = "${todayStats.delayedCount}",
                valueColor = if (todayStats.delayedCount > 0) Color(0xFFFF6B6B) else Color(0xFF00D97E),
                fontSize = 10.sp
            )
        }

        // Insight box (Whoop style)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1F2634)),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("💡", fontSize = 16.sp, modifier = Modifier.padding(top = 1.dp))
                Text(
                    insightMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFC4D0DE),
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
private fun MetricRow(
    icon: String,
    label: String,
    value: String,
    valueColor: Color,
    fontSize: TextUnit = 12.sp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(icon, fontSize = 14.sp)
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8A95A6),
                fontSize = 9.sp
            )
        }
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            fontSize = fontSize
        )
    }
}

/**
 * Past day card - Whoop style design
 */
@Composable
private fun PastDayCard(
    dayLog: DayLog,
    viewModel: PerformanceDashboardViewModel
) {
    // Parse the date string (format: "yyyy-MM-dd") and format it for display
    val dateStr = try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val date = sdf.parse(dayLog.date) ?: java.util.Date()
        val displayFormat = java.text.SimpleDateFormat("EEE MMM dd", java.util.Locale.getDefault())
        displayFormat.format(date)
    } catch (e: Exception) {
        dayLog.date  // Fallback to raw date string
    }
    val completedCount = dayLog.totalJobsCompleted

    // Calculate completion percentage (assuming 10 jobs per day target)
    val completionPercentage = (completedCount / 10f) * 100f

    // Color coding: Green (80%+), Cyan/Yellow (60-80%), Orange (40-60%), Red (<40%)
    val progressColor = when {
        completionPercentage >= 80f -> Color(0xFF00D97E)  // Green - Excellent
        completionPercentage >= 60f -> Color(0xFFFFB81C)  // Orange/Yellow - Good
        completionPercentage >= 40f -> Color(0xFFFF9500)  // Orange - Okay
        else -> Color(0xFFFF6B6B)                          // Red - Poor
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF252D3D))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Small circular progress
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(56.dp),
                color = Color(0xFF3A4557),
                strokeWidth = 3.dp,
                strokeCap = StrokeCap.Round,
                trackColor = Color.Transparent
            )

            CircularProgressIndicator(
                progress = { completionPercentage / 100f },
                modifier = Modifier.size(56.dp),
                color = progressColor,
                strokeWidth = 3.dp,
                strokeCap = StrokeCap.Round,
                trackColor = Color.Transparent
            )

            Text(
                "${completionPercentage.toInt()}%",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = progressColor,
                textAlign = TextAlign.Center
            )
        }

        // Date and stats column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Date
            Text(
                dateStr,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                fontSize = 13.sp
            )

            // Jobs completed
            Text(
                "$completedCount jobs completed",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8A95A6),
                fontSize = 11.sp
            )
        }

        // Performance indicator
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(progressColor, shape = CircleShape)
        )
    }
}
