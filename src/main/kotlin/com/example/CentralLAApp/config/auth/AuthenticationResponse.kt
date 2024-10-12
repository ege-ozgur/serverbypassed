package com.example.CentralLAApp.config.auth

import com.example.CentralLAApp.dto.response.UserDTO


data class AuthenticationResponse (
    val token: String,
    val user: UserDTO? = null,
    val id: Int = 0,
) {
    companion object {
        fun builder() = Builder()
    }

    class Builder(
        private var token: String = "",
        var id : Int = 0,
        var user: UserDTO? = null
    ) {
        fun token(token: String) = apply { this.token = token }

        fun id(id:Int) = apply {this.id = id}

        fun user(userPayload: UserDTO) = apply { this.user = userPayload }
        fun build() = AuthenticationResponse(token,user,id)
    }
}
