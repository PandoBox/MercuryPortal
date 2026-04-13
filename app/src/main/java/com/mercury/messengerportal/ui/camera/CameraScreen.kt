package com.mercury.messengerportal.ui.camera

import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import com.mercury.messengerportal.util.LocationHelper
import com.mercury.messengerportal.util.PhotoHelper
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Composable
fun CameraScreen(
    jobId: String,
    logId: String,
    onPhotoCaptured: (photoUrl: String?) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    // Hold references that survive recomposition.
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var isInitializing by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // A stable reference to the PreviewView so the LaunchedEffect can call
    // setSurfaceProvider directly without relying on AndroidView.update timing.
    val previewViewRef = remember { mutableStateOf<PreviewView?>(null) }

    // ── Camera setup ─────────────────────────────────────────────────────────
    // Runs inside a LaunchedEffect so it is scoped to the Compose lifecycle and
    // properly suspends until the ProcessCameraProvider is ready.  The old pattern
    // (setupCamera inside AndroidView factory) was fire-and-forget: if the future
    // resolved after a recomposition the ImageCapture reference was never stored,
    // leaving imageCapture == null and the shutter button silently doing nothing.
    LaunchedEffect(lifecycleOwner) {
        try {
            val provider = suspendCancellableCoroutine<ProcessCameraProvider> { cont ->
                val future = ProcessCameraProvider.getInstance(context)
                future.addListener({
                    try { cont.resume(future.get()) }
                    catch (e: Exception) { cont.resumeWithException(e) }
                }, ContextCompat.getMainExecutor(context))
            }

            val preview = Preview.Builder().build()
            val capture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                capture
            )

            // Wire the surface provider now that we have a bound Preview.
            // previewViewRef.value is set synchronously in the AndroidView factory,
            // so it is always available by the time this coroutine resumes on the main thread.
            previewViewRef.value?.let { preview.setSurfaceProvider(it.surfaceProvider) }

            cameraProvider = provider
            imageCapture = capture
            isInitializing = false
        } catch (e: Exception) {
            errorMessage = "Camera failed to start: ${e.message}"
            isInitializing = false
        }
    }

    // Release the camera when leaving this screen to prevent "camera in use" errors on re-entry.
    DisposableEffect(lifecycleOwner) {
        onDispose { cameraProvider?.unbindAll() }
    }

    // ── UI ───────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera preview — factory runs synchronously on first composition, storing the
        // PreviewView reference before the LaunchedEffect coroutine body executes.
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { pv ->
                    pv.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    previewViewRef.value = pv
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
            }
            Spacer(Modifier.weight(1f))
            Text(
                "Camera only — no gallery",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            errorMessage?.let {
                Text(it, color = Color.Red, style = MaterialTheme.typography.labelMedium)
            }

            // Shutter button
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isInitializing || isCapturing -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    else -> {
                        val capture = imageCapture
                        Button(
                            onClick = {
                                if (capture == null) {
                                    errorMessage = "Camera not ready — please wait"
                                    return@Button
                                }
                                isCapturing = true
                                errorMessage = null
                                scope.launch {
                                    try {
                                        val location = try {
                                            LocationHelper.getCurrentLocation(context)
                                        } catch (e: Exception) {
                                            null // Photo proceeds even if GPS fails
                                        }
                                        val file = PhotoHelper.capturePhoto(
                                            context = context,
                                            imageCapture = capture,
                                            executor = ContextCompat.getMainExecutor(context),
                                            location = location
                                        )
                                        // TODO: Upload file to server, get back photoUrl
                                        val photoUrl = "dummy://photo/${file.name}"
                                        PhotoHelper.deleteTemp(file)

                                        // TODO: update status log with photoUrl via ViewModel
                                        onPhotoCaptured(photoUrl)
                                    } catch (e: Exception) {
                                        errorMessage = "Capture failed: ${e.message}"
                                        isCapturing = false
                                    }
                                }
                            },
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Box(
                                Modifier
                                    .size(56.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                        }
                    }
                }
            }

            Text(
                if (isInitializing) "Initializing camera…" else "Tap to capture",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )

            // ── PROTOTYPE BYPASS ─────────────────────────────────────────────
            // Skips the camera entirely so the status transition can be tested
            // without a working camera. Remove this before going to production.
            OutlinedButton(
                onClick = { onPhotoCaptured(null) },
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color(0xFFFFA000).copy(alpha = 0.8f)
                )
            ) {
                Text(
                    "⚠ Skip Photo (Prototype)",
                    color = Color(0xFFFFA000),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
