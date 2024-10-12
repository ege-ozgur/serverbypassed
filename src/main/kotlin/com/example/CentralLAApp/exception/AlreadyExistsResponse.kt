package com.example.CentralLAApp.exception
data class AlreadyExistsResponse(
        val error: String,
        val message: String,
        val alreadyExistsList: List<Int>
)