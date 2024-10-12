package com.example.CentralLAApp.config.auth

data class AuthenticationRequest (
    val ticket: String,
    val serviceUrl: String
) {
    companion object {
        fun builder() = Builder()
    }

    data class Builder(
        var ticket: String = "",
        var serviceUrl: String = ""
    ) {

        fun ticket(ticket: String) = apply { this.ticket = ticket }

        fun serviceUrl(serviceUrl: String) = apply { this.serviceUrl = serviceUrl }

        fun build() = AuthenticationRequest(ticket, serviceUrl)
    }
}
