package com.example.CentralLAApp.config.mail

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSenderImpl

@Configuration
class MailConfig {

    @Bean
    fun javaMailSender(): JavaMailSenderImpl {
        val mailSender = JavaMailSenderImpl()
        mailSender.host = "smtp.gmail.com"
        mailSender.port = 587
        mailSender.username = "central.la.app@gmail.com"
        mailSender.password = "qbkq itks ajxd fbqf"

        val props = mailSender.javaMailProperties
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"

        return mailSender
    }
}
