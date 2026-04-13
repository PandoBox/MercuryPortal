package com.mercury.messengerportal.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * One-shot location fetch using FusedLocationProviderClient.
 * Caller is responsible for verifying location permissions before calling [getCurrentLocation].
 */
object LocationHelper {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location {
        // [Pilot-config] Return spoofed location when enabled — remove before prod.
        if (PilotConfig.FAKE_LOCATION_ENABLED) return PilotConfig.FAKE_LOCATION

        return suspendCancellableCoroutine { cont ->
            val client = LocationServices.getFusedLocationProviderClient(context)
            val task = client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)

            task.addOnSuccessListener { location ->
                if (location != null) {
                    cont.resume(location)
                } else {
                    // Fallback to last known location
                    client.lastLocation.addOnSuccessListener { last ->
                        if (last != null) cont.resume(last)
                        else cont.resumeWithException(
                            IllegalStateException("Location unavailable — ensure GPS is enabled")
                        )
                    }.addOnFailureListener { cont.resumeWithException(it) }
                }
            }
            task.addOnFailureListener { cont.resumeWithException(it) }
        }
    }

    /**
     * Reverse geocodes [lat]/[lng] into a human-readable address using the Android Geocoder
     * (backed by Google on Play Services devices).
     *
     * Uses the async listener API on Android 33+ and falls back to the blocking call on older
     * devices. Returns null if the Geocoder is unavailable or no address is found.
     */
    suspend fun reverseGeocode(context: Context, lat: Double, lng: Double): String? {
        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(context)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // API 33+: non-blocking listener
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocation(lat, lng, 1) { addresses ->
                    cont.resume(addresses.firstOrNull()?.toDisplayString())
                }
            }
        } else {
            @Suppress("DEPRECATION")
            runCatching {
                geocoder.getFromLocation(lat, lng, 1)?.firstOrNull()?.toDisplayString()
            }.getOrNull()
        }
    }
}

/** Formats a lat/lng pair for display: e.g. "13.7263°N, 100.5221°E" */
fun formatLatLng(lat: Double, lng: Double): String {
    val latDir = if (lat >= 0) "N" else "S"
    val lngDir = if (lng >= 0) "E" else "W"
    return "%.4f°%s, %.4f°%s".format(Math.abs(lat), latDir, Math.abs(lng), lngDir)
}

/** Returns a Google Maps deep-link URI for navigation to a destination */
fun googleMapsNavUri(lat: Double, lng: Double, label: String): android.net.Uri =
    android.net.Uri.parse("google.navigation:q=$lat,$lng&mode=d&label=${label.take(50)}")

/**
 * Formats an [android.location.Address] into a concise, single-line display string.
 * Builds: "thoroughfare, locality postalCode, countryName"
 */
private fun android.location.Address.toDisplayString(): String {
    val parts = listOfNotNull(
        thoroughfare,                                     // street
        listOfNotNull(locality, postalCode).joinToString(" ").ifBlank { null }, // city + ZIP
        countryName
    )
    return parts.joinToString(", ").ifBlank { getAddressLine(0) ?: "" }
}
