package com.mercury.messengerportal.data.repository

import android.content.Context
import android.util.Log
import com.mercury.messengerportal.data.dummy.dummyJobs
import com.mercury.messengerportal.data.local.dao.DayLogDao
import com.mercury.messengerportal.data.local.dao.JobDao
import com.mercury.messengerportal.data.local.dao.JobStatusLogDao
import com.mercury.messengerportal.data.local.entity.DayLogEntity
import com.mercury.messengerportal.data.local.entity.JobStatusLogEntity
import com.mercury.messengerportal.data.remote.ApiService
import com.mercury.messengerportal.domain.model.DayLog
import com.mercury.messengerportal.domain.model.GpsEvent
import com.mercury.messengerportal.domain.model.Job
import com.mercury.messengerportal.domain.model.JobStatus
import com.mercury.messengerportal.domain.model.JobStatusLog
import com.mercury.messengerportal.util.LocationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "JobRepository"

@Singleton
class JobRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val jobDao: JobDao,
    private val statusLogDao: JobStatusLogDao,
    private val dayLogDao: DayLogDao,
    private val apiService: ApiService
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // ── Jobs ──────────────────────────────────────────────────────────────────

    fun observeJobs(messengerId: String): Flow<List<Job>> =
        jobDao.observeJobsForMessenger(messengerId).map { list -> list.map { it.toDomain() } }

    fun observeJob(jobId: String): Flow<Job?> =
        jobDao.observeJob(jobId).map { it?.toDomain() }

    /**
     * Seed dummy jobs if the DB is empty for this messenger.
     * Only inserts if no jobs exist — does not reset existing jobs.
     */
    suspend fun seedDummyJobsIfEmpty(messengerId: String) {
        val existing = jobDao.observeJobsForMessenger(messengerId).first()
        if (existing.isEmpty()) {
            jobDao.insertAll(dummyJobs(messengerId))
        }
    }

    suspend fun reorderJob(jobId: String, newOrder: Int) {
        jobDao.updateSequenceOrder(jobId, newOrder)
        // TODO: sync reorder to server
    }

    /**
     * [Pilot/Dev] Resets today's progress for testing:
     * 1. Deletes today's clock-in/out log.
     * 2. Deletes all status logs (activity history).
     * 3. Resets all job statuses back to ASSIGNED.
     * 4. Clears all delay reasons (messenger remarks).
     */
    suspend fun resetTodayProgress(messengerId: String) {
        val today = dateFormat.format(Date())
        dayLogDao.deleteTodayLog(messengerId, today)
        statusLogDao.clearLogsForMessenger(messengerId)

        // Reset all jobs to ASSIGNED status and clear delay reasons
        val jobs = jobDao.observeJobsForMessenger(messengerId).first()
        for (job in jobs) {
            jobDao.updateStatus(job.id, JobStatus.ASSIGNED.name)
            // Clear delay reason (messenger remark)
            jobDao.updateMessengerRemark(job.id, "")
        }
    }

    suspend fun requestReassign(jobId: String, note: String) {
        jobDao.requestReassign(jobId, note)
        // TODO: POST to API
    }

    suspend fun updateMessengerRemark(jobId: String, note: String) {
        jobDao.updateMessengerRemark(jobId, note)
    }

    // ── Status Updates (offline-safe) ─────────────────────────────────────────

    /**
     * Records a status transition. Writes to Room immediately (offline-safe),
     * then attempts to sync with the server. If offline, SyncWorker will retry.
     *
     * [isNetworkAvailable] should be checked by the caller before invoking.
     */
    suspend fun updateJobStatus(
        job: Job,
        newStatus: JobStatus,
        latitude: Double?,
        longitude: Double?,
        locationAddress: String?,
        photoUrl: String?,
        isNetworkAvailable: Boolean
    ): Result<Unit> {
        val now = System.currentTimeMillis()
        val logId = UUID.randomUUID().toString()

        val logEntity = JobStatusLogEntity(
            id = logId,
            jobId = job.id,
            status = newStatus.name,
            latitude = latitude,
            longitude = longitude,
            locationAddress = locationAddress,
            photoUrl = photoUrl,
            deviceTimestamp = now,
            serverTimestamp = null,
            isOfflineQueued = !isNetworkAvailable,
            syncedAt = null
        )

        // 1. Write to Room immediately (offline-safe)
        statusLogDao.insert(logEntity)
        jobDao.updateStatus(job.id, newStatus.name)

        // 2. Attempt live sync if online
        if (isNetworkAvailable) {
            trySyncStatusLog(logEntity)
        } else {
            Log.d(TAG, "Offline — queued status ${newStatus.name} for job ${job.id} at $now")
        }

        return Result.success(Unit)
    }

    /** Called by SyncWorker to flush offline queue */
    suspend fun syncPendingLogs() {
        val unsynced = statusLogDao.getUnsynced()
        Log.d(TAG, "Syncing ${unsynced.size} offline status log(s)")
        unsynced.forEach { trySyncStatusLog(it) }
    }

    private suspend fun trySyncStatusLog(log: JobStatusLogEntity) {
        try {
            // TODO: Uncomment when real API is ready
            // val response = apiService.updateJobStatus(
            //     log.jobId,
            //     StatusUpdateRequest(
            //         logId = log.id,
            //         status = log.status,
            //         latitude = log.latitude,
            //         longitude = log.longitude,
            //         deviceTimestamp = log.deviceTimestamp,
            //         photoUrl = log.photoUrl
            //     )
            // )
            // if (response.isSuccessful) {
            //     val serverTs = response.body()!!.serverTimestamp
            //     statusLogDao.markSynced(log.id, System.currentTimeMillis(), serverTs)
            // }

            // DUMMY: simulate successful sync after a short delay
            kotlinx.coroutines.delay(300)
            statusLogDao.markSynced(log.id, System.currentTimeMillis(), System.currentTimeMillis())
            Log.d(TAG, "Synced log ${log.id} (status=${log.status})")
        } catch (e: Exception) {
            Log.w(TAG, "Sync failed for log ${log.id}: ${e.message}")
            // Leave isOfflineQueued=true; WorkManager will retry
        }
    }

    suspend fun insertHqEvent(jobId: String, status: String, latitude: Double, longitude: Double, address: String?) {
        // Only insert DEPART_HQ if it doesn't already exist for today
        if (status == "DEPART_HQ") {
            val today = dateFormat.format(Date())
            val existingDepart = statusLogDao.checkHqEventExists(status, today)
            if (existingDepart) {
                Log.d(TAG, "DEPART_HQ already recorded today, skipping duplicate")
                return
            }
        }

        val now = System.currentTimeMillis()
        val logEntity = JobStatusLogEntity(
            id = UUID.randomUUID().toString(),
            jobId = jobId,
            status = status,
            latitude = latitude,
            longitude = longitude,
            locationAddress = address,
            photoUrl = null,
            deviceTimestamp = now,
            serverTimestamp = null,
            isOfflineQueued = false,
            syncedAt = now
        )
        statusLogDao.insert(logEntity)
    }

    // ── Status Log Observer ───────────────────────────────────────────────────

    fun observeStatusLogs(jobId: String): Flow<List<JobStatusLog>> =
        statusLogDao.observeLogsForJob(jobId).map { list -> list.map { it.toDomain() } }

    // ── Clock In / Out ────────────────────────────────────────────────────────

    suspend fun clockIn(messengerId: String, lat: Double, lng: Double): Result<DayLog> {
        val today = dateFormat.format(Date())
        val existing = dayLogDao.getTodayLog(messengerId, today)
        if (existing != null) return Result.success(existing.toDomain())

        // New day detected — reset all jobs to ASSIGNED status
        resetJobsForNewDay(messengerId)

        // Resolve Google address — non-fatal if Geocoder is unavailable.
        val address = runCatching {
            LocationHelper.reverseGeocode(context, lat, lng)
        }.getOrNull()

        val entity = DayLogEntity(
            id = UUID.randomUUID().toString(),
            date = today,
            messengerId = messengerId,
            clockInTime = System.currentTimeMillis(),
            clockInLat = lat,
            clockInLng = lng,
            clockInAddress = address
        )
        dayLogDao.insert(entity)
        return Result.success(entity.toDomain())
    }

    /**
     * Reset all jobs for a new working day:
     * - Reset all job statuses back to ASSIGNED
     * - Clear all delay reasons (messenger remarks)
     * - Clear status logs (GPS trail)
     */
    private suspend fun resetJobsForNewDay(messengerId: String) {
        val jobs = jobDao.observeJobsForMessenger(messengerId).first()
        for (job in jobs) {
            jobDao.updateStatus(job.id, JobStatus.ASSIGNED.name)
            jobDao.updateMessengerRemark(job.id, "")
        }
        statusLogDao.clearLogsForMessenger(messengerId)
        Log.d(TAG, "Reset ${jobs.size} jobs for new working day")
    }

    suspend fun clockOut(messengerId: String, lat: Double, lng: Double): Result<Unit> {
        val today = dateFormat.format(Date())
        // Get completed job count (first emission only)
        val jobs = jobDao.observeJobsForMessenger(messengerId).first()
        val completed = jobs.count { it.status == "COMPLETED" }

        // Resolve Google address — non-fatal if Geocoder is unavailable.
        val address = runCatching {
            LocationHelper.reverseGeocode(context, lat, lng)
        }.getOrNull()

        dayLogDao.closeDay(
            messengerId = messengerId,
            date = today,
            clockOutTime = System.currentTimeMillis(),
            lat = lat,
            lng = lng,
            address = address,
            jobsCompleted = completed
        )
        Log.d(TAG, "Clock out completed successfully for $messengerId")
        return Result.success(Unit)
    }

    fun observeTodayLog(messengerId: String): Flow<DayLog?> {
        val today = dateFormat.format(Date())
        return dayLogDao.observeTodayLog(messengerId, today).map { it?.toDomain() }
    }

    /**
     * Observes recent day logs for the messenger (for performance dashboard).
     * Returns the last [limit] days (default 7) in descending order (most recent first).
     */
    fun observeRecentDayLogs(messengerId: String, limit: Int = 7): Flow<List<DayLog>> =
        dayLogDao.observeRecentLogs(messengerId, limit).map { list -> list.map { it.toDomain() } }

    // ── Day Closing GPS Trail ─────────────────────────────────────────────────

    suspend fun buildGpsTrail(messengerId: String): List<GpsEvent> {
        val today = dateFormat.format(Date())
        val dayLog = dayLogDao.getTodayLog(messengerId, today)
        val gpsLogs = statusLogDao.getGpsLogsForMessenger(messengerId)

        val events = mutableListOf<GpsEvent>()

        // Add clock-in as first event (carries Google address from DayLogEntity)
        dayLog?.let {
            events.add(
                GpsEvent(
                    timestamp = it.clockInTime,
                    latitude = it.clockInLat,
                    longitude = it.clockInLng,
                    eventLabel = "Clock In",
                    locationAddress = it.clockInAddress
                )
            )
        }

        // Add all job status GPS events (carries locationAddress persisted at action time)
        gpsLogs.forEach { log ->
            // Skip CLOCK_OUT from status logs since it's already added from DayLog below
            if (log.jobId == "HQ_EVENT" && log.status == "CLOCK_OUT") {
                return@forEach
            }

            val job = jobDao.getJob(log.jobId)
            val seq = job?.sequenceOrder ?: 0
            val label = if (log.jobId == "HQ_EVENT") {
                when (log.status) {
                    "DEPART_HQ" -> "Depart HQ"
                    "ARRIVE_HQ" -> "Arrive HQ"
                    else -> log.status
                }
            } else {
                "Job #$seq — ${JobStatus.valueOf(log.status).displayName()}"
            }
            events.add(
                GpsEvent(
                    timestamp = log.deviceTimestamp,
                    latitude = log.latitude ?: 0.0,
                    longitude = log.longitude ?: 0.0,
                    eventLabel = label,
                    locationAddress = log.locationAddress,
                    isOfflineQueued = log.isOfflineQueued,
                    syncedAt = log.syncedAt,
                    jobId = if (log.jobId == "HQ_EVENT") null else log.jobId,
                    jobSequence = if (log.jobId == "HQ_EVENT") null else seq,
                    rawStatus = log.status
                )
            )
        }

        // Add clock-out if present (carries Google address from DayLogEntity)
        dayLog?.clockOutTime?.let {
            events.add(
                GpsEvent(
                    timestamp = it,
                    latitude = dayLog.clockOutLat!!,
                    longitude = dayLog.clockOutLng!!,
                    eventLabel = "Clock Out",
                    locationAddress = dayLog.clockOutAddress
                )
            )
        }

        return events.sortedBy { it.timestamp }
    }
}

