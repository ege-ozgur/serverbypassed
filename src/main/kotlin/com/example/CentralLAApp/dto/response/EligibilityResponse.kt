package com.example.CentralLAApp.dto.response

import com.example.CentralLAApp.enums.LetterGrade


data class EligibilityResponse(
    val eligibility: List<CourseEligibilityResponse>,
    val totalCourseCount : Int,
    val eligibleCourseCount : Int,
    val notEligibleCourseCount : Int,
    val isStudentEligible: Boolean,
    val questionCount : Int
)
data class CourseEligibilityResponse(
    val courseCode: String,
    val requiredLetterGrade: LetterGrade?,
    val isInProgressAllowed: Boolean,
    val isNotTakenAllowed: Boolean,
    val studentGrade: LetterGrade?,
    val isEligible: Boolean,
    val eligibilityInfo : String
)