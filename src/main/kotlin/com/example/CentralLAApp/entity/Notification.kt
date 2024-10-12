package com.example.CentralLAApp.entity

import com.example.CentralLAApp.entity.user.User
import com.example.CentralLAApp.enums.NotificationRelationType
import com.example.CentralLAApp.enums.NotificationType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PreRemove
import java.time.LocalDateTime


@Entity
class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val notificationId: Long = 0L,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: User,

    val title: String,
    val description: String,
    val notificationType: NotificationType = NotificationType.DEFAULT,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val relation: NotificationRelationType = NotificationRelationType.DIRECT,
    var readStatus: Boolean = false,
    val applicationId: Long? = null,
    val applicationRequestId: Long? = null
) {

}
