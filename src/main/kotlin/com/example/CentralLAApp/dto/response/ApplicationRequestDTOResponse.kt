package com.example.CentralLAApp.dto.response

import com.example.CentralLAApp.enums.ApplicationResult
import java.time.Duration

class ApplicationRequestDTOResponse(
    val applicationRequestId: Long,
    val application: ApplicationDTOResponse?,
    val status: ApplicationResult?,
    val statusIns: ApplicationResult?,
    val student: StudentDTOResponse?,
    val qAndA: List<QuestionAndAnswer>,
    val createdAt: String?,
    val updatedAt: String?,
    val transcript: TranscriptDTOResponse?,
    val committed: Boolean,
    val forgiven: Boolean,
    var weeklyWorkHours: Duration,
    val redFlagged: Boolean
)

data class QuestionAndAnswer(val question: QuestionDTOResponse, val answer: String)
