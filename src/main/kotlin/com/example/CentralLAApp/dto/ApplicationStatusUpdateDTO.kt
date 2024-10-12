package com.example.CentralLAApp.dto

import com.example.CentralLAApp.enums.ApplicationStatus

data class ApplicationStatusUpdateDTO(
    val to: ApplicationStatus
) {
}