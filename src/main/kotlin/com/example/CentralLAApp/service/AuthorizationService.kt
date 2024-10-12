package com.example.CentralLAApp.service

import com.example.CentralLAApp.entity.user.User
import com.example.CentralLAApp.enums.UserRole
import com.example.CentralLAApp.exception.NotFoundException
import com.example.CentralLAApp.exception.securityExceptions.UnauthorizedException
import com.example.CentralLAApp.repository.ApplicationRequestRepository
import com.example.CentralLAApp.repository.StudentRepository
import org.springframework.stereotype.Service

@Service
class AuthorizationService(
    private val studentRepository: StudentRepository,
    private val applicationRequestRepository: ApplicationRequestRepository
) {

    private fun instructorHasStudentApplied(instructorId: Int, studentId: Int) = applicationRequestRepository.checkByInstructorIdAndStudentId(instructorId, studentId)



    fun validateAuthorizationToStudentInfo(user: User, studentId: Int) {
        studentRepository.findById(studentId).orElseThrow{ NotFoundException("Student with id: $studentId not found") }

        when (user.role) {
            UserRole.STUDENT -> validateStudentAccess(user, studentId)
            UserRole.INSTRUCTOR -> validateInstructorAccess(user, studentId)
            else -> throw UnauthorizedException()
        }
    }

    private fun validateStudentAccess(user: User, studentId: Int) {
        if (user.userID != studentId) {
            throw UnauthorizedException()
        }
    }

    private fun validateInstructorAccess(user: User, studentId: Int) {
        if (instructorHasStudentApplied(user.userID, studentId) == 0) {
            throw UnauthorizedException()
        }
    }
}