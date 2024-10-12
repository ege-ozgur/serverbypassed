package com.example.CentralLAApp.service.helper

import com.example.CentralLAApp.exception.InvalidInputException
import java.time.Year
import java.util.*
import kotlin.collections.List
import kotlin.math.abs

/**
 * Tries to convert the given input to an integer. If conversion fails, returns the input as a string.
 * If conversion to neither an integer nor a string is possible, throws an InvalidInputException.
 *
 * @param input The input value to be checked.
 * @return If the input can be converted to an integer, returns the integer; otherwise, returns it as a string.
 * @throws InvalidInputException if the input cannot be converted to either an integer or a string.
 */

fun checkInputType(input: Any): Any {
    return input.toString().toIntOrNull() ?: input.toString().takeIf { it.isNotEmpty() }
    ?: throw InvalidInputException("Invalid input. Input type should be either an integer or a string.")
}


fun verifyLong(input: Any) = input.toString().toIntOrNull()?.toLong()
        ?: throw InvalidInputException("Invalid input. Input type should be long.")


fun verifyInt(input: Any) = input.toString().toIntOrNull()
        ?: throw InvalidInputException("Invalid input. Input type should be integer.")


fun validateTerm(inputTerm: String): String {
    try {
        val (term, year) = inputTerm.split(' ')
        val (firstYear, secondYear) = year.split('-')

        if (!term.first().isUpperCase() && Term.values().none { it.name == term.uppercase() }) {
            throw Exception()
        }

        if( !areConsecutiveYears(firstYear,secondYear)){
            throw Exception()
        }

        return inputTerm

    } catch (_: Exception) {
        throw InvalidInputException("Invalid term input. Example input: Spring 2023-2024")
    }
}

fun isValidYear(yearString: String): Boolean {
    return yearString.length == 4 && yearString.all { it.isDigit() }
}


fun areConsecutiveYears(year1: String, year2: String): Boolean {
    if (!isValidYear(year1) || !isValidYear(year2)) {
        return false
    }
    val numericYear1 = year1.toInt()
    val numericYear2 = year2.toInt()

    return numericYear2 - numericYear1 == 1
}
