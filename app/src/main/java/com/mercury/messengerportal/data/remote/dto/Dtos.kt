package com.mercury.messengerportal.data.remote.dto

// ── Auth ─────────────────────────────────────────────────────────────────────

data class LoginRequest(val employeeId: String, val password: String)

data class LoginResponse(
    val token: String,
    val messengerId: String,
    val messengerName: String,
    val employeeId: String,
    val phone: String
)

// ── Job ───────────────────────────────────────────────────────────────────────

data class JobDto(
    val id: String,
    val title: String,
    val type: String,
    val senderName: String,
    val senderPhone: String,
    val receiverName: String,
    val receiverPhone: String,
    val locationName: String,
    val locationAddress: String,
    val latitude: Double,
    val longitude: Double,
    val sequenceOrder: Int,
    val status: String,
    val assignedAt: Long,
    val notes: String,
    val messengerId: String
)

// ── Status Update ─────────────────────────────────────────────────────────────

data class StatusUpdateRequest(
    val logId: String,
    val status: String,
    val latitude: Double?,
    val longitude: Double?,
    val deviceTimestamp: Long,
    val photoUrl: String?
)

data class StatusUpdateResponse(
    val logId: String,
    val serverTimestamp: Long
)

// ── Re-assign ─────────────────────────────────────────────────────────────────

data class ReassignRequest(val note: String)

// ── Reorder ───────────────────────────────────────────────────────────────────

data class ReorderRequest(val jobs: List<JobOrderItem>)
data class JobOrderItem(val jobId: String, val sequenceOrder: Int)

// ── Clock events ─────────────────────────────────────────────────────────────

data class ClockEventRequest(
    val messengerId: String,
    val latitude: Double,
    val longitude: Double,
    val deviceTimestamp: Long
)

data class ClockEventResponse(val serverTimestamp: Long)

// ── Photo ─────────────────────────────────────────────────────────────────────

data class PhotoUploadResponse(val photoUrl: String)
