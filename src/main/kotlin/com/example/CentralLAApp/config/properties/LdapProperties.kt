package com.example.CentralLAApp.config.properties

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated

@Configuration
@ConfigurationProperties(prefix = "ldap")
@Validated
class LdapProperties {
    @NotBlank
    lateinit var url: String

    @NotBlank
    lateinit var base: String

    @NotBlank
    lateinit var user: String

    @NotBlank
    lateinit var password: String
    override fun toString(): String {
        return "LdapProperties(url='$url', base='$base', user='$user', password='$password')"
    }


}
