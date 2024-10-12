package com.example.CentralLAApp.config.auth

import com.example.CentralLAApp.enums.UserRole

data class RegisterRequest(
    val username: String,
    val password: String,
    val role: UserRole
) {

    companion object {
        fun builder() = Builder()
    }

    data class Builder(
        var username: String = "",
        var password: String = "",
        var role: UserRole = UserRole.STUDENT
    ) {
        fun username(username: String) = apply { this.username = username }
        fun password(password: String) = apply { this.password = password }
        fun role(role: UserRole) = apply { this.role = role }

        fun build() = RegisterRequest(username, password, role)
    }
}


