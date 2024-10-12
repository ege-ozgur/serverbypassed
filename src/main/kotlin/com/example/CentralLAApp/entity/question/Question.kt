package com.example.CentralLAApp.entity.question

import com.example.CentralLAApp.entity.application.Application
import com.example.CentralLAApp.entity.transcript.Transcript
import com.example.CentralLAApp.enums.QuestionType
import jakarta.persistence.*
import jakarta.validation.constraints.Size


@Entity
data class Question(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val questionId: Long = 0,

    @Enumerated(EnumType.STRING)
    val type : QuestionType = QuestionType.TEXT,

    val question: String,

    @ElementCollection
    @Size(max = 15)
    val choices: MutableList<Choice>? = mutableListOf(),

    var allowMultipleAnswers: Boolean,

    val isConditionalQuestion: Boolean,

    @ElementCollection
    val depends: List<QAndC>? = null,

)

@Embeddable
data class QAndC(
    val dependsOnQuestion: Long,
    val dependsOnChoice: Int
) {

}

