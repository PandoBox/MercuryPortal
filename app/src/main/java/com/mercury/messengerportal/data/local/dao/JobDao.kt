package com.mercury.messengerportal.data.local.dao

import androidx.room.*
import com.mercury.messengerportal.data.local.entity.JobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {

    @Query("SELECT * FROM jobs WHERE messengerId = :messengerId ORDER BY sequenceOrder ASC")
    fun observeJobsForMessenger(messengerId: String): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE id = :jobId")
    fun observeJob(jobId: String): Flow<JobEntity?>

    @Query("SELECT * FROM jobs WHERE id = :jobId")
    suspend fun getJob(jobId: String): JobEntity?

    @Query("SELECT COUNT(*) FROM jobs WHERE messengerId = :messengerId")
    suspend fun countJobs(messengerId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(jobs: List<JobEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(job: JobEntity)

    @Query("UPDATE jobs SET status = :status WHERE id = :jobId")
    suspend fun updateStatus(jobId: String, status: String)

    @Query("UPDATE jobs SET messengerRemark = :note WHERE id = :jobId")
    suspend fun updateMessengerRemark(jobId: String, note: String)

    @Query("UPDATE jobs SET sequenceOrder = :order WHERE id = :jobId")
    suspend fun updateSequenceOrder(jobId: String, order: Int)

    @Query("UPDATE jobs SET reassignNote = :note, status = 'DELAYED' WHERE id = :jobId")
    suspend fun requestReassign(jobId: String, note: String)

    @Query("DELETE FROM jobs WHERE messengerId = :messengerId")
    suspend fun clearForMessenger(messengerId: String)
}
