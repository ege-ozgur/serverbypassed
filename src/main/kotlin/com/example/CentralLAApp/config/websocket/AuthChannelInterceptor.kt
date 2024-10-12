package com.example.CentralLAApp.config.websocket

import com.example.CentralLAApp.entity.user.Instructor
import com.example.CentralLAApp.entity.user.Student
import com.example.CentralLAApp.enums.UserRole
import com.example.CentralLAApp.repository.TokenRepository
import com.example.CentralLAApp.service.JwtService
import org.slf4j.LoggerFactory
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService


@Component
class AuthChannelInterceptor(
    private val jwtService: JwtService,
    val userDetailsService: UserDetailsService,
    private val tokenRepository: TokenRepository
) : ChannelInterceptor {

    private val logger = LoggerFactory.getLogger(AuthChannelInterceptor::class.java)
    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
        logger.info("Intercepting STOMP command: ${accessor?.command}")
        if (accessor?.command == StompCommand.CONNECT) {
            logger.info("Handling connect command")
            val authHeader: String? = accessor.getFirstNativeHeader("Authorization")
            logger.info("authHeader: $authHeader")
            accessor.sessionAttributes?.set("authHeader", authHeader)
        } else if (accessor?.command == StompCommand.SUBSCRIBE) {
            logger.info("Handling subscribe command")
            logger.info("accessor.sessionAttributes: ${accessor.sessionAttributes}")

            var authHeader :String? = accessor.sessionAttributes?.get("authHeader") as String?
            val username :String?
            logger.info("authHeader: $authHeader")

            if(authHeader == null ){
                logger.error("Invalid JWT token")
                throw AccessDeniedException("Invalid JWT token")
            }
            if (!authHeader.startsWith("Bearer ")){
                authHeader = "Bearer $authHeader"
            }
            val jwt :String = authHeader.split(" ")[1].trim();

            try {
                username = jwtService.extractUsername(jwt)
            }
            catch (ex:Exception){
                logger.error("Token validation failed: ${ex.message}")
                throw AccessDeniedException( "Token is not valid")
            }



            val userSubscription = accessor.destination
            logger.info("User subscription destination: $userSubscription")
            if(userSubscription != null && username != null && SecurityContextHolder.getContext().authentication == null){
                val userDetails: UserDetails = this.userDetailsService.loadUserByUsername(username)
                logger.info("All claims ${jwtService.extractAllClaims(jwt)}")

                val userId = jwtService.extractAllClaims(jwt)["userID"] as? Int ?: throw AccessDeniedException("Token is not valid")

                val isTokenValid = tokenRepository.findByToken(jwt)
                    .map {
                        !it.expired && !it.revoked
                    }.orElse(false)

                if (jwtService.isTokenValid(jwt,userDetails) && isTokenValid) run {

                    /*if (!userSubscription.startsWith("/user") ) {
                        logger.error("Destination does not starts with userıd ($userId: $userSubscription")
                        throw AccessDeniedException("User ID does not match the subscription topic")
                    }*/


                    val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                    SecurityContextHolder.getContext().authentication = authentication
                    try {
                        accessor.user = if (userDetails.authorities.contains(SimpleGrantedAuthority(UserRole.INSTRUCTOR.value))) userDetails as Instructor else  userDetails as Student
                        logger.info("accessor.user: ${accessor.user!!.name}")
                        logger.info("auth: ${authentication.principal}")
                    } catch (e: Exception) {
                        logger.info("Except: ${e.message}")
                    }

                }else {
                    throw AccessDeniedException("Token is not valid")
                }
            }

        }

        return super.preSend(message, channel)!!
    }
}
