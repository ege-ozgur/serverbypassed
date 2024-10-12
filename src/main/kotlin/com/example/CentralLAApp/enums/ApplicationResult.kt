package com.example.CentralLAApp.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class ApplicationResult(@JsonValue val value: String) {
    IN_PROGRESS("In Progress"),
    ACCEPTED("Accepted"),
    REJECTED("Rejected"),
    WAIT_LISTED("Waiting List"),
    WITHDRAWN("Withdrawn"),
}