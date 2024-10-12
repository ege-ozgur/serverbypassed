package com.example.CentralLAApp.entity.application

import com.example.CentralLAApp.entity.course.Course
import com.example.CentralLAApp.enums.LetterGrade
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "application_course_grades")
data class ApplicationCourseGrade(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "course_id")
    val course: Course,

    @Enumerated(EnumType.STRING)
    val desiredLetterGrade: LetterGrade? = null,


    var isInprogressAllowed: Boolean? = null,

    var isNotTakenAllowed: Boolean? = null
)