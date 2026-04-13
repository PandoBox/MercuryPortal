package com.mercury.messengerportal.domain.model

/**
 * Immutable audit record for every status transition on a job.
 *
 * Offline handling:
 * - [deviceTimestamp] is always set at the moment the messenger taps the action (offline-safe).
 * - [isOfflineQueued] = true means the record was created without network connectivity.
 * - [syncedAt] is populated by SyncWorker when the record is successfully pushed to the server.
 * - UI displays "Recorded offline at [deviceTimestamp], synced at [syncedAt]" when isOfflineQueued = true.
 */
data class JobStatusLog(
    val id: String,
    val jobId: String,
    val status: JobStatus,
    val latitude: Double?,
    val longitude: Double?,
    val locationAddress: String?,   // human-readable address from Google reverse geocoding; null if unavailable
    val photoUrl: String?,          // null for statuses that don't require a photo
    val deviceTimestamp: Long,      // epoch ms — captured at the moment of action (offline-safe)
    val serverTimestamp: Long?,     // epoch ms — set by server on receipt; null until synced
    val isOfflineQueued: Boolean,   // true = action was taken without connectivity
    val syncedAt: Long?             // epoch ms — when WorkManager successfully synced this record
)
