package com.example.CentralLAApp.exception.securityExceptions
data class CustomJsonFormatResponse(
    val error: String,
    val message: String,
    val properJsonFormat: Any
)