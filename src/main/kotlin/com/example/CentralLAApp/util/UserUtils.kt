package com.example.CentralLAApp.util

import com.example.CentralLAApp.exception.NotFoundException
import com.example.CentralLAApp.service.helper.verifyInt
import org.springframework.data.jpa.repository.JpaRepository

inline fun <reified T : Any, reified DTO : Any> getUserById(searchKey: Any, repository: JpaRepository<T, Int>, converter: (T) -> DTO): DTO {
    val userId: Int = verifyInt(searchKey)
    val user: T = repository.findById(userId).orElseThrow {
        NotFoundException("${T::class.simpleName} with ID $userId not found")
    }

    return converter(user)
}

fun <T, DTO> getAllEntitiesAndMapToDTO(
    repository: JpaRepository<T, Int>,
    converter: (T) -> DTO
): Collection<DTO> =
    repository.findAll().map {
        converter(it)
    }