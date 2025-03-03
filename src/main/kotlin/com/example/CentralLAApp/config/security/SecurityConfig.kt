package com.example.CentralLAApp.config.security

import com.example.CentralLAApp.service.LogoutService
import com.example.CentralLAApp.exception.securityExceptions.CustomAuthenticationEntryPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig {

    companion object {
        const val ROLE_STUDENT = "STUDENT"
        const val ROLE_ADMIN = "ADMIN"
        const val ROLE_INSTRUCTOR = "INSTRUCTOR"
        const val ROLE_USER = "USER"
    }

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        //jwtAuthFilter: JwtAuthenticationFilter,
        authenticationProvider: AuthenticationProvider,
        customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
        logoutService: LogoutService
    ): SecurityFilterChain{


         http
             .csrf {
                it.disable()
             }
             .cors {
                 it.configurationSource(corsConfigurationSource())
             }
             .authorizeHttpRequests {
                    it
                        .anyRequest().permitAll()
             }
            .sessionManagement {
                    it
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .exceptionHandling {
                 it.authenticationEntryPoint(customAuthenticationEntryPoint)

            }





             .logout {
                 it
                     .logoutUrl("/api/v1/auth/logout")
                     .addLogoutHandler(logoutService)
                     .logoutSuccessHandler { request, response, authentication ->
                         SecurityContextHolder.clearContext()

                     }
             }



        return http.build()
    }
    private fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()
        config.addAllowedOrigin("http://localhost:3000")
        config.addAllowedOrigin("localhost:3000")
        config.addAllowedOriginPattern("*")
        config.addAllowedMethod("*")
        config.addAllowedHeader("*")

        config.allowCredentials = true
        source.registerCorsConfiguration("/**", config)
        return source
    }

}