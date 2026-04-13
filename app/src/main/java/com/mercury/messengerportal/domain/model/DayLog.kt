package com.mercury.messengerportal.domain.model

/**
 * Represents a single working day for a messenger.
 * Used for the Day Closing screen GPS trail and shift summary.
 */
data class DayLog(
    val id: String,
    val date: String,               // yyyy-MM-dd
    val messengerId: String,
    val clockInTime: Long,          // epoch ms
    val clockInLat: Double,
    val clockInLng: Double,
    val clockInAddress: String? = null,  // Google reverse geocoded address
    val clockOutTime: Long? = null, // null until day is closed
    val clockOutLat: Double? = null,
    val clockOutLng: Double? = null,
    val clockOutAddress: String? = null, // Google reverse geocoded address
    val isClosed: Boolean = false,
    val totalJobsCompleted: Int = 0
)

/**
 * A single GPS event in the day's activity trail, shown in the Day Closing log.
 */
data class GpsEvent(
    val timestamp: Long,            // epoch ms (device time)
    val latitude: Double,
    val longitude: Double,
    val eventLabel: String,         // e.g. "Clock In", "Job #2 Departed", "Job #3 Arrived"
    val locationAddress: String? = null, // Google reverse geocoded address
    val isOfflineQueued: Boolean = false,
    val syncedAt: Long? = null,
    val jobId: String? = null,
    val jobSequence: Int? = null,
    val rawStatus: String? = null
)
