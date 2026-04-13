package com.mercury.messengerportal.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mercury.messengerportal.domain.model.JobStatus
import com.mercury.messengerportal.domain.model.JobStatusLog

@Entity(
    tableName = "job_status_logs",
    indices = [Index("jobId")]
)
data class JobStatusLogEntity(
    @PrimaryKey val id: String,
    val jobId: String,
    val status: String,             // JobStatus name
    val latitude: Double?,
    val longitude: Double?,
    val locationAddress: String?,   // human-readable address from Geocoder; null if unavailable
    val photoUrl: String?,
    val deviceTimestamp: Long,      // epoch ms — captured at action tap (offline-safe)
    val serverTimestamp: Long?,     // epoch ms — from server response; null until synced
    val isOfflineQueued: Boolean,   // true = created without network
    val syncedAt: Long?             // epoch ms — set by SyncWorker on success
) {
    fun toDomain() = JobStatusLog(
        id = id,
        jobId = jobId,
        status = JobStatus.valueOf(status),
        latitude = latitude,
        longitude = longitude,
        locationAddress = locationAddress,
        photoUrl = photoUrl,
        deviceTimestamp = deviceTimestamp,
        serverTimestamp = serverTimestamp,
        isOfflineQueued = isOfflineQueued,
        syncedAt = syncedAt
    )

    companion object {
        fun fromDomain(log: JobStatusLog) = JobStatusLogEntity(
            id = log.id,
            jobId = log.jobId,
            status = log.status.name,
            latitude = log.latitude,
            longitude = log.longitude,
            locationAddress = log.locationAddress,
            photoUrl = log.photoUrl,
            deviceTimestamp = log.deviceTimestamp,
            serverTimestamp = log.serverTimestamp,
            isOfflineQueued = log.isOfflineQueued,
            syncedAt = log.syncedAt
        )
    }
}

