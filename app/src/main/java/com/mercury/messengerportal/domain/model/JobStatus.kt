package com.mercury.messengerportal.domain.model

enum class JobStatus {
    ASSIGNED,
    DEPARTED,
    ARRIVED,
    COMPLETED,
    DELAYED;

    fun displayName(): String = when (this) {
        ASSIGNED -> "Assigned"
        DEPARTED -> "Departed"
        ARRIVED  -> "Arrived"
        COMPLETED-> "Completed"
        DELAYED  -> "Delayed"
    }

    /** The next logical status the messenger can advance to, or null if terminal / blocked */
    fun nextAction(): JobStatus? = when (this) {
        ASSIGNED -> DEPARTED
        DEPARTED -> ARRIVED
        ARRIVED  -> COMPLETED
        COMPLETED-> null
        DELAYED  -> null
    }

    fun actionLabel(): String? = when (this) {
        DEPARTED -> "Depart"
        ARRIVED  -> "Arrive Location"
        COMPLETED-> "Complete Job"
        else     -> null
    }

    /** True if this status transition requires a photo */
    fun requiresPhoto(): Boolean = this == ARRIVED || this == COMPLETED

    fun isTerminal(): Boolean = this == COMPLETED || this == DELAYED
}


// ─────────────────────────────────────────────────────────────────────────────
// Service Request Type — maps directly to the 4 real types in the sample data
// ─────────────────────────────────────────────────────────────────────────────

enum class ServiceRequestType {
    SEND_DOCUMENT,          // ส่งเอกสาร (235 jobs — most common)
    PICKUP_DOCUMENT,        // รับเอกสาร (210 jobs)
    FINANCIAL_DOCUMENT,     // ดำเนินการรับ-ส่งเอกสารทางการเงิน (145 jobs)
    SEND_AND_WAIT_RETURN,   // ส่งเอกสารและรอรับกลับ (137 jobs)
    OTHER;

    fun displayName(): String = when (this) {
        SEND_DOCUMENT        -> "Send Document"
        PICKUP_DOCUMENT      -> "Pickup Document"
        FINANCIAL_DOCUMENT   -> "Financial Document"
        SEND_AND_WAIT_RETURN -> "Send & Wait Return"
        OTHER                -> "Other"
    }

    fun thaiName(): String = when (this) {
        SEND_DOCUMENT        -> "ส่งเอกสาร"
        PICKUP_DOCUMENT      -> "รับเอกสาร"
        FINANCIAL_DOCUMENT   -> "รับ-ส่งเอกสารทางการเงิน"
        SEND_AND_WAIT_RETURN -> "ส่งและรอรับกลับ"
        OTHER                -> "อื่นๆ"
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Delivery Session — Morning / Afternoon / Urgent (with optional specifyTime)
// ─────────────────────────────────────────────────────────────────────────────

enum class DeliverySession {
    MORNING,    // Morning / รอบเช้า  (~71% of jobs)
    AFTERNOON,  // Afternoon / รอบบ่าย (~27% of jobs)
    URGENT;     // เอกสารด่วน — has a specifyTime deadline (~2% of jobs)

    fun displayName(): String = when (this) {
        MORNING   -> "Morning"
        AFTERNOON -> "Afternoon"
        URGENT    -> "Urgent"
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// JobType — kept for backwards compatibility; prefer ServiceRequestType for new code
// ─────────────────────────────────────────────────────────────────────────────

enum class JobType {
    DOCUMENT_DELIVERY,
    CHEQUE_CLEARING,
    PARCEL_PICKUP,
    PARCEL_DELIVERY,
    BANK_DEPOSIT,
    GOVERNMENT_DOCUMENT,
    OTHER;

    fun displayName(): String = when (this) {
        DOCUMENT_DELIVERY  -> "Document Delivery"
        CHEQUE_CLEARING    -> "Cheque Clearing"
        PARCEL_PICKUP      -> "Parcel Pickup"
        PARCEL_DELIVERY    -> "Parcel Delivery"
        BANK_DEPOSIT       -> "Bank Deposit"
        GOVERNMENT_DOCUMENT -> "Government Document"
        OTHER              -> "Other"
    }
}
