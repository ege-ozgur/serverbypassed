package com.example.CentralLAApp.controller

import com.example.CentralLAApp.dto.request.CourseDTORequest
import com.example.CentralLAApp.dto.response.CourseDTOResponse
import com.example.CentralLAApp.enums.UserRole
import com.example.CentralLAApp.service.CoursesService
import com.example.CentralLAApp.util.security.getId
import com.example.CentralLAApp.util.security.getUser
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


@RestController()
@RequestMapping("/api/v1/courses")
class CoursesController(private val coursesService: CoursesService) {
    @GetMapping
    fun getCourses(): Collection<CourseDTOResponse> {
        return coursesService.getAllCourses()
    }

    @PostMapping
    fun addCourse(@RequestBody course: CourseDTORequest): ResponseEntity<Any> {
        getId(UserRole.INSTRUCTOR)
        val courseDTO = coursesService.addCourse(course)

        return ResponseEntity.status(HttpStatus.CREATED).body(courseDTO)
    }

}