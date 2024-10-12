package com.example.CentralLAApp.dto.response

data class CurrentTranscriptStatusResponse(
    val currentTerm : String,
    val isUploadedAnyTranscript : Boolean,
    val isUploadedValidTranscript: Boolean,
    val lastUploadedTerm : String?
)