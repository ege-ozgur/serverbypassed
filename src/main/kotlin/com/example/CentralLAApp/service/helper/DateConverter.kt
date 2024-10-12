package com.example.CentralLAApp.service.helper

import com.example.CentralLAApp.exception.LocalDateTimeParseException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

fun parseDateTime(dateTimeString: String): LocalDateTime {
    return try {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        LocalDateTime.parse(dateTimeString, formatter)
    }catch (_ : Exception){
        throw LocalDateTimeParseException("Invalid Date Input. Date format should be: dd/MM/yyyy HH:mm")
    }

}