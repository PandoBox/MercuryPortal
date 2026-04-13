package com.mercury.messengerportal.util

import android.content.Context
import android.location.Location
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object PhotoHelper {

    /**
     * Captures a photo using CameraX [ImageCapture], embeds GPS + timestamp into EXIF metadata,
     * and returns a temporary [File]. The caller is responsible for uploading and deleting the file
     * after a successful upload — no permanent local storage.
     */
    suspend fun capturePhoto(
        context: Context,
        imageCapture: ImageCapture,
        executor: Executor,
        location: Location?
    ): File = suspendCancellableCoroutine { cont ->
        val cacheDir = File(context.cacheDir, "camera").also { it.mkdirs() }
        val fileName = "mercury_${System.currentTimeMillis()}.jpg"
        val photoFile = File(cacheDir, fileName)

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    embedExifData(photoFile, location)
                    cont.resume(photoFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    cont.resumeWithException(exception)
                }
            }
        )
    }

    /**
     * Embeds GPS coordinates and current timestamp into JPEG EXIF metadata.
     * This ensures photo provenance is verifiable even if metadata is stripped later.
     */
    private fun embedExifData(file: File, location: Location?) {
        try {
            val exif = ExifInterface(file.absolutePath)

            // Timestamp
            val dateTime = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())
                .format(Date())
            exif.setAttribute(ExifInterface.TAG_DATETIME, dateTime)
            exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, dateTime)

            // GPS
            location?.let { loc ->
                exif.setLatLong(loc.latitude, loc.longitude)
                exif.setAttribute(
                    ExifInterface.TAG_GPS_ALTITUDE,
                    loc.altitude.toBits().toString()
                )
                exif.setAttribute(
                    ExifInterface.TAG_GPS_ALTITUDE_REF,
                    if (loc.altitude >= 0) "0" else "1"
                )
            }

            exif.saveAttributes()
        } catch (e: Exception) {
            // Non-fatal — photo is still valid without EXIF
            android.util.Log.w("PhotoHelper", "Failed to embed EXIF: ${e.message}")
        }
    }

    /** Deletes the temporary photo file after a successful upload */
    fun deleteTemp(file: File) {
        if (file.exists()) file.delete()
    }
}
