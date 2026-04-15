package com.mercury.messengerportal.data.local.dao

import androidx.room.*
import com.mercury.messengerportal.data.local.entity.DayLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DayLogDao {

    @Query("SELECT * FROM day_logs WHERE messengerId = :messengerId AND date = :date LIMIT 1")
    fun observeTodayLog(messengerId: String, date: String): Flow<DayLogEntity?>

    @Query("SELECT * FROM day_logs WHERE messengerId = :messengerId AND date = :date LIMIT 1")
    suspend fun getTodayLog(messengerId: String, date: String): DayLogEntity?

    @Query("SELECT * FROM day_logs WHERE messengerId = :messengerId ORDER BY date DESC LIMIT :limit")
    fun observeRecentLogs(messengerId: String, limit: Int = 7): Flow<List<DayLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dayLog: DayLogEntity)

    @Query("""
        UPDATE day_logs
        SET clockOutTime = :clockOutTime,
            clockOutLat = :lat,
            clockOutLng = :lng,
            clockOutAddress = :address,
            isClosed = 1,
            totalJobsCompleted = :jobsCompleted
        WHERE messengerId = :messengerId AND date = :date
    """)
    suspend fun closeDay(
        messengerId: String,
        date: String,
        clockOutTime: Long,
        lat: Double,
        lng: Double,
        address: String?,
        jobsCompleted: Int
    )
    @Query("DELETE FROM day_logs WHERE messengerId = :messengerId AND date = :date")
    suspend fun deleteTodayLog(messengerId: String, date: String)
}
