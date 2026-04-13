package com.mercury.messengerportal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mercury.messengerportal.domain.model.DayLog

@Entity(tableName = "day_logs")
data class DayLogEntity(
    @PrimaryKey val id: String,
    val date: String,               // yyyy-MM-dd
    val messengerId: String,
    val clockInTime: Long,
    val clockInLat: Double,
    val clockInLng: Double,
    val clockInAddress: String? = null,   // Google reverse geocoded address
    val clockOutTime: Long? = null,
    val clockOutLat: Double? = null,
    val clockOutLng: Double? = null,
    val clockOutAddress: String? = null,  // Google reverse geocoded address
    val isClosed: Boolean = false,
    val totalJobsCompleted: Int = 0
) {
    fun toDomain() = DayLog(
        id = id,
        date = date,
        messengerId = messengerId,
        clockInTime = clockInTime,
        clockInLat = clockInLat,
        clockInLng = clockInLng,
        clockInAddress = clockInAddress,
        clockOutTime = clockOutTime,
        clockOutLat = clockOutLat,
        clockOutLng = clockOutLng,
        clockOutAddress = clockOutAddress,
        isClosed = isClosed,
        totalJobsCompleted = totalJobsCompleted
    )

    companion object {
        fun fromDomain(log: DayLog) = DayLogEntity(
            id = log.id,
            date = log.date,
            messengerId = log.messengerId,
            clockInTime = log.clockInTime,
            clockInLat = log.clockInLat,
            clockInLng = log.clockInLng,
            clockInAddress = log.clockInAddress,
            clockOutTime = log.clockOutTime,
            clockOutLat = log.clockOutLat,
            clockOutLng = log.clockOutLng,
            clockOutAddress = log.clockOutAddress,
            isClosed = log.isClosed,
            totalJobsCompleted = log.totalJobsCompleted
        )
    }
}
