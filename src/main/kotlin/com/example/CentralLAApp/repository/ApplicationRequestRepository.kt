package com.example.CentralLAApp.repository

import com.example.CentralLAApp.entity.application.ApplicationRequest
import com.example.CentralLAApp.entity.user.Student
import com.example.CentralLAApp.enums.ApplicationResult
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query


interface ApplicationRequestRepository: JpaRepository<ApplicationRequest, Long> {

    fun findByStudent(student: Student): List<ApplicationRequest>

    fun findByStudentAndStatus(student: Student, status: ApplicationResult): List<ApplicationRequest>

    fun findByStudentAndStatusOrderByCreatedAt(student: Student, status: ApplicationResult): List<ApplicationRequest>

    fun getApplicationRequestsByStudentAndStatusOrderByCreatedAtDesc(
        student: Student,
        status: ApplicationResult,
        pageable: Pageable
    ): Page<ApplicationRequest>

    fun getApplicationRequestsByStudentOrderByCreatedAtDesc(student: Student, pageable: Pageable): Page<ApplicationRequest>
    fun findAllByStatusIsNotAndStudent(status: ApplicationResult, student: Student): List<ApplicationRequest>

    fun findAllByStatusIsNotAndStatusIsNotAndStudent(status1: ApplicationResult, status2: ApplicationResult, student: Student): List<ApplicationRequest>

    @Query("""
        SELECT COUNT(*) > 0
        FROM application_instructors ai, applications ap, application_requests ar
        where ai.instructor_id = :instructorId and ai.application_id = ap.application_id and ar.application_id = ap.application_id and ar.student_id = :studentId
    """, nativeQuery = true)
    fun checkByInstructorIdAndStudentId(instructorId: Int, studentId: Int): Int


    @Query(
        """
            SELECT COUNT(*) > 0
            FROM ApplicationRequest ar
            where ar.application.applicationId = :applicationId and ar.student.userID = :userID
        """
    )
    fun checkStudentAlreadyHasApplicationRequest(userID: Int, applicationId: Long): Boolean


   @Query(
       """
           select ar
           from ApplicationRequest ar
           where ar.application.applicationId = :applicationId
       """
   )
    fun findByApplicationId(applicationId: Long): List<ApplicationRequest>


    @Query("""
        select ar
        from ApplicationRequest ar
        where ar.student.userID = :studentId
                AND (ar.application.course.courseCode = :courseCode 
                OR ar.application.term = :term
                OR ar.status = 'ACCEPTED')    
    """)
    fun getStudentLaHistory(studentId: Int, courseCode: String, term: String, pageable: Pageable): Page<ApplicationRequest>

    @Query(
        """
            select ar
            from ApplicationRequest ar
            where ar.student.userID = :userID
                AND ar.application.term = :term
                AND ar.committed = true
        """
    )
    fun findWorkingApplicationRequestsByTermAndStudentId(userID: Int, term: String) : List<ApplicationRequest>


}