package com.example.CentralLAApp.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class Eligibility(@JsonValue val value: String){
    ELIGIBLE("Eligible"),
    NOT_ELIGIBLE("Not Eligible"),
    DEADLINE_PASSED("Deadline Passed"),
    NO_TRANSCRIPT("No Transcript Uploaded")
}