package com.example.CentralLAApp.dto

data class TermDTO(
    val term_code: String,
    val term_desc: String,
    val term_start_date: String,
    val term_end_date: String,
    val aid_year: String,
    val academic_year: String,
    val is_active: String
)

data class CollectionTermDTO(
    val login_ok: String,
    val isSuccessful: String,
    val errorDefinition: String,
    val terms: List<TermDTO>
)
