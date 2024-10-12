package com.example.CentralLAApp.repository

import com.example.CentralLAApp.entity.user.Instructor
import com.example.CentralLAApp.entity.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional


@Repository
interface InstructorRepository : JpaRepository<Instructor,Int> {






}