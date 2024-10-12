package com.example.CentralLAApp.repository

import com.example.CentralLAApp.entity.course.Course
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface CourseRepository : JpaRepository<Course,Int> {

    fun findByCourseCode(courseCode: String) : Optional<Course>

    fun existsByCourseCode(courseCode: String) : Boolean


    @Transactional
    fun deleteByCourseCode( courseCode: String)

}