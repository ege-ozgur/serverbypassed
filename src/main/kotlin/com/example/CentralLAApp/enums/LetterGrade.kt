package com.example.CentralLAApp.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class LetterGrade(@JsonValue val value: String) {
    A("A"),
    A_MINUS("A-"),
    B_PLUS("B+"),
    B("B"),
    B_MINUS("B-"),
    C_PLUS("C+"),
    C("C"),
    C_MINUS("C-"),
    D_PLUS("D+"),
    D("D"),
    F("F"),
    S("S"),
    W("W"),
    IP("IP");

    companion object {
        fun fromString(grade: String): LetterGrade? {
            return values().find { it.value == grade }
        }
    }
}


