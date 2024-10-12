package com.example.CentralLAApp.dto

import com.example.CentralLAApp.dto.response.NotificationResponse
import com.example.CentralLAApp.entity.Notification

fun Notification.toNotificationResponse(email: String) = NotificationResponse(
    id = notificationId,
    username = email,
    title = title,
    description = description,
    timestamp = timestamp.toString(),
    read = readStatus,
    notificationType = notificationType,
    relation = relation,
    applicationId = applicationId,
    applicationRequestId = applicationRequestId
)