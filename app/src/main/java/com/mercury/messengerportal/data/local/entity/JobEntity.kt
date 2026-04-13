package com.mercury.messengerportal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mercury.messengerportal.domain.model.DeliverySession
import com.mercury.messengerportal.domain.model.Job
import com.mercury.messengerportal.domain.model.JobStatus
import com.mercury.messengerportal.domain.model.JobType
import com.mercury.messengerportal.domain.model.ServiceRequestType

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: String,                       // JobType.name (legacy)
    val serviceRequest: String = ServiceRequestType.SEND_DOCUMENT.name,
    val deliverySession: String = DeliverySession.MORNING.name,
    val specifyTime: String? = null,        // e.g. "14:10" — for URGENT jobs
    val refNo: String = "",                 // dispatcher reference e.g. "2025060005"
    val requesterDepartment: String = "",   // e.g. "finance", "Legal Dept"
    val senderName: String,
    val senderPhone: String,
    val receiverName: String,
    val receiverPhone: String,
    val locationName: String,
    val locationAddress: String,
    val latitude: Double,
    val longitude: Double,
    val sequenceOrder: Int,
    val status: String,                     // JobStatus.name
    val assignedAt: Long,
    val notes: String = "",
    val messengerRemark: String = "",
    val reassignNote: String = "",
    val messengerId: String
) {
    fun toDomain() = Job(
        id = id,
        title = title,
        type = runCatching { JobType.valueOf(type) }.getOrDefault(JobType.OTHER),
        serviceRequest = runCatching { ServiceRequestType.valueOf(serviceRequest) }
            .getOrDefault(ServiceRequestType.SEND_DOCUMENT),
        deliverySession = runCatching { DeliverySession.valueOf(deliverySession) }
            .getOrDefault(DeliverySession.MORNING),
        specifyTime = specifyTime,
        refNo = refNo,
        requesterDepartment = requesterDepartment,
        senderName = senderName,
        senderPhone = senderPhone,
        receiverName = receiverName,
        receiverPhone = receiverPhone,
        locationName = locationName,
        locationAddress = locationAddress,
        latitude = latitude,
        longitude = longitude,
        sequenceOrder = sequenceOrder,
        status = runCatching { JobStatus.valueOf(status) }.getOrDefault(JobStatus.ASSIGNED),
        assignedAt = assignedAt,
        notes = notes,
        messengerRemark = messengerRemark,
        reassignNote = reassignNote,
        messengerId = messengerId
    )

    companion object {
        fun fromDomain(job: Job) = JobEntity(
            id = job.id,
            title = job.title,
            type = job.type.name,
            serviceRequest = job.serviceRequest.name,
            deliverySession = job.deliverySession.name,
            specifyTime = job.specifyTime,
            refNo = job.refNo,
            requesterDepartment = job.requesterDepartment,
            senderName = job.senderName,
            senderPhone = job.senderPhone,
            receiverName = job.receiverName,
            receiverPhone = job.receiverPhone,
            locationName = job.locationName,
            locationAddress = job.locationAddress,
            latitude = job.latitude,
            longitude = job.longitude,
            sequenceOrder = job.sequenceOrder,
            status = job.status.name,
            assignedAt = job.assignedAt,
            notes = job.notes,
            messengerRemark = job.messengerRemark,
            reassignNote = job.reassignNote,
            messengerId = job.messengerId
        )
    }
}
