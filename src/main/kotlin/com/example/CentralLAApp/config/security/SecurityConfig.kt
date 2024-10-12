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
        jwtAuthFilter: JwtAuthenticationFilter,
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
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/terms").permitAll()
                        .requestMatchers("/api/v1/users/instructors/**").hasRole(ROLE_INSTRUCTOR)
                        .requestMatchers("/api/v1/courses/**").hasRole(ROLE_INSTRUCTOR)

                        .requestMatchers("/api/v1/users/students/info").hasRole(ROLE_STUDENT)
                        .requestMatchers( "/api/v1/users/previous-grades").hasRole(ROLE_STUDENT)

                        .requestMatchers(HttpMethod.GET,"/api/v1/applicationRequest/student/{studentId}").authenticated()
                        .requestMatchers(HttpMethod.POST,"/api/v1/applicationRequest/student/la_history").authenticated()


                        .requestMatchers(HttpMethod.PUT,"/api/v1/applicationRequest/{searchKey}/status").authenticated()
                        .requestMatchers(HttpMethod.PUT,"/api/v1/applicationRequest/status").authenticated()

                        .requestMatchers("/api/v1/applicationRequest/student/**").hasRole(ROLE_STUDENT)
                        .requestMatchers("/api/v1/applicationRequest/student").hasRole(ROLE_STUDENT)
                        .requestMatchers("/api/v1/applicationRequest/withdraw/{searchKey}").hasRole(ROLE_STUDENT)
                        .requestMatchers("/api/v1/applicationRequest/{searchKey}/commit").hasRole(ROLE_STUDENT)
                        .requestMatchers("/api/v1/applicationRequest/{searchKey}/uncommit").hasRole(ROLE_STUDENT)
                        .requestMatchers("/api/v1/applicationRequest/{applicationReqId}/workHour").authenticated()

                        .requestMatchers("/api/v1/applicationRequest/{applicationId}/accept-all").hasRole(ROLE_INSTRUCTOR)
                        .requestMatchers("/api/v1/applicationRequest/{applicationId}/reject-all").hasRole(ROLE_INSTRUCTOR)
                        .requestMatchers("/api/v1/applicationRequest/instructor/finalizeStatus/{searchKey}").hasRole(ROLE_INSTRUCTOR)
                        .requestMatchers("/api/v1/applicationRequest/instructor/resetCommitment/{applicationReqId}").hasRole(ROLE_INSTRUCTOR)
                        .requestMatchers("/api/v1/applicationRequest/instructor/redFlag/{applicationReqId}").hasRole(ROLE_INSTRUCTOR)
                        .requestMatchers("/api/v1/applicationRequest/instructor/unRedFlag/{applicationReqId}").hasRole(ROLE_INSTRUCTOR)




                        .requestMatchers(HttpMethod.GET,"/api/v1/applicationRequest/{searchKey}").authenticated()

                        .requestMatchers("/api/v1/transcript/**").authenticated()

                        .requestMatchers(HttpMethod.GET,"/api/v1/applications").authenticated()
                        .requestMatchers(HttpMethod.GET,"/api/v1/applications/{searchKey}").authenticated()

                        .requestMatchers("/api/v1/applications/student/**").hasRole(ROLE_STUDENT)
                        .requestMatchers("/api/v1/applications/**").hasRole(ROLE_INSTRUCTOR)
                        .requestMatchers("/api/v1/applications/instructor/**").hasRole(ROLE_INSTRUCTOR)
                        .requestMatchers("/api/v1/applications/instructor").hasRole(ROLE_INSTRUCTOR)

                        .requestMatchers("/api/v1/terms/**").authenticated()
                        .requestMatchers("/api/v1/users/info").authenticated()
                        .requestMatchers("/ws").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/api/v1/notifications/**").authenticated()
                        .requestMatchers(HttpMethod.PUT,"/api/v1/applicationRequest/updateWorkHour/{searchKey}").hasRole(ROLE_INSTRUCTOR)
                        .requestMatchers(HttpMethod.PUT,"/api/v1/applications/{searchKey}/mailUpdate").hasRole(ROLE_INSTRUCTOR)
                        .anyRequest().hasRole(ROLE_ADMIN)
                        //.anyRequest().permitAll()


             }
            .sessionManagement {
                    it
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .exceptionHandling {
                 it.authenticationEntryPoint(customAuthenticationEntryPoint)

            }

            .authenticationProvider(authenticationProvider)

            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

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