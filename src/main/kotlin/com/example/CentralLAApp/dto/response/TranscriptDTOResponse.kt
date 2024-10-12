package com.example.CentralLAApp.dto.response

import com.example.CentralLAApp.enums.LetterGrade

data class TranscriptDTOResponse (
    val transcriptId: Long = 0,
    val studentId: Int = 0,
    val studentSuId: String = "",
    val term: String = "",
    val year: String = "",
    val studentName: String = "",
    val program: ProgramsResponse,
    val course: List<CoursesAndGradesResponse> ,
    val faculty: String = "",
    val cumulativeGPA: String = "",
    val cumulativeCredits: String = "",
    val photoUrl: String
)


data class CoursesAndGradesResponse (
    val courseCode: String,
    val grade: LetterGrade,
    val term: String
)


data class ProgramsResponse(
    val majors: MutableList<Any> = mutableListOf(),
    val minors: MutableList<Any> = mutableListOf()
)