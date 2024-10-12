package com.example.CentralLAApp.entity.transcript

import com.example.CentralLAApp.entity.user.Student
import com.example.CentralLAApp.enums.LetterGrade
import jakarta.persistence.*


@Entity
data class CoursesAndGrades(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "transcript_id")
    val transcript: Transcript,

    @Column(name = "courseCode")
    val courseCode: String,

    @Enumerated(EnumType.STRING)
    val grade: LetterGrade,

    val term: String
){
    override fun toString(): String {
        return "CoursesAndGrades(courseCode='$courseCode', grade=$grade)"
    }
}