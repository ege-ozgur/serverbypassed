package com.example.CentralLAApp.repository

import com.example.CentralLAApp.entity.application.ApplicationCourseGrade
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface ApplicationCourseGradeRepository : JpaRepository<ApplicationCourseGrade,Long> {

    @Query("select a from ApplicationCourseGrade a where a.course.courseCode = :courseCode")
    fun findByCourseCode(courseCode: String) : Optional<ApplicationCourseGrade>
}