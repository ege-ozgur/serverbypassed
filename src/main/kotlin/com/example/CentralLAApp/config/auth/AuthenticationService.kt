package com.example.CentralLAApp.config.auth


import com.example.CentralLAApp.dto.response.UserDTO
import com.example.CentralLAApp.entity.auth.Token
import com.example.CentralLAApp.entity.auth.TokenType
import com.example.CentralLAApp.entity.user.Instructor
import com.example.CentralLAApp.entity.user.Student
import com.example.CentralLAApp.entity.user.User
import com.example.CentralLAApp.enums.UserRole
import com.example.CentralLAApp.exception.TokenIsNotValidException
import com.example.CentralLAApp.exception.securityExceptions.UnauthorizedException
import com.example.CentralLAApp.repository.TokenRepository
import com.example.CentralLAApp.repository.UserRepository
import com.example.CentralLAApp.service.ApplicationService
import com.example.CentralLAApp.service.CasService
import com.example.CentralLAApp.service.JwtService
import com.example.CentralLAApp.service.LdapUserService
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder

import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse

@Service
class AuthenticationService(
    private val repository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val tokenRepository: TokenRepository,
    private val casService: CasService,
    private val ldapUserService: LdapUserService,
    private val applicationService: ApplicationService
) {

    companion object : KLogging()


    fun authenticate(request: AuthenticationRequest): AuthenticationResponse {

        logger.info { "Authentication Request: $request" }
        val response = casService.getCasResponse(request.serviceUrl, request.ticket)
        logger.info { "Authentication Response: $response" }

        if (!response.status.is2xxSuccessful) {
            throw TokenIsNotValidException("Token is not a valid token", HttpStatus.UNAUTHORIZED)
        }

        if (response.jsonResponse?.authenticationSuccess == null) {
            logger.info { "Request is $request" }
            throw TokenIsNotValidException("Token is not a valid token", HttpStatus.UNAUTHORIZED)
        }

        if (response.loginTry) {
            throw TokenIsNotValidException("Token is not a valid token", HttpStatus.UNAUTHORIZED)
        }

        val user = repository.findByEmail(response.jsonResponse.authenticationSuccess.attributes.mail).getOrElse {
            with(response.jsonResponse.authenticationSuccess.attributes) {
                logger.info { "Could not find the user... creating new one." }
                val role = when (this.ou.lastOrNull()) {
                    "student" -> UserRole.STUDENT
                    "academic" -> UserRole.INSTRUCTOR
                    else -> throw UnauthorizedException()
                }
                if (role == UserRole.STUDENT) {
                    if(this.ou[0] != "UG"){
                        throw UnauthorizedException()
                    }
                }
                val userBuilder = when (role) {
                    UserRole.STUDENT -> Student.builder()
                    UserRole.INSTRUCTOR -> Instructor.builder()
                    else -> User.builder()
                }
                logger.info { "Getting user id from ldap server..." }
                val userId = ldapUserService.getUserId(this.sAMAccountName)!!

                logger.info { "Getting photo url" }
                val photoUrl = if(role == UserRole.STUDENT) applicationService.getPhotoUrlByUserId(userId, role) else "https://www.sabanciuniv.edu/rehber/fotograflar/${userId.trimStart('0')}.jpg"
                logger.info { "Everything is ok creating new user." }
                val user = userBuilder
                    .email(this.mail)
                    .graduationType(this.ou[0])
                    .name(this.givenName)
                    .surname(this.sn)
                    .password("123")
                    .role(role)
                    .universityId(userId)
                    .photoUrl(photoUrl)
                    .build()

                repository.save(user)

            }
        }


        val roleMap: Map<String, Any> = mapOf(
            "role" to user.role,
            "userID" to user.userID
        )
        val jwtToken = jwtService.generateToken(userDetails = user, extraClaims = roleMap)
        revokeAllUserTokens(user)

        val token = Token.builder()
            .user(user)
            .token(jwtToken)
            .tokenType(TokenType.BEARER)
            .expired(false)
            .revoked(false)
            .build()


        tokenRepository.save(token)

        val userDtoObject = user.let {
            UserDTO(
                it.userID,
                it.email,
                it._name,
                it.surname,
                it.graduationType,
                it.role,
                notificationPreference = it.notificationPreferences,
                it.photoUrl,
                it.universityId
            )
        }


        return AuthenticationResponse
            .builder()
            .token(jwtToken)
            .user(userDtoObject)
            .id(user.userID)
            .build()
    }


    private fun revokeAllUserTokens(user: User) {
        val validUserTokens = tokenRepository.findAllValidTokensByUser(user.userID)
        if (validUserTokens.isEmpty())
            return
        validUserTokens.forEach {
            it.expired = true
            it.revoked = true
        }
        tokenRepository.saveAll(validUserTokens)
    }

}