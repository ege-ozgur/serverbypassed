package com.example.CentralLAApp

import com.example.CentralLAApp.exceptionHandler.GlobalErrorHandler
import com.example.CentralLAApp.service.LdapUserService
import com.example.CentralLAApp.util.extractTextFromPDF
import com.example.CentralLAApp.util.*

import com.example.CentralLAApp.service.TranscriptService
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableScheduling

	@SpringBootApplication
	@EnableScheduling
	@Import(
		GlobalErrorHandler::class
	)
	class CentralLaAppApplication : SpringBootServletInitializer() {

		override fun configure(application: SpringApplicationBuilder): SpringApplicationBuilder {
			return application.sources(CentralLaAppApplication::class.java)
		}

		companion object {
			@JvmStatic
			fun main(args: Array<String>) {
				SpringApplication.run(CentralLaAppApplication::class.java, *args)
			}
		}
	}
