package com.mercury.messengerportal.data.local.dao

import androidx.room.*
import com.mercury.messengerportal.data.local.entity.JobStatusLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JobStatusLogDao {

    @Query("SELECT * FROM job_status_logs WHERE jobId = :jobId ORDER BY deviceTimestamp ASC")
    fun observeLogsForJob(jobId: String): Flow<List<JobStatusLogEntity>>

    @Query("SELECT * FROM job_status_logs WHERE isOfflineQueued = 1 AND syncedAt IS NULL")
    suspend fun getUnsynced(): List<JobStatusLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: JobStatusLogEntity)

    @Query("UPDATE job_status_logs SET syncedAt = :syncedAt, serverTimestamp = :serverTimestamp WHERE id = :logId")
    suspend fun markSynced(logId: String, syncedAt: Long, serverTimestamp: Long)

    /** Returns all logs with GPS for the day closing trail (including HQ events), ordered chronologically */
    @Query("""
        SELECT jsl.* FROM job_status_logs jsl
        LEFT JOIN jobs j ON jsl.jobId = j.id
        WHERE (j.messengerId = :messengerId OR jsl.jobId = 'HQ_EVENT')
          AND jsl.latitude IS NOT NULL
        ORDER BY jsl.deviceTimestamp ASC
    """)
    suspend fun getGpsLogsForMessenger(messengerId: String): List<JobStatusLogEntity>

    @Query("""
        DELETE FROM job_status_logs
        WHERE jobId IN (SELECT id FROM jobs WHERE messengerId = :messengerId)
           OR jobId = 'HQ_EVENT'
    """)
    suspend fun clearLogsForMessenger(messengerId: String)

    @Query("""
        SELECT COUNT(*) > 0 FROM job_status_logs jsl
        WHERE jsl.jobId = 'HQ_EVENT'
          AND jsl.status = :status
          AND DATE(jsl.deviceTimestamp / 1000, 'unixepoch') = :today
    """)
    suspend fun checkHqEventExists(status: String, today: String): Boolean
}
