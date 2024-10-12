package com.example.CentralLAApp.config.auth

import com.example.CentralLAApp.config.properties.LdapProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.core.support.LdapContextSource

@Configuration
class LdapConfig(private val ldapProperties: LdapProperties) {

    @Bean
    fun contextSource(): LdapContextSource {
        val contextSource = LdapContextSource()
        contextSource.setUrl(ldapProperties.url)
        contextSource.setBase(ldapProperties.base)
        contextSource.setUserDn(ldapProperties.user)
        contextSource.setPassword(ldapProperties.password)
        contextSource.setReferral("ignore")
        contextSource.afterPropertiesSet()
        println("LDAP CONFIG $ldapProperties")
        return contextSource
    }

    @Bean
    fun ldapTemplate(contextSource: LdapContextSource): LdapTemplate {
        return LdapTemplate(contextSource)
    }
}
