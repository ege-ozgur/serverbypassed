package com.example.CentralLAApp.dto.request

import com.example.CentralLAApp.enums.UserRole

data class UserDTORequest(
    val email: String,
    val name: String,
    val surname: String,
    val graduationType: String?,
    val role: UserRole,

)
