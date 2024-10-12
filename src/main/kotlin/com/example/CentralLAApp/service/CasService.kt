package com.example.CentralLAApp.service

import com.example.CentralLAApp.dto.request.UserPayload
import com.example.CentralLAApp.service.helper.XmlToJsonConverter
import com.example.CentralLAApp.service.helper.mapJsonNodeToUserPayload
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.net.URLEncoder


data class CasResponse(
    val status: HttpStatus,
    val jsonResponse: UserPayload?,
    val loginTry: Boolean = false
)

@Service
class CasService(private val httpService: HTTPService) {

    companion object : KLogging()

    fun getCasResponse(serviceUrl: String, ticket: String): CasResponse {
        val url = "https://login.sabanciuniv.edu/cas/serviceValidate"
        val encodedServiceUrl = URLEncoder.encode(serviceUrl, "UTF-8")
        val fullUrl = "$url?service=$encodedServiceUrl&ticket=$ticket"
        val responseEntity = httpService.get(fullUrl)
        logger.info { "CAS Response for serviceUrl: $serviceUrl and ticket: $ticket is: $responseEntity" }
        logger.info { "CAS Response Status Code is ${responseEntity.statusCode}" }
        logger.info { "CAS Response Status Code bool ${responseEntity.statusCode.is2xxSuccessful}" }
        if (responseEntity.statusCode.is2xxSuccessful) {
            logger.info { "Entered successfull response" }
            val xmlResponse = responseEntity.body
            logger.info { "xml Response: $xmlResponse" }
            val jsonResponse = xmlResponse?.let {
                XmlToJsonConverter.convert(it)
            }
            logger.info { "json Response: $jsonResponse" }
            val userPayload = mapJsonNodeToUserPayload(jsonResponse)
            logger.info { "Returning $userPayload" }
            return CasResponse(HttpStatus.OK, userPayload, false)
        }
        logger.info { "Could not enter successfull response, returning null" }
        return CasResponse(HttpStatus.valueOf(responseEntity.statusCode.value()), null, true)
    }
}


