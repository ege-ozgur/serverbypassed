package com.example.CentralLAApp.repository

import com.example.CentralLAApp.entity.transcript.Transcript
import com.example.CentralLAApp.entity.user.Student
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface StudentRepository : JpaRepository<Student, Int> {

    @Query("select s from Student s where s.notificationPreferences.followingNewAnnouncement = true")
    fun getNewAnnouncementFollowers() : List<Student>




}