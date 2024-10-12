package com.example.CentralLAApp.util

import com.example.CentralLAApp.dto.TermDTO
import com.example.CentralLAApp.service.helper.Term


class TermUtils {
    companion object {

        private data class SemesterDates(val startDate: String, val endDate: String)

        private val defaultSemesterDates = mapOf(
            Term.FALL to SemesterDates("09-15", "01-15"),
            Term.SPRING to SemesterDates("01-25", "06-12"),
            Term.SUMMER to SemesterDates("06-15", "09-01")
        )

        private fun nextTerm(currentTerm: Term): Term {
            return when (currentTerm) {
                Term.FALL -> Term.SPRING
                Term.SPRING -> Term.SUMMER
                Term.SUMMER -> Term.FALL
            }
        }

        private fun currentTerm(termDesc: String): Term {
            return Term.valueOf(termDesc)
        }

        fun nextTermDesc(termDesc: String): String {
            val (term, years) = termDesc.split(" ")
            val (firstYear, secondYear) = years.split("-")

            val currentTerm = currentTerm(term.uppercase())
            val nextTerm = nextTerm(currentTerm)

            val nextYears = nextYears(firstYear, secondYear, currentTerm)

            return "${nextTerm.value} $nextYears"
        }

        private fun nextYears(firstYear: String, secondYear: String, term: Term): String {
            return when (term) {
                Term.SUMMER -> "${secondYear}-${secondYear.toInt() + 1}"
                else -> "$firstYear-$secondYear"
            }
        }

        fun nextStartDate(termDesc: String): String {
            val (term, years) = termDesc.split(" ")
            val (firstYear, secondYear) = years.split("-")

            val currentTerm = currentTerm(term.uppercase())
            val nextTerm = nextTerm(currentTerm)

            return "$secondYear-${defaultSemesterDates[nextTerm]!!.startDate}"
        }

        fun nextEndDate(termDesc: String): String {
            val (term, years) = termDesc.split(" ")
            val (firstYear, secondYear) = years.split("-")

            val currentTerm = currentTerm(term.uppercase())
            val nextTerm = nextTerm(currentTerm)
            val nextYear =  secondYear.toInt() + if (nextTerm != Term.FALL) 0 else 1
            return "$nextYear-${defaultSemesterDates[nextTerm]!!.endDate}"
        }

        fun nextAcademicYear(termDesc: String): String {
            val term = termDesc.split(" ")[0]
            val currentTerm = currentTerm(term.uppercase())
            val currentYear = termDesc.split(" ")[1].split("-")[0].toInt()
            val nextYear = (currentYear + if (currentTerm == Term.SUMMER) 1 else 0)
            return nextYear.toString()

        }

        fun nextTermCode(termCode: String): String {
            val currentYear = termCode.substring(0, 4).toInt()
            val currentNumber = termCode.substring(4).toInt()

            val nextYear = if (currentNumber == 3) currentYear + 1 else currentYear
            val nextNumber = (currentNumber % 3) + 1

            return "${nextYear}${String.format("%02d", nextNumber)}"
        }

        fun compareTwoTerms(td1: String, td2: String): Int {
            val (term_1, year_1) = td1.split(" ")
            val (term_2, year_2) = td2.split(" ")

            val currentTerm1 = currentTerm(term_1.uppercase())
            val currentTerm2 = currentTerm(term_2.uppercase())
            println(currentTerm1)
            println(currentTerm2)
            if (year_1 >  year_2)
                return 1
            else if (year_2 > year_1)
                return -1
            else if (currentTerm1 > currentTerm2)
                return 1
            else if (currentTerm2 > currentTerm1)
                return -1
            else
                return 0
        }

    }
}



