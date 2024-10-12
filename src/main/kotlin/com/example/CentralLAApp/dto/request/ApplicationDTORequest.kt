package com.example.CentralLAApp.dto.request

import com.example.CentralLAApp.enums.LetterGrade
import java.time.Duration

class ApplicationDTORequest(
    val courseCode: String,
    val minimumRequiredGrade: LetterGrade? = null,
    val questions: List<QuestionDTORequest>,
    val term: String,
    val lastApplicationDate: String,
    val authorizedInstructors: List<Int>,
    val weeklyWorkHours: Duration,
    val previousCourseGrades: MutableList<CourseGradesDTORequest>,
    val jobDetails: String? = null,
    val isInprogressAllowed: Boolean? = null,
    val isNotTakenAllowed: Boolean? = null,
    val section: String? = null
)