package com.mercury.messengerportal.ui.jobdetail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import com.mercury.messengerportal.domain.model.JobStatus
import com.mercury.messengerportal.domain.model.JobStatusLog
import com.mercury.messengerportal.ui.components.StatusChip
import com.mercury.messengerportal.ui.navigation.Screen
import com.mercury.messengerportal.ui.theme.StatusCompleted
import com.mercury.messengerportal.util.PilotConfig
import com.mercury.messengerportal.util.formatLatLng
import com.mercury.messengerportal.util.googleMapsNavUri
import com.mercury.messengerportal.util.toDateTimeString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(
    jobId: String,
    navController: androidx.navigation.NavController? = null,
    onNavigateToCamera: (jobId: String, logId: String, requirePhoto: Boolean) -> Unit,
    onBack: () -> Unit,
    viewModel: JobDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(jobId) { viewModel.loadJob(jobId) }

    // Listen for photo results from CameraScreen
    if (navController != null) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val savedStateHandle = navBackStackEntry?.savedStateHandle
        val photoUrl = savedStateHandle?.get<String?>("photoUrl")

        LaunchedEffect(photoUrl) {
            if (savedStateHandle?.contains("photoUrl") == true) {
                viewModel.commitAfterCamera(photoUrl)
                savedStateHandle.remove<String?>("photoUrl")
            }
        }
    }

    // When primary action requires camera, navigate
    LaunchedEffect(state.pendingCameraLogId) {
        val logId = state.pendingCameraLogId ?: return@LaunchedEffect
        val nextStatus = state.pendingNextStatus ?: return@LaunchedEffect
        onNavigateToCamera(jobId, logId, nextStatus.requiresPhoto())
        viewModel.onCameraNavigated()
    }

    // Navigate to next job after depart
    LaunchedEffect(state.navigateToJobId) {
        val nextJobId = state.navigateToJobId ?: return@LaunchedEffect
        navController?.navigate(Screen.JobDetail.createRoute(nextJobId))
        viewModel.onNavigatedToNextJob()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Job #${state.job?.sequenceOrder ?: ""}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        state.job?.type?.let {
                            Text(it.displayName(), style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                        }
                    }
                },
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
            state.job?.let { job ->
                val nextStatus = job.status.nextAction()
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Show "Depart Next Job" if just completed or delayed
                    if (state.justStatusChanged && (job.status == JobStatus.COMPLETED || job.status == JobStatus.DELAYED)) {
                        Button(
                            onClick = viewModel::onDepartNextJob,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = !state.isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(Icons.Default.NavigateNext, null)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Depart Next Job",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else if (nextStatus != null) {
                        Button(
                            onClick = viewModel::onPrimaryAction,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = !state.isLoading
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(Icons.Default.PlayArrow, null)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    nextStatus.actionLabel() ?: "",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (nextStatus.requiresPhoto()) {
                                    Spacer(Modifier.width(4.dp))
                                    Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        // [Pilot Test] Bypass camera at complete job step
                        if (nextStatus.isTerminal() && job.status != JobStatus.ARRIVED) {
                            TextButton(
                                onClick = {
                                    viewModel.onBypassCameraForStatus(job, nextStatus)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("[Pilot Test] Bypass Camera & Complete", color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }

                    if (job.status == JobStatus.ASSIGNED) {
                        OutlinedButton(
                            onClick = viewModel::onShowReassignDialog,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.SwapHoriz, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Request Re-assign")
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (state.isLoading && state.job == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val job = state.job ?: return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status card
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Current Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    StatusChip(
                        status = job.status,
                        textStyle = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Location card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Location", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(job.locationName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Text(job.locationAddress, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    if (job.notes.isNotBlank()) {
                        HorizontalDivider()
                        Text("Job Remark", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(job.notes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Messenger Remark", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            if (job.messengerRemark.isNotBlank()) {
                                Text(job.messengerRemark, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            } else {
                                Text("No remark added", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }
                        if (!job.status.isTerminal()) {
                            IconButton(onClick = viewModel::onShowEditNote) {
                                Icon(
                                    if (job.messengerRemark.isNotBlank()) Icons.Default.Edit else Icons.Default.Add,
                                    contentDescription = "Edit Remark",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    if (job.status == JobStatus.ASSIGNED || job.status == JobStatus.DEPARTED) {
                        OutlinedButton(
                            onClick = {
                                val uri = googleMapsNavUri(job.latitude, job.longitude, job.locationName)
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply {
                                    setPackage("com.google.android.apps.maps")
                                })
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Navigation, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Navigate")
                        }
                    }
                }
            }

            // Contact card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Contacts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        if (job.status != JobStatus.COMPLETED) {
                            Button(
                                onClick = viewModel::onShowDelayDialog,
                                modifier = Modifier.height(32.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Delay", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            ContactItem("ผู้ส่ง (Sender)", job.senderName, job.senderPhone, context)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ContactItem("ผู้รับ (Receiver)", job.receiverName, job.receiverPhone, context)
                        }
                    }
                }
            }

            // Status History
            if (state.statusLogs.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Activity Log", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        state.statusLogs.forEach { log ->
                            StatusLogRow(log)
                        }
                    }
                }
            }
        }
    }

    // [Pilot-config] Arrival location picker dialog
    // Shown when ARRIVED transition is tapped and ARRIVAL_LOCATION_PICKER_ENABLED = true.
    if (state.showArrivalLocationPicker) {
        val job = state.job
        AlertDialog(
            onDismissRequest = viewModel::onDismissArrivalPicker,
            title = { Text("Choose Location") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "[Pilot-config] Select which location to stamp:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    // Option A — Job's expected destination
                    OutlinedButton(
                        onClick = { viewModel.onArrivalLocationChosen(ArrivalLocationChoice.JOB_DESTINATION) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.LocationOn, null)
                        Spacer(Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("Destination Location", fontWeight = FontWeight.SemiBold)
                            if (job != null) {
                                Text(
                                    job.locationName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    formatLatLng(job.latitude, job.longitude),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                    // Option B — Current GPS (may be spoofed by PilotConfig)
                    OutlinedButton(
                        onClick = { viewModel.onArrivalLocationChosen(ArrivalLocationChoice.CURRENT_GPS) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.GpsFixed, null)
                        Spacer(Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text("Current Location", fontWeight = FontWeight.SemiBold)
                            if (PilotConfig.FAKE_LOCATION_ENABLED) {
                                Text(
                                    "⚠ Spoofed: BTS Visionary Park",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFFA000)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = viewModel::onDismissArrivalPicker) { Text("Cancel") }
            }
        )
    }

    // Re-assign dialog
    if (state.showReassignDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissReassign,
            title = { Text("Request Re-assign") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Provide a reason. Admin will review your request.")
                    OutlinedTextField(
                        value = state.reassignNote,
                        onValueChange = viewModel::onReassignNoteChange,
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::onConfirmReassign,
                    enabled = state.reassignNote.isNotBlank()
                ) { Text("Submit") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissReassign) { Text("Cancel") }
            }
        )
    }

    // Edit Note dialog
    if (state.showEditNoteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissEditNote,
            title = { Text("Edit Messenger Remark") },
            text = {
                OutlinedTextField(
                    value = state.editNoteText,
                    onValueChange = viewModel::onEditNoteTextChange,
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::onConfirmEditNote) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissEditNote) { Text("Cancel") }
            }
        )
    }

    // Delay dialog
    if (state.showDelayDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDelayDialog,
            title = { Text("Reason for Delay") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Why is this job delayed? (required)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    OutlinedTextField(
                        value = state.delayReason,
                        onValueChange = viewModel::onDelayReasonChange,
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        isError = state.delayReason.isBlank()
                    )
                    if (state.delayReason.isBlank()) {
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
                    onClick = viewModel::onConfirmDelay,
                    enabled = state.delayReason.isNotBlank()
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissDelayDialog) { Text("Cancel") }
            }
        )
    }

    state.error?.let { error ->
        AlertDialog(
            onDismissRequest = viewModel::dismissError,
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = { TextButton(onClick = viewModel::dismissError) { Text("OK") } }
        )
    }
}

@Composable
private fun ContactItem(label: String, name: String, phone: String, context: android.content.Context) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        if (phone.isNotBlank() && phone != "-") {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                Icon(Icons.Default.Phone, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(phone, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                    context.startActivity(intent)
                },
                modifier = Modifier.padding(top = 8.dp).fillMaxWidth().height(36.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50), contentColor = Color.White),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(18.dp))
            }
        } else {
            Text("No phone", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.5f), modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun StatusLogRow(log: JobStatusLog) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Default.Circle,
            contentDescription = null,
            modifier = Modifier.size(10.dp).padding(top = 4.dp),
            tint = if (log.status == JobStatus.COMPLETED) StatusCompleted else MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(log.status.displayName(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                log.deviceTimestamp.toDateTimeString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            // Show Google-resolved address when available
            log.locationAddress?.let { address ->
                Text(
                    address,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }
            if (log.isOfflineQueued) {
                val syncedText = log.syncedAt?.let { "Synced at ${it.toDateTimeString()}" } ?: "Pending sync..."
                Text(
                    "Recorded offline — $syncedText",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFEF6C00)
                )
            }
        }
    }
}