package com.example.CentralLAApp.dto.request

import com.example.CentralLAApp.enums.QuestionType

data class QuestionDTORequest(
    val question: String,
    val type : QuestionType,
    val choices: MutableList<ChoiceDTO>? = mutableListOf(),
    val allowMultipleAnswers: Boolean,
    val isConditionalQuestion: Boolean?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuestionDTORequest

        if (question != other.question) return false
        if (type != other.type) return false
        if (choices != other.choices) return false
        if (allowMultipleAnswers != other.allowMultipleAnswers) return false
        if (isConditionalQuestion != other.isConditionalQuestion) return false

        return true
    }

    override fun hashCode(): Int {
        var result = question.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + (choices?.hashCode() ?: 0)
        result = 31 * result + allowMultipleAnswers.hashCode()
        result = 31 * result + (isConditionalQuestion?.hashCode() ?: 0)
        return result
    }
}

data class ChoiceDTO(
    val choice: String,
    val conditionallyOpen: String
)

