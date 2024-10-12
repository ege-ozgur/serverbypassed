package com.example.CentralLAApp.repository

import com.example.CentralLAApp.entity.question.Question
import org.springframework.data.jpa.repository.JpaRepository

interface QuestionRepository : JpaRepository<Question, Long> {
}