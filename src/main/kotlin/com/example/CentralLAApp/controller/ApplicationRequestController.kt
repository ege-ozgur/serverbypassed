package com.example.CentralLAApp.controller

import com.example.CentralLAApp.dto.request.*
import com.example.CentralLAApp.dto.response.ApplicationRequestDTOResponse
import com.example.CentralLAApp.enums.UserRole
import com.example.CentralLAApp.service.ApplicationRequestService
import com.example.CentralLAApp.service.AuthorizationService
import com.example.CentralLAApp.service.UserService
import com.example.CentralLAApp.util.security.getId
import com.example.CentralLAApp.util.security.getUser
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Duration


@RestController
@RequestMapping("/api/v1/applicationRequest")
class ApplicationRequestController(
    val applicationRequestService: ApplicationRequestService,
    private val userService: UserService,
    private val authorizationService: AuthorizationService
) {

    @GetMapping
    fun getApplicationRequests(): Collection<ApplicationRequestDTOResponse> { //unused
        return applicationRequestService.getAllApplicationRequests()
    }

    @GetMapping("/{searchKey}")
    fun getApplicationRequestById(@PathVariable searchKey: Any): ApplicationRequestDTOResponse {
        return applicationRequestService.getApplicationRequestById(searchKey);
    }

    @PostMapping("/student")
    @ResponseBody
    fun addApplicationRequest(@RequestBody theApplicationRequest: ApplicationRequestDTORequest): ResponseEntity<Any> {
        val studentId = getId(UserRole.STUDENT)
        //val studentId = 2
        val applicationRequestDTO = applicationRequestService.addApplicationRequest(theApplicationRequest, studentId)

        return ResponseEntity.status(HttpStatus.CREATED).body(applicationRequestDTO)
    }

    @GetMapping("/student/{studentId}")
    fun getApplicationRequestByStudentId(
        @PathVariable studentId: Int,
        @PageableDefault(sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): Page<ApplicationRequestDTOResponse> {
        val user = getUser()
        authorizationService.validateAuthorizationToStudentInfo(user, studentId)
        return applicationRequestService.getApplicationRequestsByStudentId(studentId, false, pageable)
    }

    @PostMapping("/student/la_history")
    fun getStudentLaHistory(
        @RequestBody laHistoryRequest: LaHistoryRequest,
        @PageableDefault(sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable,
        ): Page<ApplicationRequestDTOResponse> {
        val user = getUser()
        authorizationService.validateAuthorizationToStudentInfo(user, laHistoryRequest.studentId)
        return applicationRequestService.getStudentLaHistory(laHistoryRequest, pageable)
    }

    @GetMapping("/accepted-application-requests/{studentId}") //unused
    fun getAcceptedApplicationRequestByStudentId(
        @PathVariable studentId: Int,
        @PageableDefault(sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): Page<ApplicationRequestDTOResponse> {
        return applicationRequestService.getApplicationRequestsByStudentId(studentId, true, pageable)
    }

    @PutMapping("/{searchKey}/status")
    fun updateStatus(
        @PathVariable searchKey: Any,
        @RequestBody status: StatusDTORequest
    ): ApplicationRequestDTOResponse {
        return applicationRequestService.updateStatus(searchKey, status)
    }


    @PutMapping("/status")
    fun updateStatusMultiple(
        @RequestBody statusList: List<MultipleStatusDTORequest>
    ): List<ApplicationRequestDTOResponse> {
        return applicationRequestService.updateStatusMultiple(statusList)
    }

    @GetMapping("/student/{studentId}/active") //unused
    fun getActiveApplicationRequestByStudentId(@PathVariable studentId: Int): Collection<ApplicationRequestDTOResponse> {
        return applicationRequestService.findByStudentForActiveApplicationRequests(studentId)
    }

    @DeleteMapping("/{searchKey}")
    fun deleteApplicationRequestById(@PathVariable searchKey: Any): ResponseEntity<Any> {
        val user = getUser()
        applicationRequestService.deleteApplicationRequestById(searchKey,user)
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .body("ApplicationRequest with ID $searchKey deleted successfully")
    }

    @PutMapping("/{searchKey}")
    fun acceptApplicationRequest(@PathVariable searchKey: Any): ApplicationRequestDTOResponse {
        return applicationRequestService.acceptApplicationRequest(searchKey)
    }

    @PutMapping("withdraw/{searchKey}")
    fun withdrawApplicationRequest(@PathVariable searchKey: Any): HttpStatus {
        val user = getUser()

        return applicationRequestService.withdrawApplicationRequest(searchKey, user)
    }


    @PutMapping("/student/update/{searchKey}")
    fun updateApplicationRequest(
        @PathVariable searchKey: Any,
        @RequestBody theApplicationRequest: ApplicationRequestDTORequest
    ): ResponseEntity<Any> {
        val studentId = getId(UserRole.STUDENT)
        val applicationRequestDTO =
            applicationRequestService.updateApplicationRequest(theApplicationRequest, searchKey, studentId)

        return ResponseEntity.status(HttpStatus.CREATED).body(applicationRequestDTO)
    }

    @PutMapping("/updateWorkHour/{searchKey}")
    fun updateApplicationRequest(
        @PathVariable searchKey: Long,
        @RequestParam duration: Duration
    ): ResponseEntity<Any> {
        return try {
            val applicationRequestDTO = applicationRequestService.updateAppReqWorkHour(searchKey, duration)
            ResponseEntity.status(HttpStatus.CREATED).body(applicationRequestDTO)
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.CONFLICT).body(e.message)
        }
    }


    @PostMapping("/student/checkEligibility/{applicationId}")
    @ResponseBody
    fun checkStudentEligibilityForApplication(@PathVariable applicationId: Long): ResponseEntity<Any> {
        val studentId = getId(UserRole.STUDENT)
        val response = applicationRequestService.checkStudentEligibilityForApplication(studentId, applicationId)

        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PutMapping("instructor/finalizeStatus/{searchKey}") //unused
    fun finalizeStatus(@PathVariable searchKey: Any): ApplicationRequestDTOResponse {
        return applicationRequestService.finalizeAppReqStatus(searchKey)
    }

    @PutMapping("/{applicationId}/accept-all")
    fun acceptAllRequests(@PathVariable applicationId: Long){
        val userId = getId(UserRole.INSTRUCTOR)
        return applicationRequestService.acceptAllRequests(applicationId, userId)
    }

    @PutMapping("/{applicationId}/reject-all")
    fun rejectAllRequests(@PathVariable applicationId: Long){
        val userId = getId(UserRole.INSTRUCTOR)
        return applicationRequestService.rejectAllRequests(applicationId, userId)
    }

    @PutMapping("/{applicationReqId}/commit")
    fun commitApplicationRequest(@PathVariable applicationReqId: Long): ApplicationRequestDTOResponse {
        val userId = getId(UserRole.STUDENT)
        //val userId = 2
        return applicationRequestService.commitApplicationRequest(applicationReqId, userId, true)
    }

    @PutMapping("/{applicationReqId}/uncommit")
    fun forgivenApplicationRequest(@PathVariable applicationReqId: Long): ApplicationRequestDTOResponse {
        val userId = getId(UserRole.STUDENT)
        return applicationRequestService.commitApplicationRequest(applicationReqId, userId, false)
    }

    @PutMapping("/{applicationReqId}/workHour")
    fun changeAppReqWorkHour(@PathVariable applicationReqId: Long, @RequestBody newWorkHour: ApplicationRequestChangeWorkHourRequest): ResponseEntity<String> {
        return try {
            val userId = getId(UserRole.STUDENT)
            applicationRequestService.changeAppReqWorkHour(applicationReqId, userId, newWorkHour.duration)
            ResponseEntity.status(HttpStatus.OK).body("Updated successfully.")
        } catch (e: Exception) {
            throw e
        }
    }

    @PutMapping("/instructor/resetCommitment/{applicationReqId}")
    fun resetCommitment(@PathVariable applicationReqId: Long): ApplicationRequestDTOResponse {
        val userId = getId(UserRole.INSTRUCTOR)
        return applicationRequestService.resetAppReqCommit(applicationReqId)
    }

    @PutMapping("/instructor/redFlag/{applicationReqId}")
    fun redFlagApplicationRequest(@PathVariable applicationReqId: Long): ApplicationRequestDTOResponse {
        return applicationRequestService.flagTheApplicationRequest(applicationReqId)
    }

    @PutMapping("/instructor/unRedFlag/{applicationReqId}")
    fun unRedFlagApplicationRequest(@PathVariable applicationReqId: Long): ApplicationRequestDTOResponse {
        return applicationRequestService.unflagTheApplicationRequest(applicationReqId)
    }


}