package com.example.CentralLAApp.dto.response

import com.example.CentralLAApp.enums.ApplicationResult
import java.time.Duration

class ApplicationRequestDTOForApplicationResponse (
    val applicationRequestId: Long,
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
    //val application: ApplicationDTOResponse?
)