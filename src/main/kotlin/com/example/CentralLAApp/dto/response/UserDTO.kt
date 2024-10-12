package com.example.CentralLAApp.dto.response

import com.example.CentralLAApp.entity.NotificationPreference
import com.example.CentralLAApp.enums.UserRole

data class UserDTO(
    val id: Int,
    val email: String,
    val name: String,
    val surname: String,
    val graduationType: String?,
    val role: UserRole,
    val notificationPreference: NotificationPreference? = null,
    var photoUrl: String? = null,
    val universityId: String
)
