package com.mercury.messengerportal.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import com.mercury.messengerportal.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.mercury.messengerportal.ui.theme.MercuryNavy
import com.mercury.messengerportal.ui.theme.MercuryNavyDark
import com.mercury.messengerportal.util.formatLatLng
import com.mercury.messengerportal.util.toTimeString

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onViewJobs: () -> Unit,
    onDayClosing: () -> Unit,
    onDepartJob: (String) -> Unit = {},
    onOpenDashboard: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val locationPermissions = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    var showClockInDialog by remember { mutableStateOf(false) }
    var hasPromptedClockIn by remember { mutableStateOf(false) }

    LaunchedEffect(state.isClockedIn, state.isLoading) {
        if (!state.isLoading && !state.isClockedIn && state.todayLog == null && !hasPromptedClockIn) {
            showClockInDialog = true
        }
    }

    if (showClockInDialog) {
        AlertDialog(
            onDismissRequest = { 
                showClockInDialog = false
                hasPromptedClockIn = true
            },
            title = { Text("Daily Clock In") },
            text = { Text("Welcome! Please clock in to start your shift. You'll need GPS active.") },
            confirmButton = {
                Button(onClick = {
                    showClockInDialog = false
                    hasPromptedClockIn = true
                    if (locationPermissions.allPermissionsGranted) viewModel.clockIn()
                    else locationPermissions.launchMultiplePermissionRequest()
                }) {
                    Text("Clock In Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showClockInDialog = false
                    hasPromptedClockIn = true 
                }) {
                    Text("Later")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(MercuryNavyDark, MercuryNavy)))
                    .padding(top = 48.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Welcome back,",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            state.messenger?.name ?: "...",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "Mercury App Logo",
                            modifier = Modifier.width(60.dp).height(30.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "MERCURY",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Clock-In Card & Depart Button
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ClockInCard(
                    state = state,
                    onClockIn = {
                        if (locationPermissions.allPermissionsGranted) viewModel.clockIn()
                        else locationPermissions.launchMultiplePermissionRequest()
                    }
                )

                if (state.isClockedIn) {
                    if (state.isAtHq && state.pendingCount > 0) {
                        Button(
                            onClick = { 
                                if (locationPermissions.allPermissionsGranted) viewModel.departFirstJob(onDepartJob)
                                else locationPermissions.launchMultiplePermissionRequest()
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Depart from HQ", fontWeight = FontWeight.Bold)
                        }
                    } else if (!state.isAtHq) {
                        Button(
                            onClick = { 
                                if (locationPermissions.allPermissionsGranted) viewModel.arriveHq()
                                else locationPermissions.launchMultiplePermissionRequest()
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Arrive HQ", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Job Summary Row
            JobSummaryRow(state = state)

            // Action Buttons
            if (state.jobs.isNotEmpty()) {
                TodayJobsSummaryCard(
                    jobs = state.jobs,
                    onViewJobs = onViewJobs,
                    enabled = state.isClockedIn
                )
            } else {
                Button(
                    onClick = onViewJobs,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = state.isClockedIn
                ) {
                    Icon(Icons.Default.List, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("View Jobs | Re-arrange Jobs (${state.jobs.size})", fontWeight = FontWeight.SemiBold)
                }
            }



            OutlinedButton(
                onClick = onDayClosing,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = state.isClockedIn
            ) {
                Icon(Icons.Default.NightShelter, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Day Review | Day Closing", fontWeight = FontWeight.SemiBold)
            }

            // My Performance Button
            OutlinedButton(
                onClick = onOpenDashboard,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = state.isClockedIn || state.completedCount > 0
            ) {
                Icon(Icons.Default.BarChart, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("My Performance", fontWeight = FontWeight.SemiBold)
            }

            if (!state.isClockedIn) {
                Spacer(Modifier.weight(1f, fill = false))
                Text(
                    "Clock in to start your shift and access jobs.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else if (state.jobs.isNotEmpty() && state.completedCount == state.jobs.size) {
                Spacer(Modifier.weight(1f, fill = false))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(32.dp))
                        Text(
                            "Ready at HQ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Text(
                            "All assigned jobs are completed. Awaiting second round or ready for day closing.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF2E7D32),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(Modifier.weight(1f))

            // [Pilot/Dev] Reset Button moved to absolute bottom
            TextButton(
                onClick = viewModel::resetTodayProgress,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("[Pilot/Dev] Reset Today's Progress", style = MaterialTheme.typography.labelMedium)
            }
        }

        // Error snackbar
        state.error?.let { error ->
            AlertDialog(
                onDismissRequest = viewModel::dismissError,
                title = { Text("Location Error") },
                text = { Text(error) },
                confirmButton = {
                    TextButton(onClick = viewModel::dismissError) { Text("OK") }
                }
            )
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun ClockInCard(state: HomeUiState, onClockIn: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (state.isClockedIn)
                Color(0xFFE3F2FD) // Light blue vibrant tint
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (state.isClockedIn || state.todayLog?.clockOutTime != null) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (state.isClockedIn || state.todayLog?.clockOutTime != null) Color(0xFF1976D2) else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    val statusText = if (state.todayLog?.clockOutTime != null) {
                        "Day Completed"
                    } else if (state.isClockedIn) {
                        if (state.isAtHq) "Stand by at HQ" else "On Duty"
                    } else {
                        "Off Duty"
                    }
                    Text(
                        statusText,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (state.isClockedIn || state.todayLog?.clockOutTime != null) Color(0xFF1976D2) else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )

                    state.todayLog?.let { log ->
                        Text(
                            if (log.clockOutTime != null)
                                "${log.clockInTime.toTimeString()} - ${log.clockOutTime.toTimeString()}"
                            else
                                "Since ${log.clockInTime.toTimeString()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF1565C0)
                        )
                    }
                }
            }

            if (!state.isClockedIn && state.todayLog?.clockOutTime == null) {
                Button(onClick = onClockIn) {
                    Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Clock In", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun JobSummaryRow(state: HomeUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryChip(
            modifier = Modifier.weight(1f),
            label = "Pending",
            count = state.pendingCount,
            color = Color(0xFF78909C)
        )
        SummaryChip(
            modifier = Modifier.weight(1f),
            label = "Active",
            count = state.inProgressCount,
            color = Color(0xFF1565C0)
        )
        SummaryChip(
            modifier = Modifier.weight(1f),
            label = "Done",
            count = state.completedCount,
            color = Color(0xFF2E7D32)
        )
    }
}

@Composable
private fun SummaryChip(
    modifier: Modifier,
    label: String,
    count: Int,
    color: Color
) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(label, style = MaterialTheme.typography.labelMedium, color = color)
        }
    }
}

@Composable
private fun TodayJobsSummaryCard(
    jobs: List<com.mercury.messengerportal.domain.model.Job>,
    onViewJobs: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = { if (enabled) onViewJobs() }
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.List, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Today's Jobs",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    "${jobs.size} Total",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            val groupedJobs = jobs.groupBy { it.type }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                groupedJobs.forEach { (type, typeJobs) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(com.mercury.messengerportal.ui.components.getJobTypeColor(type).copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = com.mercury.messengerportal.ui.components.getJobTypeIcon(type),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = com.mercury.messengerportal.ui.components.getJobTypeColor(type)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            type.displayName(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${typeJobs.size} jobs",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Button(
                onClick = onViewJobs,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled
            ) {
                Text("View Jobs | Re-arrange Jobs", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
