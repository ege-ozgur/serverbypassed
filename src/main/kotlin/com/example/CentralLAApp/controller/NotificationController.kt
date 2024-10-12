package com.example.CentralLAApp.controller

import com.example.CentralLAApp.dto.request.NotificationPreferenceChangeRequest
import com.example.CentralLAApp.dto.request.NotificationsChangeRequest
import com.example.CentralLAApp.dto.response.NotificationResponse
import com.example.CentralLAApp.dto.response.UnreadNotificationCountResponse
import com.example.CentralLAApp.entity.NotificationPreference
import com.example.CentralLAApp.enums.UserRole
import com.example.CentralLAApp.service.NotificationService
import com.example.CentralLAApp.util.security.getId
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {

    @GetMapping()
    fun getNotifications(): List<NotificationResponse> {

        val userId = getId(UserRole.USER)
        return notificationService.getNotifications(userId)
    }

    @PutMapping
    fun changeNotificationStatus(@RequestBody notificationsChangeRequest: NotificationsChangeRequest){
        val userId = getId(UserRole.USER)
        return notificationService.changeNotificationStatus(userId , notificationsChangeRequest)
    }

    @GetMapping("/preferences")
    fun getNotificationPreferences() : NotificationPreference {
        val userId = getId(UserRole.USER)
        return notificationService.getNotificationPreferences(userId)
    }

    @PutMapping("/preferences")
    fun changeNotificationPreferences(@RequestBody notificationPreferenceChangeRequest: NotificationPreferenceChangeRequest) : NotificationPreference{
        val userId = getId(UserRole.USER)
        return notificationService.changeNotificationPreferences(userId , notificationPreferenceChangeRequest)
    }

    @GetMapping("/unread")
    fun getUnreadNotificationCount(): UnreadNotificationCountResponse {
        val userId = getId(UserRole.USER)
        return notificationService.getUnreadNotificationCount(userId).run {
            UnreadNotificationCountResponse(this)
        }
    }


}