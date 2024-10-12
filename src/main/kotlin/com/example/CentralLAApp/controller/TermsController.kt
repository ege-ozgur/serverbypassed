package com.example.CentralLAApp.controller

import com.example.CentralLAApp.dto.TermDTO
import com.example.CentralLAApp.dto.request.NotificationRequest
import com.example.CentralLAApp.dto.request.PublicNotificationRequest
import com.example.CentralLAApp.entity.NotificationPreference
import com.example.CentralLAApp.enums.NotificationRelationType
import com.example.CentralLAApp.enums.NotificationType
import com.example.CentralLAApp.service.EmailService
import com.example.CentralLAApp.service.NotificationService
import com.example.CentralLAApp.service.SuService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime


@RestController
@RequestMapping("/api/v1/terms")
class TermsController(
    private val suService: SuService,
) {

    @GetMapping()
    fun getTerms(): Collection<TermDTO> {
        return suService.getTerms()
    }
}