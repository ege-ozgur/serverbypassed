package com.example.CentralLAApp.controller

import com.example.CentralLAApp.dto.ApplicationStatusUpdateDTO
import com.example.CentralLAApp.dto.request.ApplicationDTORequest
import com.example.CentralLAApp.dto.request.MailDTORequest
import com.example.CentralLAApp.dto.response.ApplicationDTOResponse
import com.example.CentralLAApp.dto.response.ApplicationRequestDTOResponse
import com.example.CentralLAApp.dto.response.ApplicationRequestResponse
import com.example.CentralLAApp.enums.UserRole
import com.example.CentralLAApp.service.ApplicationService
import com.example.CentralLAApp.service.helper.verifyLong
import com.example.CentralLAApp.util.security.getId
import com.example.CentralLAApp.util.security.getUser
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
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
@RequestMapping("/api/v1/applications")
class ApplicationController (val applicationService: ApplicationService) {


    @GetMapping
    fun getApplications(
        @PageableDefault(sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): Page<ApplicationDTOResponse> {
        val user = getUser()
        return applicationService.getAllApplications(user, pageable)
    }

    @GetMapping("/{searchKey}")
    fun getApplicationById(@PathVariable searchKey: Any): ApplicationDTOResponse {
        val user = getUser()
        return applicationService.getApplicationById(searchKey, user);
    }

    @GetMapping("/{searchKey}/applicationRequests")
    fun getApplicationRequestsForApplication(@PathVariable searchKey: Any): ApplicationRequestResponse {
        val userId = getId(UserRole.INSTRUCTOR)
        return applicationService.getApplicationRequestsForApplication(searchKey, userId)
    }

    @GetMapping("/course/{searchKey}")
    fun getAllApplicationsByCourseCode(@PathVariable searchKey: String) : Collection<ApplicationDTOResponse>{
        return applicationService.getAllApplicationsByCourseCode(searchKey)
    }

    @PostMapping
    fun addApplication(@RequestBody theApplication: ApplicationDTORequest): ResponseEntity<Any> {
        val userId = getId(UserRole.INSTRUCTOR)
        //val userId = 2
        val applicationDTO : ApplicationDTOResponse = applicationService.addApplication(theApplication, userId)

        return ResponseEntity.status(HttpStatus.CREATED).body(applicationDTO)
    }

    @PutMapping("/{searchKey}/status")
    fun changeApplicationStatus(@PathVariable searchKey: Any ,@RequestBody newStatus : ApplicationStatusUpdateDTO): ApplicationDTOResponse {
        val userId = getId(UserRole.INSTRUCTOR)
        return applicationService.changeApplicationStatus(searchKey,newStatus.to, userId)
    }

    @DeleteMapping("/{searchKey}")
    fun deleteApplicationById(@PathVariable searchKey: Any): ResponseEntity<Any> {
        val user = getUser()
        return  applicationService.deleteApplicationById(searchKey, user)
    }

    @PutMapping("/{searchKey}")
    fun updateApplication(@PathVariable searchKey: Any, @RequestBody applicationInput: ApplicationDTORequest): ApplicationDTOResponse {
        val user = getUser()
        return applicationService.updateApplicationById(searchKey, applicationInput, user)
    }

    @GetMapping("/instructor") // Unused
    fun getApplicationsByInstructor(pageable: Pageable):Page<ApplicationDTOResponse>{
        val instructorId = getId(UserRole.INSTRUCTOR)
        return applicationService.getApplicationsByAuthorizedInstructor(instructorId, pageable)
    }

    @PutMapping("student/{searchKey}/followers/add") // Unused
    fun addFollowerToApplication(@PathVariable searchKey: Any): ApplicationDTOResponse {
        val appId = verifyLong(searchKey)
        return applicationService.addFollowerToApplication(appId)
    }

    @PutMapping("student/{searchKey}/followers/remove")// Unused
    fun removeFollowerFromApplication(@PathVariable searchKey: Any): ApplicationDTOResponse {
        val appId = verifyLong(searchKey)
        return applicationService.removeFollowerFromApplication(appId)
    }

    @GetMapping("/byFollowers")// Unused
    fun getApplicationsByFollower():Collection<ApplicationDTOResponse>{
        return applicationService.getApplicationsByFollower()
    }

    @PutMapping("/{searchKey}/finalizeStatus")
    fun finalizeApplicationStatus(@PathVariable searchKey: Any, @RequestBody mailInput: MailDTORequest): List<ApplicationRequestDTOResponse> {
        val instructorId = getId(UserRole.INSTRUCTOR)

        return applicationService.finalizeAppReqsStatus(searchKey, mailInput, instructorId)
    }

    @PutMapping("/{searchKey}/mailUpdate") // unused
    fun updateApplicationMail(@PathVariable searchKey: Any, @RequestBody mailInput: MailDTORequest): ApplicationDTOResponse {
        val userId = getId(UserRole.INSTRUCTOR)
        return applicationService.updateApplicationMail(searchKey, mailInput, userId)
    }


}