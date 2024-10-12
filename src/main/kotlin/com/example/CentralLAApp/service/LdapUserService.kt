package com.example.CentralLAApp.service

import org.springframework.context.annotation.Bean
import org.springframework.ldap.core.AttributesMapper
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.query.LdapQueryBuilder
import org.springframework.stereotype.Service

@Service
class LdapUserService(private val ldapTemplate: LdapTemplate) {

    fun getUserId(username: String): String? {
        val query = LdapQueryBuilder.query().where("sAMAccountName").`is`(username)

        val userId = ldapTemplate.search(
            query,
            AttributesMapper { attrs ->
                val id = attrs.get("universityID")?.get()?.toString()
                id
            }
        )

        return if (userId.isEmpty()) {
            null
        } else {
            userId.first()
        }
    }
}
