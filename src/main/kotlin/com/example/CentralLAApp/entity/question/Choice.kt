package com.example.CentralLAApp.entity.question


import jakarta.persistence.Embeddable
import jakarta.validation.constraints.NotNull

@Embeddable
data class Choice(
    @NotNull
    val choice: String,

    val conditionallyOpen: String? = null
)