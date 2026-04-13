package com.mercury.messengerportal.domain.model

data class Job(
    val id: String,
    val title: String,
    val type: JobType,
    // ── New fields from real sample data ──────────────────────────────────
    val serviceRequest: ServiceRequestType = ServiceRequestType.SEND_DOCUMENT,
    val deliverySession: DeliverySession = DeliverySession.MORNING,
    val specifyTime: String? = null,        // e.g. "14:10" — set only for URGENT session
    val refNo: String = "",                 // e.g. "2025060005" (dispatcher reference ID)
    val requesterDepartment: String = "",   // e.g. "finance", "Legal Dept" — for pickup verification
    // ─────────────────────────────────────────────────────────────────────
    val senderName: String,
    val senderPhone: String,
    val receiverName: String,
    val receiverPhone: String,
    val locationName: String,
    val locationAddress: String,
    val latitude: Double,
    val longitude: Double,
    val sequenceOrder: Int,
    val status: JobStatus,
    val assignedAt: Long,
    val notes: String = "",
    val messengerRemark: String = "",
    val reassignNote: String = "",
    val messengerId: String
)
