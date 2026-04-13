package com.mercury.messengerportal.domain.model

data class Messenger(
    val id: String,
    val name: String,
    val employeeId: String,
    val phone: String,
    val fcmToken: String? = null
)
