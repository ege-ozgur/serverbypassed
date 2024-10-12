package com.example.CentralLAApp.dto

import com.example.CentralLAApp.dto.request.NotificationRequest
import com.example.CentralLAApp.entity.Notification
import com.example.CentralLAApp.entity.user.User
import java.time.LocalDateTime

fun NotificationRequest.toNotification(user: User, now: LocalDateTime) = Notification(
    user = user,
    title = title,
    description = description,
    timestamp = now,
    notificationType = notificationType,
    relation = relation,
    applicationId = applicationId,
)