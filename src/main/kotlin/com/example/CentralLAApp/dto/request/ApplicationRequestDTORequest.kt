package com.example.CentralLAApp.dto.request

import jakarta.persistence.Id

data class ApplicationRequestDTORequest(
    val applicationId: Long,
    val answers: MutableList<AnswerDTO>
)

data class AnswerDTO(
    val questionId: Long,
    val answer: String
)
