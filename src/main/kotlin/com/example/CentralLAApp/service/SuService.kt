package com.example.CentralLAApp.service

import com.example.CentralLAApp.dto.CollectionTermDTO
import com.example.CentralLAApp.dto.CourseData
import com.example.CentralLAApp.dto.TermDTO
import com.example.CentralLAApp.service.helper.Term
import com.example.CentralLAApp.util.TermUtils
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service


@Service
class SuService(
    private val httpService: HTTPService,
    private val objectMapper: ObjectMapper
) {

    val termsAPIWithAuth =
        "https://mysu.sabanciuniv.edu/apps/ods_api/getTerms.php" to "Basic dGVybXNfYXBpOmF5WV8zNjZUYTE="

    fun getTerms(): Collection<TermDTO> {
        val headers = HttpHeaders()
        headers.set("Authorization", termsAPIWithAuth.second)


        val responseEntity = httpService.get(termsAPIWithAuth.first, headers)

        return if (responseEntity.statusCode.is2xxSuccessful) {
            val terms = objectMapper.readValue(responseEntity.body, CollectionTermDTO::class.java).terms
            val idxx = terms.indexOfFirst { it.term_desc == "Spring 2023-2024" }
            val terms2 = terms.subList(0, idxx+1)
            completeTermList(terms2)

        } else {
            emptyList()
        }
    }

    private fun completeTermList(terms: List<TermDTO>): List<TermDTO> {
        return try {
            val activeTermIdx = terms.indexOfFirst { it.is_active == "1" }

            addMoreTerm(terms, 2 - activeTermIdx)
        } catch (_: Exception) {
            throw Exception()
        }
    }

    fun addMoreTerm(terms: List<TermDTO>, count: Int): List<TermDTO> {
        val updatedTerms = mutableListOf<TermDTO>()
        var lastTerm = terms.first()
        for (i in 1..count) {
            lastTerm = lastTerm.nextTermDTO()
            updatedTerms.add(lastTerm)
        }
        updatedTerms.reverse()
        updatedTerms.addAll(terms)
        return updatedTerms
    }

    fun getCurrenTerm() : TermDTO = getTerms().find { it.is_active == "1" }!!

    fun getTermByTermDesc(termDesc : String) = getTerms().find { it.term_desc == termDesc }

}

private fun TermDTO.nextTermDTO(): TermDTO {

    return TermDTO(
        term_code = TermUtils.nextTermCode(this.term_code),
        term_desc = TermUtils.nextTermDesc(this.term_desc),
        term_start_date = TermUtils.nextStartDate(this.term_desc),
        term_end_date = TermUtils.nextEndDate(this.term_desc),
        academic_year = TermUtils.nextAcademicYear(this.term_desc),
        aid_year = this.aid_year,
        is_active = "0"
    )
}

