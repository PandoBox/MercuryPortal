package com.mercury.messengerportal.ui.joblist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mercury.messengerportal.domain.model.Job
import com.mercury.messengerportal.domain.model.JobStatus
import com.mercury.messengerportal.ui.components.JobCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobListScreen(
    onJobClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: JobListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today's Jobs", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (state.jobs.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No jobs assigned today",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            return@Scaffold
        }

        val listState = rememberLazyListState()
        var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
        var draggingOffset by remember { mutableStateOf(0f) }

        val locationCounts = remember(state.jobs) {
            state.jobs.groupingBy { it.locationName }.eachCount()
        }

        // Separate active, delayed, and completed jobs
        val activeJobs = remember(state.jobs) {
            state.jobs.filter { it.status != JobStatus.COMPLETED && it.status != JobStatus.DELAYED }.sortedBy { it.sequenceOrder }
        }
        val delayedJobs = remember(state.jobs) {
            state.jobs.filter { it.status == JobStatus.DELAYED }.sortedBy { it.sequenceOrder }
        }
        val completedJobs = remember(state.jobs) {
            state.jobs.filter { it.status == JobStatus.COMPLETED }.sortedBy { it.sequenceOrder }
        }

        val viewConfig = LocalViewConfiguration.current
        val customViewConfig = remember(viewConfig) {
            object : ViewConfiguration by viewConfig {
                override val longPressTimeoutMillis: Long
                    get() = viewConfig.longPressTimeoutMillis / 2
            }
        }

        CompositionLocalProvider(LocalViewConfiguration provides customViewConfig) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { offset ->
                                    listState.layoutInfo.visibleItemsInfo
                                        .firstOrNull { item ->
                                            offset.y.toInt() in item.offset..(item.offset + item.size)
                                        }?.takeIf { it.key is String }?.let { item ->
                                            // Only allow dragging active jobs (not completed or delayed)
                                            val job = activeJobs.getOrNull(item.index)
                                            if (job?.status != JobStatus.COMPLETED && job?.status != JobStatus.DELAYED) {
                                                draggedItemIndex = item.index
                                            }
                                        }
                                },
                                onDrag = { change, dragAmount ->
                            change.consume()
                            draggingOffset += dragAmount.y

                            val draggedIndex = draggedItemIndex ?: return@detectDragGesturesAfterLongPress
                            val currentOffset = listState.layoutInfo.visibleItemsInfo
                                .getOrNull(draggedIndex - listState.firstVisibleItemIndex)?.offset ?: 0
                            val totalOffset = currentOffset + draggingOffset

                            val targetItem = listState.layoutInfo.visibleItemsInfo.find { item ->
                                totalOffset.toInt() in item.offset..(item.offset + item.size)
                            }

                            // Only swap if both are active jobs
                            if (targetItem != null && targetItem.index != draggedIndex && targetItem.index < activeJobs.size) {
                                viewModel.moveJob(draggedIndex, targetItem.index)
                                draggedItemIndex = targetItem.index
                                draggingOffset = 0f
                            }
                        },
                        onDragEnd = {
                            draggedItemIndex = null
                            draggingOffset = 0f
                        },
                        onDragCancel = {
                            draggedItemIndex = null
                            draggingOffset = 0f
                        }
                    )
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Active jobs section
                itemsIndexed(activeJobs, key = { _, job -> job.id }) { index, job ->
                    val isDragging = index == draggedItemIndex
                    val isShared = (locationCounts[job.locationName] ?: 0) > 1
                    JobListItem(
                        job = job,
                        isFirst = index == 0,
                        isLast = index == activeJobs.size - 1,
                        isSharedLocation = isShared,
                        onJobClick = onJobClick,
                        onMoveUp = { viewModel.moveJobUp(job) },
                        onMoveDown = { viewModel.moveJobDown(job) },
                        modifier = Modifier
                            .zIndex(if (isDragging) 1f else 0f)
                            .graphicsLayer {
                                translationY = if (isDragging) draggingOffset else 0f
                                scaleX = if (isDragging) 1.05f else 1f
                                scaleY = if (isDragging) 1.05f else 1f
                            }
                    )
                }

                // Completed jobs section
                if (completedJobs.isNotEmpty()) {
                    item {
                        Text(
                            "Completed",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                    itemsIndexed(completedJobs, key = { _, job -> "completed_${job.id}" }) { _, job ->
                        val isShared = (locationCounts[job.locationName] ?: 0) > 1
                        JobListItem(
                            job = job,
                            isFirst = false,
                            isLast = false,
                            isSharedLocation = isShared,
                            onJobClick = onJobClick,
                            onMoveUp = {},
                            onMoveDown = {},
                            modifier = Modifier
                        )
                    }
                }

                // Delayed jobs section
                if (delayedJobs.isNotEmpty()) {
                    item {
                        Text(
                            "Delayed",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                    itemsIndexed(delayedJobs, key = { _, job -> "delayed_${job.id}" }) { _, job ->
                        val isShared = (locationCounts[job.locationName] ?: 0) > 1
                        JobListItem(
                            job = job,
                            isFirst = false,
                            isLast = false,
                            isSharedLocation = isShared,
                            onJobClick = onJobClick,
                            onMoveUp = {},
                            onMoveDown = {},
                            modifier = Modifier
                        )
                    }
                }
            }
            Text(
                "💡 Tip: Long-press any job to drag and re-arrange.\nJobs in the same location share the same highlight color.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            }
        }
    }
}

@Composable
private fun JobListItem(
    job: Job,
    isFirst: Boolean,
    isLast: Boolean,
    isSharedLocation: Boolean,
    onJobClick: (String) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Reorder controls — only show for non-completed and non-delayed jobs
        if (job.status != JobStatus.COMPLETED && job.status != JobStatus.DELAYED) {
            Column {
                IconButton(
                    onClick = onMoveUp,
                    enabled = !isFirst,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = "Move up",
                        tint = if (!isFirst) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                }
                IconButton(
                    onClick = onMoveDown,
                    enabled = !isLast,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Move down",
                        tint = if (!isLast) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                }
            }
        } else {
            Spacer(Modifier.width(32.dp))
        }

        JobCard(
            job = job,
            isSharedLocation = isSharedLocation,
            modifier = Modifier.weight(1f),
            onClick = { onJobClick(job.id) }
        )
    }
}
