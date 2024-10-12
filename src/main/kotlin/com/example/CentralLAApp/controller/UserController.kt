package com.example.CentralLAApp.controller

import com.example.CentralLAApp.dto.request.UserDTORequest
import com.example.CentralLAApp.dto.response.*
import com.example.CentralLAApp.enums.UserRole
import com.example.CentralLAApp.service.UserService
import com.example.CentralLAApp.util.security.getId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1/users")
class UserController (val userService: UserService) {

    @GetMapping
    fun getUsers(): Collection<UserDTO> {
        return userService.getAllUsers()
    }

    @GetMapping("/students")
    fun getStudents(): Collection<StudentDTOResponse>{
        return userService.getAllStudents()
    }
    @GetMapping("/instructors")
    fun getInstructors(): Collection<InstructorDTOResponse>{
        return userService.getAllInstructors()
    }


    @GetMapping("/info")
    fun getUserById(): UserDTO {
        val userId = getId(UserRole.USER)
        return userService.getById(userId,UserRole.USER) as UserDTO;
    }

    @GetMapping("/students/info")
    fun getStudentById(): StudentDTOResponse {
        val studentId = getId(UserRole.USER)
        return userService.getById(studentId,UserRole.STUDENT) as StudentDTOResponse;
    }

    @GetMapping("/instructors/info")
    suspend fun getInstructorById(): InstructorDTOResponse {
        val instructorId = getId(UserRole.INSTRUCTOR)
        return userService.getById(instructorId,UserRole.INSTRUCTOR) as InstructorDTOResponse;
    }

    @PostMapping
    fun addUser(@RequestBody user: UserDTORequest): ResponseEntity<Any> {
        val userDTO = userService.addUser(user)

        return ResponseEntity.status(HttpStatus.CREATED).body(userDTO)
    }

    @DeleteMapping("/{searchKey}")
    suspend fun deleteUser(@PathVariable searchKey: Any): ResponseEntity<Any> {
        return userService.deleteUser(searchKey)
    }

    @PutMapping("/{searchKey}")
    fun updateUser(@PathVariable searchKey: Any, @RequestBody user: UserDTORequest): UserDTO {
        return userService.updateUser(searchKey, user)
    }

    @GetMapping("/previous-grades")
    fun getStudentPreviousGrades(): List<CoursesAndGradesResponse> {
        val studentId = getId(UserRole.STUDENT)
        return userService.getStudentPreviousGrades(studentId)
    }

}