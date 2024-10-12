package com.example.CentralLAApp.service

import com.example.CentralLAApp.dto.request.UserDTORequest
import com.example.CentralLAApp.dto.response.*
import com.example.CentralLAApp.entity.NotificationPreference
import com.example.CentralLAApp.entity.user.Instructor
import com.example.CentralLAApp.entity.user.Student
import com.example.CentralLAApp.entity.user.User
import com.example.CentralLAApp.enums.UserRole
import com.example.CentralLAApp.exception.NotFoundException
import com.example.CentralLAApp.repository.InstructorRepository
import com.example.CentralLAApp.repository.StudentRepository
import com.example.CentralLAApp.repository.UserRepository
import com.example.CentralLAApp.service.helper.*
import com.example.CentralLAApp.util.getAllEntitiesAndMapToDTO
import com.example.CentralLAApp.util.getUserById
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrElse


@Service
class UserService(
    val userRepository: UserRepository,
    val instructorRepository: InstructorRepository,
    val studentRepository: StudentRepository,
    private val applicationService: ApplicationService
) {

    companion object : KLogging()

    fun getAll(userType: UserRole): Any? =
        when (userType) {
            UserRole.STUDENT -> getAllStudents()
            UserRole.INSTRUCTOR -> getAllInstructors()
            else -> getAllUsers()
        }

    fun getAllUsers(): Collection<UserDTO> =
        getAllEntitiesAndMapToDTO(userRepository) { convertToUserDTOResponse(it) }

    fun getAllStudents(): Collection<StudentDTOResponse> =
        getAllEntitiesAndMapToDTO(studentRepository) { convertToStudentDTOResponse(it) }

    fun getAllInstructors(): Collection<InstructorDTOResponse> =
        getAllEntitiesAndMapToDTO(instructorRepository) { convertToInstructorDTOResponse(it) }

    fun getById(searchKey: Any, userType: UserRole = UserRole.USER): Any? =
        when (userType) {
            UserRole.STUDENT -> getUserById(searchKey, studentRepository) { convertToStudentDTOResponse(it) }
            UserRole.INSTRUCTOR -> getUserById(searchKey, instructorRepository) { convertToInstructorDTOResponse(it) }
            else -> getUserById(searchKey, userRepository) { convertToUserDTOResponse(it) }
        }

    fun addUser(user: UserDTORequest): UserDTO {

        val role = user.role

        val userEntity =
            userRepository.findByEmail(user.email).getOrElse {

                when (role) {
                    UserRole.STUDENT -> Student.builder()
                    UserRole.INSTRUCTOR -> Instructor.builder()
                    else -> User.builder()
                }
                    .name(user.name)
                    .surname(user.surname)
                    .email(user.email)
                    .graduationType(user.graduationType)
                    .password("123")
                    .role(role)
                    .build()
                    .also {
                        userRepository.save(it)
                    }
            }



        return convertToUserDTOResponse(userEntity)
    }


    fun deleteUser(input: Any): ResponseEntity<Any> {
        val userId = verifyInt(input)

        val user = userRepository.findById(userId).getOrElse {
            throw NotFoundException("User with id: $userId not found")
        }

        if (user.role == UserRole.INSTRUCTOR) {
            val instructor = user as Instructor
            instructor.applications.forEach {
                it.removeInstructor(instructor)
            }
            instructor.students.forEach {
                it.removeInstructor(instructor)
            }

            val applicationsCopy = ArrayList(instructor.applications)

            applicationsCopy.forEach {
                it.removeInstructor(instructor)
                if (it.authorizedInstructors.isEmpty()) {
                    applicationService.deleteApplicationById(it.applicationId, user)
                }
            }
        }

        userRepository.deleteById(userId)

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    fun updateUser(searchKey: Any, userInput: UserDTORequest): UserDTO {
        val userId: Int = verifyInt(searchKey)

        val user: User = userRepository.findById(userId).getOrElse {
            throw NotFoundException("User with id: $searchKey not found")
        }

        if (user.role != userInput.role) {
            deleteUser(userId)
            return addUser(userInput)
        }
        val updatedUser = user.let {
            it.role = userInput.role
            it._name = userInput.name
            it.surname = userInput.surname
            it.email = userInput.email
            it.graduationType = userInput.graduationType
            it.notificationPreferences = NotificationPreference()
            userRepository.save(it)
        }

        return convertToUserDTOResponse(updatedUser)
    }

    fun getPreviousInstructors(searchKey: Any): Collection<InstructorDTOResponse> {
        val userId: Int = verifyInt(searchKey)
        val student = studentRepository.findById(userId).orElseThrow {
            NotFoundException("Student with ID $userId not found")
        }
        val previousInstructors: MutableSet<Instructor> = student.previousInstructors

        return previousInstructors.map {
            it.run { convertToInstructorDTOResponse(this) }
        }
    }

    fun getStudentPreviousGrades(searchKey: Any): List<CoursesAndGradesResponse> {
        val userId: Int = verifyInt(searchKey)
        val student = studentRepository.findById(userId).orElseThrow {
            NotFoundException("Student with ID $userId not found")
        }

        return student.transcripts.lastOrNull()?.courses?.run {
            convertToCoursesAndGradesResponse(this)
        } ?: listOf()

    }


}
