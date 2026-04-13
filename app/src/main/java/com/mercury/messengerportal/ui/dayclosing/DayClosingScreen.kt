package com.mercury.messengerportal.ui.dayclosing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mercury.messengerportal.domain.model.GpsEvent
import com.mercury.messengerportal.util.formatLatLng
import com.mercury.messengerportal.util.toDateTimeString
import com.mercury.messengerportal.util.toTimeString

private data class ActivityPeriod(
    val id: String,
    val type: String, // "HQ_STANDBY", "JOB", or "STANDALONE"
    val events: List<GpsEvent>,
    val startTime: Long,
    val endTime: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayClosingScreen(
    onDayClosed: () -> Unit,
    onBack: () -> Unit,
    viewModel: DayClosingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showConfirmDialog by remember { mutableStateOf(false) }

    val totalJobs = state.jobs.size
    val completedJobs = state.jobs.count { it.status == com.mercury.messengerportal.domain.model.JobStatus.COMPLETED }
    val delayedJobs = state.jobs.count { it.status == com.mercury.messengerportal.domain.model.JobStatus.DELAYED }
    val pendingJobs = state.jobs.count { it.status != com.mercury.messengerportal.domain.model.JobStatus.COMPLETED && it.status != com.mercury.messengerportal.domain.model.JobStatus.DELAYED }

    LaunchedEffect(state.currentJobForReason) {
        if (state.currentJobForReason != null) {
            showConfirmDialog = true
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Day Summary", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            if (!state.isClosed) {
                Column(Modifier.padding(16.dp)) {
                    // Determine button color based on job status
                    val jobStatuses = state.jobs.map { it.status }
                    val allCompleted = jobStatuses.all { it == com.mercury.messengerportal.domain.model.JobStatus.COMPLETED }
                    val onlyCompletedOrDelayed = jobStatuses.all { it in listOf(com.mercury.messengerportal.domain.model.JobStatus.COMPLETED, com.mercury.messengerportal.domain.model.JobStatus.DELAYED) }

                    val buttonColor = when {
                        allCompleted -> Color(0xFF2E7D32) // Green - all completed
                        onlyCompletedOrDelayed -> Color(0xFFFBC02D) // Yellow - only completed or delayed
                        else -> Color(0xFFC62828) // Red - has other statuses (ASSIGNED, DEPARTED, ARRIVED, IN_PROGRESS)
                    }

                    Button(
                        onClick = {
                            viewModel.closeDay()
                            if (pendingJobs > 0) {
                                showConfirmDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = !state.isClosing,
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                    ) {
                        if (state.isClosing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Icon(Icons.Default.NightShelter, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Confirm Day Close & Clock Out", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary card
            item {
                state.dayLog?.let { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Job statistics - 4 column layout (single row)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Total Jobs
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            totalJobs.toString(),
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            "Total",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                // Completed Jobs
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF2E7D32).copy(alpha = 0.15f))
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            completedJobs.toString(),
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF2E7D32)
                                        )
                                        Text(
                                            "Completed",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                // Pending Jobs - Lite Yellow
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFFF9C4))
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            pendingJobs.toString(),
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFF57C00)
                                        )
                                        Text(
                                            "Pending",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                // Delayed Jobs - Lite Red
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFFEBEE))
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            delayedJobs.toString(),
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFC62828)
                                        )
                                        Text(
                                            "Delayed",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // GPS Trail header
            item {
                Text(
                    "Activity Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (state.gpsTrail.isEmpty()) {
                item {
                    Text(
                        "No GPS events recorded yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }

            // Build periods: HQ standby periods, job periods, and standalone events
            val periods = mutableListOf<ActivityPeriod>()
            val processedEvents = mutableSetOf<String>()
            val groupedByJob = state.gpsTrail.groupBy { it.jobId }

            state.gpsTrail.forEach { event ->
                if (processedEvents.contains(event.eventLabel + event.timestamp)) return@forEach

                when {
                    // HQ Standby period: starts with CLOCK_IN or ARRIVE_HQ and ends with DEPART_HQ
                    (event.eventLabel == "Clock In" || event.eventLabel == "Arrive HQ") && !processedEvents.contains("HQ_STANDBY_${event.timestamp}") -> {
                        val startIdx = state.gpsTrail.indexOf(event)
                        val hqEvents = mutableListOf<GpsEvent>()
                        hqEvents.add(event)
                        processedEvents.add("HQ_STANDBY_${event.timestamp}")

                        // Collect events until we hit Depart HQ (or end of trail for ongoing standby)
                        for (i in startIdx + 1 until state.gpsTrail.size) {
                            val e = state.gpsTrail[i]
                            if (e.eventLabel == "Depart HQ") {
                                hqEvents.add(e)
                                processedEvents.add(e.eventLabel + e.timestamp)
                                break
                            }
                            if (e.jobId != null) {
                                // Don't include job events
                                break
                            }
                            // Include other HQ events
                            hqEvents.add(e)
                            processedEvents.add(e.eventLabel + e.timestamp)
                        }

                        // Add period whether or not it has been completed with Depart
                        periods.add(
                            ActivityPeriod(
                                id = "HQ_STANDBY_${event.timestamp}",
                                type = "HQ_STANDBY",
                                events = hqEvents,
                                startTime = event.timestamp,
                                endTime = hqEvents.lastOrNull()?.timestamp ?: event.timestamp
                            )
                        )
                    }

                    // Job period
                    event.jobId != null && !processedEvents.contains("JOB_${event.jobId}") -> {
                        val jobEvents = groupedByJob[event.jobId] ?: emptyList()
                        jobEvents.forEach {
                            processedEvents.add(it.eventLabel + it.timestamp)
                        }
                        periods.add(
                            ActivityPeriod(
                                id = "JOB_${event.jobId}",
                                type = "JOB",
                                events = jobEvents,
                                startTime = jobEvents.minOf { it.timestamp },
                                endTime = jobEvents.maxOf { it.timestamp }
                            )
                        )
                    }

                    // Standalone event (Clock Out)
                    event.eventLabel == "Clock Out" && !processedEvents.contains(event.eventLabel + event.timestamp) -> {
                        processedEvents.add(event.eventLabel + event.timestamp)
                        periods.add(
                            ActivityPeriod(
                                id = "${event.eventLabel}_${event.timestamp}",
                                type = "STANDALONE",
                                events = listOf(event),
                                startTime = event.timestamp,
                                endTime = event.timestamp
                            )
                        )
                    }
                }
            }

            // Sort periods by start time and display
            periods.sortedBy { it.startTime }.forEach { period ->
                when (period.type) {
                    "HQ_STANDBY" -> {
                        item {
                            var expanded by remember { mutableStateOf(false) }
                            val firstEvent = period.events.firstOrNull()
                            val arrive = firstEvent?.timestamp
                            val depart = period.events.firstOrNull { it.eventLabel == "Depart HQ" }?.timestamp

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { expanded = !expanded },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)) // Light yellow
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Standby at HQ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                                    }
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Arrive: ${arrive?.toTimeString() ?: "-"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.6f))
                                        if (depart != null) {
                                            Text("Depart: ${depart.toTimeString()}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.6f))
                                        }
                                    }
                                    if (expanded) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                        period.events.forEach { ev ->
                                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(ev.eventLabel, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                                    ev.locationAddress?.let {
                                                        Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.6f))
                                                    }
                                                }
                                                Text(ev.timestamp.toTimeString(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "JOB" -> {
                        item {
                            var expanded by remember { mutableStateOf(false) }
                            val seq = period.events.firstOrNull()?.jobSequence ?: 0
                            val departTime = period.startTime
                            val completedTime = period.events.maxByOrNull { it.timestamp }?.timestamp
                            val isCompleted = period.events.any { it.rawStatus == "COMPLETED" }

                            // Get the actual job to access status and remarks
                            val jobId = period.events.firstOrNull()?.jobId
                            val job = jobId?.let { state.jobs.firstOrNull { it.id == jobId } }
                            val isDelayed = job?.status == com.mercury.messengerportal.domain.model.JobStatus.DELAYED

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { expanded = !expanded },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDelayed) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Job #$seq", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                                    }
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Depart: ${departTime.toTimeString()}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.6f))
                                        if (isCompleted) {
                                            Text("Complete: ${completedTime?.toTimeString() ?: "-"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.6f))
                                        } else if (isDelayed) {
                                            Text("Status: Delayed", style = MaterialTheme.typography.labelMedium, color = Color(0xFFC62828))
                                        }
                                    }
                                    if (expanded) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                        period.events.forEach { ev ->
                                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(ev.rawStatus ?: ev.eventLabel, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                                    ev.locationAddress?.let {
                                                        Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.6f))
                                                    }
                                                }
                                                Text(ev.timestamp.toTimeString(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }

                                        // Show delay remark only for delayed jobs (only today's reason)
                                        if (isDelayed && job?.messengerRemark?.isNotEmpty() == true) {
                                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                            Text("Delay Remark:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                            // Extract just the reason text without date prefix
                                            val reasonText = job.messengerRemark.substringAfter(": ").takeIf { it.isNotEmpty() } ?: job.messengerRemark
                                            Text(reasonText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.7f))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "STANDALONE" -> {
                        item {
                            GpsEventRow(event = period.events.first())
                        }
                    }
                }
            }

            // Show any DELAYED jobs that don't have GPS trail entries
            val jobsWithGpsEvents = periods.filter { it.type == "JOB" }.mapNotNull { it.events.firstOrNull()?.jobId }.toSet()
            val delayedJobsWithoutGps = state.jobs.filter {
                it.status == com.mercury.messengerportal.domain.model.JobStatus.DELAYED && !jobsWithGpsEvents.contains(it.id)
            }

            delayedJobsWithoutGps.forEach { job ->
                item {
                    var expanded by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { expanded = !expanded },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)) // Light red
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Job #${job.sequenceOrder}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Status: Delayed", style = MaterialTheme.typography.labelMedium, color = Color(0xFFC62828))
                            }
                            if (expanded) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                Text(job.locationName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)

                                // Show delay remark if available (only today's reason)
                                if (job.messengerRemark?.isNotEmpty() == true) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                    Text("Delay Remark:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                    // Extract just the reason text without date prefix
                                    val reasonText = job.messengerRemark.substringAfter(": ").takeIf { it.isNotEmpty() } ?: job.messengerRemark
                                    Text(reasonText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.7f))
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    // Confirm dialog
    if (showConfirmDialog) {
        var reasonText by remember { mutableStateOf("") }

        LaunchedEffect(state.currentJobForReason) {
            reasonText = ""
        }

        state.currentJobForReason?.let { currentJob ->
            // Show reason input for current pending job
            AlertDialog(
                onDismissRequest = {
                    showConfirmDialog = false
                    viewModel.cancelClosing()
                },
                title = { Text("Reason for Pending Job") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Job #${currentJob.sequenceOrder} - ${currentJob.locationName}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Why is this job pending? (required)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        OutlinedTextField(
                            value = reasonText,
                            onValueChange = { reasonText = it },
                            label = { Text("Reason") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            isError = reasonText.isBlank()
                        )
                        if (reasonText.isBlank()) {
                            Text(
                                "Reason is required",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.addReasonForJob(currentJob.id, reasonText)
                            if (state.pendingJobsNeedingReason.size <= 1) {
                                showConfirmDialog = false
                            }
                        },
                        enabled = reasonText.isNotBlank()
                    ) {
                        Text("Next")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showConfirmDialog = false
                        viewModel.cancelClosing()
                    }) { Text("Cancel") }
                }
            )
        }
    }

    state.error?.let { error ->
        AlertDialog(
            onDismissRequest = viewModel::dismissError,
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = { TextButton(onClick = viewModel::dismissError) { Text("OK") } }
        )
    }

    // Clock Out location picker dialog (single source of truth)
    ClockOutLocationPickerDialog(state = state, viewModel = viewModel)
}

@Composable
private fun ClockOutLocationPickerDialog(state: DayClosingUiState, viewModel: DayClosingViewModel) {
    if (state.showClockOutLocationPicker) {
        AlertDialog(
            onDismissRequest = viewModel::cancelLocationPicker,
            title = { Text("Choose Location") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "[Pilot-Test] Select which location to stamp for clock out:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    // Option A — HQ Location
                    OutlinedButton(
                        onClick = {
                            viewModel.selectClockOutLocation(13.8056, 100.5531, "BTS HQ (BTSC)")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.LocationOn, null)
                        Spacer(Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("HQ Location", fontWeight = FontWeight.SemiBold)
                            Text(
                                "BTSC",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                "13.8056°N, 100.5531°E",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                    // Option B — Current Location
                    OutlinedButton(
                        onClick = {
                            viewModel.selectClockOutLocation(13.7650, 100.6381, "BTS Visionary Park, Bangkok")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.GpsFixed, null)
                        Spacer(Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("Current Location", fontWeight = FontWeight.SemiBold)
                            Text(
                                "⚠ Spoofed: BTS Visionary Park",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFA000)
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = viewModel::cancelLocationPicker) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun GpsEventRow(event: GpsEvent) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(20.dp).padding(top = 2.dp),
            tint = if (event.isOfflineQueued && event.syncedAt == null)
                Color(0xFFEF6C00) else MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(event.eventLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                event.timestamp.toDateTimeString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            // Show Google-resolved address when available
            event.locationAddress?.let { address ->
                Text(
                    address,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }
            if (event.isOfflineQueued) {
                val syncText = event.syncedAt
                    ?.let { "Synced at ${it.toDateTimeString()}" }
                    ?: "Pending sync..."
                Text(
                    "Recorded offline — $syncText",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFEF6C00)
                )
            }
        }
    }
}
