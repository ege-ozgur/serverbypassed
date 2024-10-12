package com.example.CentralLAApp.repository

import com.example.CentralLAApp.entity.application.Application
import com.example.CentralLAApp.entity.user.Instructor
import com.example.CentralLAApp.entity.user.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ApplicationRepository: JpaRepository<Application,Long> {


    @Query("""
    SELECT COUNT(a) > 0 
    FROM Application a 
    JOIN a.authorizedInstructors i 
    WHERE a.course.courseCode = :courseCode
        AND i.userID = :instructorId
        AND a.term = :term
        AND (a.section = :section OR (a.section IS NULL AND :section IS NULL))
    ORDER BY a.createdAt DESC
""")
    fun existsByCourseCodeAndInstructorId(courseCode: String, instructorId: Int, term: String, section: String?): Boolean

    @Query("SELECT a FROM Application a where a.course.courseCode = :searchKey ORDER BY a.createdAt DESC")
    fun getByCourseCode(searchKey: String) : Collection<Application>

    fun getApplicationsByAuthorizedInstructorsOrderByCreatedAtDesc(ins: Instructor, pageable: Pageable):Page<Application>

    fun getApplicationsByFollowersContains(user: User) : Collection<Application>

    @Query("SELECT a FROM Application a WHERE a.applicationId NOT IN (SELECT ar.application.applicationId FROM ApplicationRequest ar WHERE ar.student.userID = :userId)")
    fun findAllExcludingApplicants(pageable: Pageable, userId: Int): Page<Application>




}