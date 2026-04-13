package com.mercury.messengerportal.data.dummy

import com.mercury.messengerportal.data.local.entity.JobEntity
import com.mercury.messengerportal.domain.model.DeliverySession
import com.mercury.messengerportal.domain.model.Messenger
import com.mercury.messengerportal.domain.model.ServiceRequestType

/** Dummy messenger used for development/testing — mirrors real BTS Group messenger profile */
val DUMMY_MESSENGER = Messenger(
    id = "MSG-001",
    name = "Luffy D. Monkey",
    employeeId = "EMP-001",
    phone = "081-234-5678"
)

/**
 * Pre-loaded jobs simulating a realistic Bangkok messenger morning round.
 * Based on real job data from BTS Group Holdings PCL (sample data, Jul 2025).
 */
fun dummyJobs(messengerId: String): List<JobEntity> = listOf(

    // JOB 1
    JobEntity(
        id = "JOB-001",
        title = "ดำเนินการรับ-ส่งเอกสารทางการเงิน",
        type = "CHEQUE_CLEARING",
        serviceRequest = ServiceRequestType.FINANCIAL_DOCUMENT.name,
        deliverySession = DeliverySession.MORNING.name,
        refNo = "2025060003",
        requesterDepartment = "People Management",
        senderName = "โชษิตา ศรีโรจนรัตน์",
        senderPhone = "-",
        receiverName = "เจ้าหน้าที่ธนาคาร BBL,BAY",
        receiverPhone = "-",
        locationName = "BTSC",
        locationAddress = "BTSC",
        latitude = 13.8056,
        longitude = 100.5531,
        sequenceOrder = 1,
        status = "ASSIGNED",
        assignedAt = System.currentTimeMillis() - 3_600_000,
        notes = "ฝากเช็คBAY 1 ชุด BBL 6 ชุด",
        messengerId = messengerId
    ),

    // JOB 2
    JobEntity(
        id = "JOB-002",
        title = "ส่งเอกสาร",
        type = "DOCUMENT_DELIVERY",
        serviceRequest = ServiceRequestType.SEND_DOCUMENT.name,
        deliverySession = DeliverySession.MORNING.name,
        refNo = "2025060007",
        requesterDepartment = "BCAF-COE",
        senderName = "Nadlada Tummasud",
        senderPhone = "026177300",
        receiverName = "Wilailak Lertwichakarn",
        receiverPhone = "-",
        locationName = "BTSC",
        locationAddress = "จตุจักร",
        latitude = 13.8056,
        longitude = 100.5531,
        sequenceOrder = 2,
        status = "ASSIGNED",
        assignedAt = System.currentTimeMillis() - 3_600_000,
        notes = "ส่งเอกสารคุณหยก BTSC ชั้น 6",
        messengerId = messengerId
    ),

    // JOB 3
    JobEntity(
        id = "JOB-003",
        title = "ส่งเอกสาร ก่อนเครียริ่ง",
        type = "DOCUMENT_DELIVERY",
        serviceRequest = ServiceRequestType.SEND_DOCUMENT.name,
        deliverySession = DeliverySession.MORNING.name,
        specifyTime = "10:30",
        refNo = "2025060012",
        requesterDepartment = "Accounting",
        senderName = "ยุพาวดี วงศ์ธนนันท์",
        senderPhone = "02-123-4561",
        receiverName = "ฝากเช็ค เคาเตอร์",
        receiverPhone = "020801000",
        locationName = "ธนาคารกสิกร (ซันทาวเวอร์)",
        locationAddress = "ธนาคารกสิกร (ซันทาวเวอร์) ก่อนเครียริ่ง",
        latitude = 13.8056,
        longitude = 100.5555,
        sequenceOrder = 3,
        status = "ASSIGNED",
        assignedAt = System.currentTimeMillis() - 3_600_000,
        notes = "ก่อนเครียริ่ง",
        messengerId = messengerId
    ),

    // JOB 4
    JobEntity(
        id = "JOB-004",
        title = "ส่งเอกสาร",
        type = "DOCUMENT_DELIVERY",
        serviceRequest = ServiceRequestType.SEND_DOCUMENT.name,
        deliverySession = DeliverySession.MORNING.name,
        refNo = "2025060013",
        requesterDepartment = "Business Development",
        senderName = "Sarunya Soparkdithapong",
        senderPhone = "-",
        receiverName = "Woranuch W.",
        receiverPhone = "080310-",
        locationName = "UTA",
        locationAddress = "พหลโยธิน",
        latitude = 13.8214,
        longitude = 100.5596,
        sequenceOrder = 4,
        status = "ASSIGNED",
        assignedAt = System.currentTimeMillis() - 3_600_000,
        notes = "",
        messengerId = messengerId
    ),

    // JOB 5
    JobEntity(
        id = "JOB-005",
        title = "ส่งเอกสาร",
        type = "DOCUMENT_DELIVERY",
        serviceRequest = ServiceRequestType.SEND_DOCUMENT.name,
        deliverySession = DeliverySession.MORNING.name,
        specifyTime = "11:00",
        refNo = "2025060014",
        requesterDepartment = "Legal",
        senderName = "ณัฐรัตน์ รักทวี",
        senderPhone = "-",
        receiverName = "คุณวาว ณัฐพร",
        receiverPhone = "0856667474",
        locationName = "BTSC",
        locationAddress = "จตุจักร",
        latitude = 13.8056,
        longitude = 100.5531,
        sequenceOrder = 5,
        status = "ASSIGNED",
        assignedAt = System.currentTimeMillis() - 3_600_000,
        notes = "",
        messengerId = messengerId
    ),

    // JOB 6
    JobEntity(
        id = "JOB-006",
        title = "ส่งเอกสาร ฝากเงินสดเข้าบัญชี",
        type = "BANK_DEPOSIT",
        serviceRequest = ServiceRequestType.FINANCIAL_DOCUMENT.name,
        deliverySession = DeliverySession.AFTERNOON.name,
        refNo = "2025060025",
        requesterDepartment = "บัญชี",
        senderName = "สรัญญา วิชัยคำจร",
        senderPhone = "-",
        receiverName = "SCB-Suntower",
        receiverPhone = "-",
        locationName = "SCB-Suntower",
        locationAddress = "Suntower",
        latitude = 13.8056,
        longitude = 100.5555,
        sequenceOrder = 6,
        status = "ASSIGNED",
        assignedAt = System.currentTimeMillis() - 3_600_000,
        notes = "ฝากเงินสดเข้าบัญชี Custodian 0.16.- เซ็นรับและส่งกลับบัญชี",
        messengerId = messengerId
    ),

    // JOB 7
    JobEntity(
        id = "JOB-007",
        title = "ส่งเอกสาร",
        type = "DOCUMENT_DELIVERY",
        serviceRequest = ServiceRequestType.SEND_DOCUMENT.name,
        deliverySession = DeliverySession.AFTERNOON.name,
        refNo = "2025060029",
        requesterDepartment = "Accounting",
        senderName = "ยุพาวดี วงศ์ธนนันท์",
        senderPhone = "-",
        receiverName = "ธนาคารกรุงเทพ ตึกซัน",
        receiverPhone = "02-080-1000",
        locationName = "ธนาคารกรุงเทพ ตึกซัน",
        locationAddress = "ธนาคารกรุงเทพ ตึกซัน",
        latitude = 13.8056,
        longitude = 100.5555,
        sequenceOrder = 7,
        status = "ASSIGNED",
        assignedAt = System.currentTimeMillis() - 3_600_000,
        notes = "",
        messengerId = messengerId
    ),

    // JOB 8
    JobEntity(
        id = "JOB-008",
        title = "ส่งเอกสาร",
        type = "DOCUMENT_DELIVERY",
        serviceRequest = ServiceRequestType.SEND_DOCUMENT.name,
        deliverySession = DeliverySession.AFTERNOON.name,
        refNo = "2025060030",
        requesterDepartment = "Administration",
        senderName = "Sudthantip Suebvong",
        senderPhone = "-",
        receiverName = "Reception",
        receiverPhone = "-",
        locationName = "BTSC",
        locationAddress = "จตุจักร",
        latitude = 13.8056,
        longitude = 100.5531,
        sequenceOrder = 8,
        status = "ASSIGNED",
        assignedAt = System.currentTimeMillis() - 3_600_000,
        notes = "ฝากงานให้ทาง Reception รับและเซ็นรับใบปะหน้า",
        messengerId = messengerId
    ),

    // JOB 9
    JobEntity(
        id = "JOB-009",
        title = "ส่งเอกสาร",
        type = "DOCUMENT_DELIVERY",
        serviceRequest = ServiceRequestType.SEND_DOCUMENT.name,
        deliverySession = DeliverySession.AFTERNOON.name,
        refNo = "2025060031",
        requesterDepartment = "Accounting",
        senderName = "ยุพาวดี วงศ์ธนนันท์",
        senderPhone = "-",
        receiverName = "ธนาคารกสิกร ตึกซัน",
        receiverPhone = "02-0801000",
        locationName = "ธนาคารกสิกร (ซันทาวเวอร์)",
        locationAddress = "ธนาคารกสิกร (ซันทาวเวอร์)",
        latitude = 13.8056,
        longitude = 100.5555,
        sequenceOrder = 9,
        status = "ASSIGNED",
        assignedAt = System.currentTimeMillis() - 3_600_000,
        notes = "",
        messengerId = messengerId
    ),

    // JOB 10
    JobEntity(
        id = "JOB-010",
        title = "ส่งเอกสาร",
        type = "DOCUMENT_DELIVERY",
        serviceRequest = ServiceRequestType.SEND_DOCUMENT.name,
        deliverySession = DeliverySession.AFTERNOON.name,
        refNo = "2025060032",
        requesterDepartment = "Accounting",
        senderName = "ยุพาวดี วงศ์ธนนันท์",
        senderPhone = "-",
        receiverName = "ธนาคารไทยพาณิชย์ ตึกซัน",
        receiverPhone = "02-080-1000",
        locationName = "SCB-Suntower",
        locationAddress = "ธนาคารไทยพาณิชย์ ตึกซัน",
        latitude = 13.8056,
        longitude = 100.5555,
        sequenceOrder = 10,
        status = "ASSIGNED",
        assignedAt = System.currentTimeMillis() - 3_600_000,
        notes = "",
        messengerId = messengerId
    ),

    // JOB 11
    JobEntity(
        id = "JOB-011",
        title = "ส่งและรอรับกลับ",
        type = "GOVERNMENT_DOCUMENT",
        serviceRequest = ServiceRequestType.SEND_AND_WAIT_RETURN.name,
        deliverySession = DeliverySession.AFTERNOON.name,
        specifyTime = "13:30",
        refNo = "2025070104",
        requesterDepartment = "Legal Dept",
        senderName = "Apussara Mikolajczyk",
        senderPhone = "02-123-4562",
        receiverName = "เจ้าหน้าที่กรมสรรพากร",
        receiverPhone = "02-272-8000",
        locationName = "กรมสรรพากรพื้นที่ 7 (จตุจักร)",
        locationAddress = "ถ.พหลโยธิน แขวงจอมพล เขตจตุจักร",
        latitude = 13.8214,
        longitude = 100.5596,
        sequenceOrder = 11,
        status = "ASSIGNED",
        assignedAt = System.currentTimeMillis() - 3_600_000,
        notes = "DBD form set — requires stamp and return copy. รอรับสำเนาคืน",
        messengerId = messengerId
    ),

    // JOB 12
    JobEntity(
        id = "JOB-012",
        title = "ส่งเอกสาร",
        type = "GOVERNMENT_DOCUMENT",
        serviceRequest = ServiceRequestType.SEND_DOCUMENT.name,
        deliverySession = DeliverySession.AFTERNOON.name,
        refNo = "2025070105",
        requesterDepartment = "Office of the CEO",
        senderName = "Apussara Mikolajczyk",
        senderPhone = "02-123-4563",
        receiverName = "MOC Registration Office",
        receiverPhone = "02-547-4000",
        locationName = "Ministry of Commerce",
        locationAddress = "44/100 Nonthaburi 1 Rd",
        latitude = 13.8580,
        longitude = 100.5153,
        sequenceOrder = 12,
        status = "ASSIGNED",
        assignedAt = System.currentTimeMillis() - 3_600_000,
        notes = "ส่งอย่างเดียว ไม่ต้องรอ",
        messengerId = messengerId
    )
)
