package com.example.CentralLAApp.service

import com.example.CentralLAApp.dto.request.CourseDTORequest
import com.example.CentralLAApp.dto.response.CourseDTOResponse
import com.example.CentralLAApp.entity.course.Course
import com.example.CentralLAApp.exception.InvalidInputException
import com.example.CentralLAApp.exception.NotFoundException
import com.example.CentralLAApp.repository.CourseRepository
import com.example.CentralLAApp.service.helper.checkInputType
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.util.Optional
import kotlin.jvm.optionals.getOrElse

@Service
class CoursesService(private val courseRepository: CourseRepository) {


    fun getAllCourses(): Collection<CourseDTOResponse> {
        return courseRepository
            .findAll(Sort.by(Sort.Direction.ASC, "courseCode"))
            .map {
                CourseDTOResponse(
                    it.id!!,
                    it.courseTitle,
                    it.courseCode,
                    it.unit
                )
            }
    }

    fun getCourseByIdOrCourseCode(input: Any): CourseDTOResponse {

        val course: Course = when (val searchKey = checkInputType(input)) {
            is Int -> courseRepository.findById(searchKey)
                .orElseThrow { NotFoundException("Course with ID $searchKey not found") }

            else -> courseRepository.findByCourseCode(searchKey as String)
                .orElseThrow { NotFoundException("Course with course code $searchKey not found") }
        }

        return with(course) {
            CourseDTOResponse(
                this.id!!,
                this.courseTitle,
                this.courseCode,
                this.unit
            )
        }
    }

    fun addCourse(courseInput: CourseDTORequest) : CourseDTOResponse {
        val optionalCourse: Optional<Course> = courseRepository.findByCourseCode(courseInput.courseCode)

        if(!validateCourseCode(courseInput.courseCode)){
            throw InvalidInputException("Course code: ${courseInput.courseCode} is in wrong format")
        }

        val courseEntity: Course = optionalCourse.getOrElse {
            Course.builder()
                .courseCode(courseInput.courseCode)
                .courseTitle(courseInput.courseTitle)
                .unit(courseInput.unit)
                .build()
                .also {
                    courseRepository.save(it)
                }

        }

        return courseEntity.let {
            CourseDTOResponse(
                it.id!!,
                it.courseTitle,
                it.courseCode,
                it.unit
            )
        }
    }


    fun deleteCourseByIdOrCourseCode(input: Any): ResponseEntity<Any> {
        val searchKey = checkInputType(input)

        val (getCourseFunction, deleteFunction) = when (searchKey) {
            is Int -> {
                { courseRepository.findById(searchKey) } to { courseRepository.deleteById(searchKey) }
            }
            is String -> {
                { courseRepository.findByCourseCode(searchKey) } to { courseRepository.deleteByCourseCode(searchKey) }
            }
            else -> null to null
        }

        val course: Course = getCourseFunction?.invoke()?.getOrElse {
            throw NotFoundException("Course not found")
        } ?: throw NotFoundException("Course not found")

        course.applications.clear()

        deleteFunction?.invoke()

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    fun updateCourseByIdOrCourseCode(input: Any, courseInput: CourseDTORequest): CourseDTOResponse {

        val course: Course = when (val searchKey = checkInputType(input)) {
            is Int -> courseRepository.findById(searchKey)
                .orElseThrow { NotFoundException("Course with ID $searchKey not found") }

            else -> courseRepository.findByCourseCode(searchKey as String)
                .orElseThrow { NotFoundException("Course with course code $searchKey not found") }
        }


        val updatedCourse = course.let {
            it.courseTitle= courseInput.courseTitle
            courseRepository.save(it)

            CourseDTOResponse(
                it.id!!,
                it.courseTitle,
                it.courseCode,
                it.unit
            )
        }

        return updatedCourse
    }

    private fun validateCourseCode(courseInput: String): Boolean {

        if (courseInput.split(" ").size != 2 ){
            return false
        }

        val (courseName : String, courseCode : String) = courseInput.split(" ")

        if (courseName.any { !it.isLetter() || it.isLowerCase()} || courseCode.any{ !it.isDigit()}){
            return false
        }

        return true
    }

}