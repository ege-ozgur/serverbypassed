package com.example.CentralLAApp.dto.request

data class NotificationsChangeRequest(
    val notificationChanges: List<NotificationItemChangeRequest>
)

data class NotificationItemChangeRequest(
    val notificationId: Long,
    val read: Boolean
)


