package com.example.CentralLAApp.service

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URI


@Service
class HTTPService(private val restTemplate: RestTemplate) {

    fun get(url: String, headers: HttpHeaders = HttpHeaders()): ResponseEntity<String> {
        val requestEntity = HttpEntity<Any>(headers)
        return restTemplate.exchange(URI.create(url), HttpMethod.GET, requestEntity, String::class.java)
    }

    fun post(url: String, requestEntity: HttpEntity<Any>): ResponseEntity<String> {
        return restTemplate.exchange(URI.create(url), HttpMethod.POST, requestEntity, String::class.java)
    }

}