package com.example.CentralLAApp.service

import com.example.CentralLAApp.repository.TokenRepository
import com.example.CentralLAApp.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.LogoutHandler
import org.springframework.stereotype.Service


@Service
class LogoutService(
    val tokenRepository: TokenRepository,
    val userRepository: UserRepository,
    val jwtService: JwtService
) : LogoutHandler {
    override fun logout(request: HttpServletRequest?, response: HttpServletResponse?, authentication: Authentication?) {
        val authHeader :String? = request?.getHeader("Authorization")
        var jwt :String

        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return
        }


        jwt = authHeader.split(" ")[1].trim();
        val storedToken = tokenRepository.findByToken(jwt).orElse(null)

        if(storedToken != null) {
            storedToken.expired = true
            storedToken.revoked =  true
            tokenRepository.save(storedToken)

            // Get the user associated with the token and update their status
            val userName = jwtService.extractUsername(jwt)
            val user = userRepository.findByEmail(userName!!)
            userRepository.save(user.get()) // Save the updated user status
        }
    }

}