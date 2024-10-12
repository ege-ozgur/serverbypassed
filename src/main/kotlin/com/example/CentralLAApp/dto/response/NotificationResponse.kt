package com.example.CentralLAApp.dto.response

import com.example.CentralLAApp.enums.NotificationRelationType
import com.example.CentralLAApp.enums.NotificationType
import kotlinx.serialization.Serializable
import java.time.LocalDateTime


@Serializable
data class NotificationResponse(
    val id: Long?,
    val username: String,
    val title: String,
    val description: String,
    val timestamp: String,
    val read: Boolean,
    val notificationType: NotificationType,
    val relation: NotificationRelationType,
    val applicationId: Long?,
    val applicationRequestId: Long?
)

@Serializable
data class PublicNotificationResponse(
    val title: String,
    val description: String,
    val timestamp: String,
    val notificationType: NotificationType,
    val relation: NotificationRelationType
)