package com.example.CentralLAApp.config.app

import com.example.CentralLAApp.repository.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration

import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.client.RestTemplate

@Configuration
class ApplicationConfig (
    private val repository: UserRepository
) {
    @Bean
    fun userDetailsService(): org.springframework.security.core.userdetails.UserDetailsService =
        org.springframework.security.core.userdetails.UserDetailsService { username ->
            repository.findByEmail(username)
                .orElseThrow { UsernameNotFoundException("User not found") }

        }
    @Bean
    fun authenticationProvider():AuthenticationProvider{
        val authProvider = DaoAuthenticationProvider()
        authProvider.setUserDetailsService(userDetailsService())
        authProvider.setPasswordEncoder(passwordEncoder())
        return authProvider
    }

    @Bean
    fun authenticationManager(
        config:AuthenticationConfiguration
    ):AuthenticationManager {
        return config.authenticationManager
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {

        return BCryptPasswordEncoder()
    }

    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}