package com.example.CentralLAApp.dto.response

import com.example.CentralLAApp.enums.LetterGrade

data class CourseGradesDTOResponse(
    val id: Long,
    val course: CourseDTOResponse,
    val grade: String?,
    val isInprogressAllowed: Boolean,
    val isNotTakenAllowed: Boolean
) {
}