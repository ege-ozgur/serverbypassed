package com.example.CentralLAApp.controller

import com.example.CentralLAApp.dto.request.CoursesRequest
import com.example.CentralLAApp.dto.response.CurrentTranscriptStatusResponse
import com.example.CentralLAApp.enums.UserRole
import com.example.CentralLAApp.service.AuthorizationService
import com.example.CentralLAApp.service.FileService
import com.example.CentralLAApp.service.TranscriptService
import com.example.CentralLAApp.service.UserService
import com.example.CentralLAApp.util.security.getId
import com.example.CentralLAApp.util.security.getUser
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile


@RestController
@RequestMapping("/api/v1/transcript")
class FileController(
    private val fileService: FileService,
    private val transcriptService: TranscriptService,
    private val userService: UserService,
    private val authorizationService: AuthorizationService
    //private val transcriptFileRepository: TranscriptFileRepository
) {

    @PostMapping("/upload")
    fun uploadFile(@RequestParam("file") file: MultipartFile?): ResponseEntity<String> {
        return try {
            // You might want to validate file content type here as well
            val transcriptInfo: Pair<ByteArray, MutableMap<String, Any>> =  fileService.storeAndParseFile(file!!)
            val studentId = getId(UserRole.STUDENT)
            val user = getUser()
            //val studentId = 3
            val transcript = transcriptService.addTranscript(transcriptInfo, studentId, user)

            ResponseEntity.status(HttpStatus.OK).body("File uploaded successfully.")
        } catch (e: Exception) {
            // Handle exception
            throw e
        }
    }

    @GetMapping("/get-transcript")
    fun getTranscript(): ResponseEntity<Any> {
        return try {
            val studentId = getId(UserRole.STUDENT)

            ResponseEntity.status(HttpStatus.OK).body(transcriptService.getTranscriptByStudentId(studentId))
        } catch (e: Exception) {
            throw e;
        }
    }

    @GetMapping("/get-transcript/{term}")
    fun getTranscriptByTerm( @PathVariable("term") term: String): ResponseEntity<Any> {
        val studentId = getId(UserRole.STUDENT)
        return try {
            ResponseEntity.status(HttpStatus.OK).body(transcriptService.getTranscriptByStudentIdAndTerm(studentId, term))
        } catch (e: Exception) {
            throw e;
        }
    }

    @GetMapping("/get-current-transcript/{studentId}")
    fun getLastTranscript(@PathVariable studentId: Int): ResponseEntity<Any> {
        return try {
            val user = getUser()
            authorizationService.validateAuthorizationToStudentInfo(user, studentId)
            ResponseEntity.status(HttpStatus.OK).body(transcriptService.getLastTranscript(studentId))
        } catch (e: Exception) {
            throw e;
        }
    }

    @PostMapping("/course-grades/{studentId}")
    fun getCourseGrades(@RequestBody courses : CoursesRequest, @PathVariable studentId: Int): ResponseEntity<Any> {
        return try {
            val user = getUser()
            authorizationService.validateAuthorizationToStudentInfo(user, studentId)
            ResponseEntity.status(HttpStatus.OK).body(transcriptService.getCourseGrades(studentId ,courses))
        } catch (e: Exception) {
            throw e;
        }
    }

    @GetMapping("/current-transcript-status")
    fun getCurrentTranscriptStatus(): CurrentTranscriptStatusResponse {
        val studentId = getId(UserRole.STUDENT)
        return transcriptService.getCurrentTranscriptStatus(studentId)

    }


}