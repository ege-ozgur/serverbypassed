package com.example.CentralLAApp.dto.response

import com.example.CentralLAApp.entity.question.Choice
import com.example.CentralLAApp.entity.question.QAndC
import com.example.CentralLAApp.enums.QuestionType
import org.springframework.context.annotation.DependsOn

data class QuestionDTOResponse(
    val questionId: Long,
    val type: QuestionType,
    val question: String,
    val choices: MutableList<Choice>?,
    val allowMultipleAnswers: Boolean,
    val isConditionalQuestion: Boolean?,
    val depends: List<QAndCDTO>?
)

data class QAndCDTO(
    val dependsOnQuestion: Long,
    val dependsOnChoice: Int
)
