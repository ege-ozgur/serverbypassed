package com.example.CentralLAApp.repository

import com.example.CentralLAApp.entity.transcript.Transcript
import com.example.CentralLAApp.entity.user.Student
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface TranscriptRepository: JpaRepository<Transcript, Long> {

    fun findByStudent(student: Student): List<Transcript>

    fun findByStudentAndTerm(student:Student, term: String): List<Transcript>

    fun existsByStudentIdAndTerm(studentId: String, term: String): Boolean


    @Query(
        """
            SELECT t
            from Transcript t
            where t.student.userID = :userID
        """
    )
    fun findByStudentId(userID: Int): List<Transcript>


}