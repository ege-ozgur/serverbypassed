package com.example.CentralLAApp.controller


import com.example.CentralLAApp.config.auth.AuthenticationRequest
import com.example.CentralLAApp.config.auth.AuthenticationResponse
import com.example.CentralLAApp.config.auth.AuthenticationService
import com.example.CentralLAApp.config.auth.RegisterRequest
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/v1/auth")
class AuthenticationController(
    private val service: AuthenticationService

) {
    /*@PostMapping("/register")
    fun register(
        @RequestBody request: RegisterRequest
    ):ResponseEntity<AuthenticationResponse>{

       return ResponseEntity.ok(service.register(request))
    }*/

    @PostMapping("/authentication")
    fun register(
        @RequestBody request: AuthenticationRequest
    ):ResponseEntity<AuthenticationResponse>{
        return ResponseEntity.ok(service.authenticate(request))
    }


    @GetMapping("/logout")
    @ResponseBody
    fun logout(request: HttpServletRequest, response: HttpServletResponse): ResponseEntity<String> {

        SecurityContextHolder.clearContext()
        val session: HttpSession? = request.getSession(false)
        session?.invalidate()


        return ResponseEntity("Logout Successful", HttpStatus.OK)
    }



}