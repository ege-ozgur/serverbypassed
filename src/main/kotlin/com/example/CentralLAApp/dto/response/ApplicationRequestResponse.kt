package com.example.CentralLAApp.dto.response

class ApplicationRequestResponse(
    val course: CourseDTOResponse,
    val applicationRequests: List<ApplicationRequestDTOForApplicationResponse>
)