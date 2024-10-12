package com.example.CentralLAApp.dto.request

import com.example.CentralLAApp.enums.LetterGrade

data class CourseGradesDTORequest(
    val courseCode: String,
    val grade: LetterGrade? = null,
    val isInprogressAllowed: Boolean,
    val isNotTakenAllowed: Boolean
) {
}