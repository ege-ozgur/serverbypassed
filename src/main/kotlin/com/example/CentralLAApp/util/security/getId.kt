package com.example.CentralLAApp.util.security

import com.example.CentralLAApp.entity.user.Instructor
import com.example.CentralLAApp.entity.user.User
import com.example.CentralLAApp.enums.UserRole
import com.example.CentralLAApp.exception.securityExceptions.UnauthorizedException
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContextHolder
import reactor.core.publisher.Mono

fun getAuthentication() = SecurityContextHolder
    .getContext()
    ?.authentication
        as? UsernamePasswordAuthenticationToken
    ?: throw UnauthorizedException()




fun getId(role: UserRole): Int {
    val user = getUser()
    if (role != UserRole.USER && role != user.role) {
        throw UnauthorizedException()
    }
    return user.userID
}

fun getUser(): User {
    val authentication = getAuthentication()
    val user = authentication.principal as? User ?: throw UnauthorizedException()
    return user
}


fun validateAuthorizedInstructor(authorizedInstructors: MutableList<Instructor>, userId: Int) {
    if (authorizedInstructors.any { it.userID == userId })
        return

    throw UnauthorizedException()
}
