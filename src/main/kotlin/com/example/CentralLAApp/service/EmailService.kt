package com.example.CentralLAApp.service

import jakarta.mail.internet.MimeMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class EmailService {

    @Autowired
    private lateinit var javaMailSender: JavaMailSender

    fun sendEmail(to: String, subject: String, text: String) {
        val message: MimeMessage = javaMailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true)

        helper.setTo(to)
        helper.setSubject(subject)
        helper.setText(text, true)

        javaMailSender.send(message)
    }
}