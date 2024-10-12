package com.example.CentralLAApp.dto.request

import com.example.CentralLAApp.enums.ApplicationResult

data class MultipleStatusDTORequest(
    val appReqId: Long,
    val status: ApplicationResult
)
