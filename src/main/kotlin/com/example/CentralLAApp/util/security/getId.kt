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

fun dummyUser(): User {
    return User.builder()
        .id(17)
        .email("dummy@example.com")
        .password("dummy")
        .name("Dummy")
        .surname("User")
        .role(UserRole.USER)
        .build()
}

fun getAuthentication(): UsernamePasswordAuthenticationToken {
    val auth = SecurityContextHolder.getContext()?.authentication as? UsernamePasswordAuthenticationToken
    return auth ?: UsernamePasswordAuthenticationToken(dummyUser(), null, emptyList())
}

fun getId(role: UserRole): Int {
    val user = getUser()
    // If you want to disable role checking, you can remove or comment out the check:
    // if (role != UserRole.USER && role != user.role) {
    //     throw UnauthorizedException()
    // }
    return user.userID
}

fun getUser(): User {
    // This will now never throw UnauthorizedException because getAuthentication returns a dummy user if none exists.
    val authentication = getAuthentication()
    val user = authentication.principal as? User ?: dummyUser()
    return user
}

fun validateAuthorizedInstructor(authorizedInstructors: MutableList<Instructor>, userId: Int) {
    if (authorizedInstructors.any { it.userID == userId })
        return

    // You might disable this check or provide a fallback if you want to allow access without authorization.
    // throw UnauthorizedException()
}
