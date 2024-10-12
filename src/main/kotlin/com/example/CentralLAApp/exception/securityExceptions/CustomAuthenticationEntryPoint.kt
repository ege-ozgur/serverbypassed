package com.example.CentralLAApp.exception.securityExceptions
import com.example.CentralLAApp.exception.CustomErrorResponse
import com.example.CentralLAApp.exception.TokenIsNotValidException
import com.example.CentralLAApp.exception.UsernameAlreadyExistsException
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerExceptionResolver
import java.io.IOException


@Component
class CustomAuthenticationEntryPoint(
    @Qualifier("handlerExceptionResolver") val resolver: HandlerExceptionResolver,

) : AuthenticationEntryPoint {

    companion object {
        private val objectMapper = ObjectMapper()

    }

    @Throws(IOException::class)
    override fun commence(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        authException: AuthenticationException?
    ) {



        when (authException) {
            is UsernameAlreadyExistsException -> {
                response?.apply {
                    status = HttpStatus.CONFLICT.value()
                    contentType = "application/json"
                    val customErrorResponse = CustomErrorResponse(
                        error = authException.status?.toString() ?: "Auth Error",
                        message = authException.message ?: ""
                    )
                    writer.write(objectMapper.writeValueAsString(customErrorResponse))
                    writer.flush()
                }
            }
            is TokenIsNotValidException -> {
                response?.apply {
                    status = HttpStatus.CONFLICT.value()
                    contentType = "application/json"
                    val customErrorResponse = CustomErrorResponse(
                        error = authException.status?.toString() ?: "Auth Error",
                        message = authException.message ?: ""
                    )
                    writer.write(objectMapper.writeValueAsString(customErrorResponse))
                    writer.flush()
                }
            }
            else -> {
                response?.apply {
                    status = HttpStatus.UNAUTHORIZED.value()
                    contentType = "application/json"
                    writer.write("{\"error\": \"Unauthorized\", \"message\": \"${authException!!.message}\"}")
                    writer.flush()
                }
            }
        }

    }

}