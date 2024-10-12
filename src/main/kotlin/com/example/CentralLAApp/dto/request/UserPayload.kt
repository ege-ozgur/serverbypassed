package com.example.CentralLAApp.dto.request

data class UserPayload(
    val authenticationSuccess: AuthenticationSuccess
)

data class AuthenticationSuccess(
    val user: String,
    val attributes: UserAttributes
)

data class UserAttributes(
    val clientIpAddress: String,
    val isFromNewLogin: String,
    val mail: String,
    val authenticationDate: String,
    val sAMAccountName: String,
    val displayName: String,
    val ou: List<String>,
    val givenName: String,
    val successfulAuthenticationHandlers: String,
    val userAgent: String,
    val cn: String,
    val deniedServices2: String,
    val credentialType: String,
    val samlAuthenticationStatementAuthMethod: String,
    val UDC_IDENTIFIER: String,
    val authenticationMethod: String,
    val serverIpAddress: String,
    val longTermAuthenticationRequestTokenUsed: String,
    val sn: String,
    val userPrincipalName: String
)


