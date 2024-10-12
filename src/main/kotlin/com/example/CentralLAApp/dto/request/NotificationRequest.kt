package com.example.CentralLAApp.dto.request

import com.example.CentralLAApp.entity.NotificationPreference
import com.example.CentralLAApp.enums.NotificationRelationType
import com.example.CentralLAApp.enums.NotificationType
import java.time.LocalDateTime

data class NotificationRequest(
    val userId : Int,
    val title: String,
    val description: String,
    val notificationType: NotificationType,
    val timestamp: LocalDateTime? = null,
    val preferences: NotificationPreference,
    val relation: NotificationRelationType,
    val applicationId: Long? = null,
    val applicationRequestId: Long? = null
)

data class PublicNotificationRequest(
    val title: String,
    val description: String,
    val notificationType: NotificationType,
    val timestamp: LocalDateTime? = null,
)
