package com.example.CentralLAApp.service.helper

import com.example.CentralLAApp.dto.request.AuthenticationSuccess
import com.example.CentralLAApp.dto.request.UserAttributes
import com.example.CentralLAApp.dto.request.UserPayload
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper


fun mapJsonNodeToUserPayload(jsonNode: JsonNode?): UserPayload? {
    try {
        if (jsonNode != null) {
            val authenticationSuccessNode = jsonNode["authenticationSuccess"]
            val userAttributesNode = authenticationSuccessNode["attributes"]

            return mapToUserPayload(authenticationSuccessNode, userAttributesNode)
        }
    } catch (e: Exception) {
        // Handle any mapping errors
    }
    return null
}

private fun mapToUserPayload(
    authenticationSuccessNode: JsonNode,
    userAttributesNode: JsonNode
): UserPayload {
    return UserPayload(
        AuthenticationSuccess(
            user = authenticationSuccessNode["user"].asText(),
            attributes = UserAttributes(
                clientIpAddress = userAttributesNode["clientIpAddress"]?.asText() ?: "",
                isFromNewLogin = userAttributesNode["isFromNewLogin"]?.asText() ?: "",
                mail = userAttributesNode["mail"].asText(),
                authenticationDate = userAttributesNode["authenticationDate"]?.asText() ?: "",
                sAMAccountName = userAttributesNode["sAMAccountName"].asText(),
                displayName = userAttributesNode["displayName"].asText(),
                ou = userAttributesNode["ou"].map { it.asText() },
                givenName = userAttributesNode["givenName"].asText(),
                successfulAuthenticationHandlers = userAttributesNode["successfulAuthenticationHandlers"]?.asText() ?: "",
                userAgent = userAttributesNode["userAgent"]?.asText() ?: "",
                cn = userAttributesNode["cn"].asText(),
                deniedServices2 = userAttributesNode["deniedServices2"]?.asText() ?: "",
                credentialType = userAttributesNode["credentialType"]?.asText() ?: "",
                samlAuthenticationStatementAuthMethod = userAttributesNode["samlAuthenticationStatementAuthMethod"]?.asText() ?: "",
                UDC_IDENTIFIER = userAttributesNode["UDC_IDENTIFIER"]?.asText() ?: "",
                authenticationMethod = userAttributesNode["authenticationMethod"]?.asText() ?: "",
                serverIpAddress = userAttributesNode["serverIpAddress"]?.asText() ?: "",
                longTermAuthenticationRequestTokenUsed = userAttributesNode["longTermAuthenticationRequestTokenUsed"]?.asText() ?: "",
                sn = userAttributesNode["sn"].asText(),
                userPrincipalName = userAttributesNode["userPrincipalName"]?.asText() ?: ""
            )
        )
    )
}
