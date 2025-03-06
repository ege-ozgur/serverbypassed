package com.example.CentralLAApp.service

import com.example.CentralLAApp.dto.request.ApplicationRequestDTORequest
import com.example.CentralLAApp.dto.request.LaHistoryRequest
import com.example.CentralLAApp.dto.request.MultipleStatusDTORequest
import com.example.CentralLAApp.dto.request.StatusDTORequest
import com.example.CentralLAApp.dto.response.ApplicationRequestDTOResponse
import com.example.CentralLAApp.dto.response.CourseEligibilityResponse
import com.example.CentralLAApp.dto.response.EligibilityResponse
import com.example.CentralLAApp.entity.application.Answer
import com.example.CentralLAApp.entity.application.Application
import com.example.CentralLAApp.entity.application.ApplicationRequest
import com.example.CentralLAApp.entity.question.Question
import com.example.CentralLAApp.entity.transcript.CoursesAndGrades
import com.example.CentralLAApp.entity.transcript.Transcript
import com.example.CentralLAApp.entity.user.Instructor
import com.example.CentralLAApp.entity.user.Student
import com.example.CentralLAApp.entity.user.User
import com.example.CentralLAApp.enums.*
import com.example.CentralLAApp.exception.InvalidInputException
import com.example.CentralLAApp.exception.NotFoundException
import com.example.CentralLAApp.repository.ApplicationRepository
import com.example.CentralLAApp.repository.ApplicationRequestRepository
import com.example.CentralLAApp.repository.StudentRepository
import com.example.CentralLAApp.repository.TranscriptRepository
import com.example.CentralLAApp.service.helper.convertToApplicationRequestDTOResponse
import com.example.CentralLAApp.service.helper.verifyLong
import com.example.CentralLAApp.util.security.getUser
import com.example.CentralLAApp.util.security.validateAuthorizedInstructor
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime

@Service
class ApplicationRequestService(
    val applicationRequestRepository: ApplicationRequestRepository,
    val applicationRepository: ApplicationRepository,
    val studentRepository: StudentRepository,
    private val authorizationService: AuthorizationService,
    private val notificationService: NotificationService,
    private val transcriptRepository: TranscriptRepository,
) {


    fun getAllApplicationRequests(): Collection<ApplicationRequestDTOResponse> {
        return applicationRequestRepository.findAll().map {
            it.run { convertToApplicationRequestDTOResponse(this) }
        }
    }

    fun saveApplicationRequest(applicationRequest: ApplicationRequest): ApplicationRequest {
        return applicationRequestRepository.save(applicationRequest)
    }

    fun getApplicationRequestById(searchKey: Any): ApplicationRequestDTOResponse {
        val appId: Long = verifyLong(searchKey)
        val applicationRequest: ApplicationRequest = applicationRequestRepository.findById(appId).orElseThrow {
            NotFoundException("ApplicationRequest with ID $appId not found")
        }

        return convertToApplicationRequestDTOResponse(applicationRequest)
    }



    fun addApplicationRequest(theApplicationRequest: ApplicationRequestDTORequest, studentId: Int): ApplicationRequestDTOResponse {
        val now = LocalDateTime.now()


        val application = applicationRepository.findById(theApplicationRequest.applicationId).orElseThrow {
            NotFoundException("Application with ID ${theApplicationRequest.applicationId} not found")
        }

        val student = studentRepository.findById(studentId).orElseThrow {
            NotFoundException("Student with ID ${studentId} not found")
        }

        if (now.isAfter(application.lastApplicationDate)){
            throw InvalidInputException("The last application date is past. You can not apply to this application")
        }

        if ( applicationRequestRepository.checkStudentAlreadyHasApplicationRequest(student.userID, application.applicationId) ){
            throw InvalidInputException("Student already has an application request for this announcement.")
        }
        val transcripts = transcriptRepository.findByStudentId(student.userID)

        checkStudentEligibility(student, application, transcripts)

        validateAnswers(theApplicationRequest.answers.map{it.answer}.toMutableList(), application.questions.filter { it.questionId in theApplicationRequest.answers.map { it.questionId } }.toMutableList())


        val applicationRequestEntity: ApplicationRequest = ApplicationRequest.builder(
            application,
            student,
            ApplicationResult.IN_PROGRESS,
            ApplicationResult.IN_PROGRESS,
            transcripts.last()
        )
            .weeklyWorkHours(application.weeklyWorkHours)
            .build()
            .also {
                it.answers = theApplicationRequest.answers.map{
                        Answer(
                            it.questionId,
                            it.answer
                        )
                }.toMutableList()
                applicationRequestRepository.save(it) }

        removeStudentFromTheFollowers(application, student)
        notificationService.sendMultipleNotificationsAsync(
            interestedClients = application.authorizedInstructors,
            title = STUDENT_APPLIED_TITLE,
            description = "A new student ${student._name + " " + student.surname} has applied to ${application.course.courseCode} announcement for the term ${application.term}.",
            notificationType = NotificationType.STUDENT_STATUS_UPDATE,
            relation = NotificationRelationType.DIRECT
        )

        return convertToApplicationRequestDTOResponse(
            applicationRequestEntity,
            now
        )
    }

    private fun removeStudentFromTheFollowers(application: Application, student: Student) {
        val res = application.followers.remove(student)
        if (res) applicationRepository.save(application)
    }

    private fun checkStudentEligibility(student: Student, application: Application, transcripts: List<Transcript>, ) {
        if (transcripts.isEmpty()){
            throw InvalidInputException("You do not have any transcript currently")
        }
        val transcript = transcripts.last()
        val courseCode = application.course.courseCode

        val studentMainCourse = getCourse(courseCode, transcript)

        checkGrades(studentMainCourse?.grade, courseCode, application.minimumRequiredGrade, application.isInprogressAllowed, application.isNotTakenAllowed)

        application.previousCourseGrades.forEach {courseAndGrade ->

            val studentDesiredCourse = getCourse(courseAndGrade.course.courseCode, transcript)
            checkGrades(studentDesiredCourse?.grade, courseAndGrade.course.courseCode, courseAndGrade.desiredLetterGrade, courseAndGrade.isInprogressAllowed, courseAndGrade.isNotTakenAllowed)
        }

    }

    fun validateWorkHourLimit(userID: Int, term: String, newWorkHour: Duration, currentWorkHour: Duration) {
        val appReqs = applicationRequestRepository.findWorkingApplicationRequestsByTermAndStudentId(userID, term)
        val totalWorkHours = appReqs.sumOf { it.weeklyWorkHours.toHours() } - currentWorkHour.toHours()

        if (newWorkHour.toHours() + totalWorkHours > WORK_HOUR_LIMIT){
            if(totalWorkHours.toInt() == WORK_HOUR_LIMIT){
                throw InvalidInputException("You have reached the maximum total work hour (10) for ${term}.")
            }
            throw InvalidInputException("You can not commit this application. You have only ${WORK_HOUR_LIMIT - totalWorkHours} available remaining weekly work hours.")
        }
    }

    private fun getCourse(courseCode: String, transcript : Transcript): CoursesAndGrades? {
        return transcript.courses.find { it.courseCode == courseCode }
            //?: throw InvalidInputException("Student does not have the course $courseCode in the transcript")



    }

    private fun getCourseForEligibility(courseCode: String, transcript : Transcript): CoursesAndGrades? {
        return transcript.courses.find { it.courseCode == courseCode }
    }
    private fun checkGrades(studentGrade : LetterGrade?, courseCode : String, desiredGrade : LetterGrade?, isInProgressAllowed : Boolean?, isNotTakenAllowed : Boolean?) {
        if (studentGrade == null){
            if(isNotTakenAllowed != true){
                throw InvalidInputException("Student is not eligible to apply this application. For the course ${courseCode}, student has not taken the course yet")
            }
        }

        else if (studentGrade == LetterGrade.IP) {
            if (isInProgressAllowed != true){
                throw InvalidInputException("Student is not eligible to apply this application. For the course ${courseCode}, in progress applicants are not allowed")
            }
        } else {
            if (desiredGrade == null){
                return
            }
            if (studentGrade.compareTo(desiredGrade) == 1){
                throw InvalidInputException("Student is not eligible to apply this application. For the course ${courseCode}, required minimum grade is ${desiredGrade}, but found ${studentGrade.value}")
            }
        }
    }
    private fun checkGradesForEligibilityResponse(
        studentGrade: LetterGrade?,
        courseCode: String,
        desiredGrade: LetterGrade?,
        isInProgressAllowed: Boolean?,
        isNotTakenAllowed: Boolean?
    ): CourseEligibilityResponse {
        val isStuTakenCourse = studentGrade != null && studentGrade != LetterGrade.IP
        val isNotTaken = studentGrade == null
        val isIP = studentGrade == LetterGrade.IP
        //val isEligible = isStuTakenCourse || isNotTakenAllowed==true ||
                //isIP && isInProgressAllowed == true || desiredGrade?.let { studentGrade!! <= it } ?: true
        // for isEligible, if student has not taken the course, do not check the desired grade
        val isEligible = when{
            isStuTakenCourse -> desiredGrade?.let { studentGrade!! <= it } ?: true
            isNotTaken -> isNotTakenAllowed==true
            isIP -> isInProgressAllowed == true
            else -> false
        }


        val eligibilityInfo = when {
            !isStuTakenCourse && !isIP && isNotTakenAllowed == false -> STUDENT_NOT_TAKEN_COURSE
            desiredGrade == null -> NO_REQUIRED_GRADE
            isIP && isInProgressAllowed == true -> STUDENT_IP_ELIGIBLE
            isNotTaken && isNotTakenAllowed == true -> STUDENT_NOT_TAKEN_COURSE_ELIGIBLE
            isIP -> STUDENT_IP_NOT_ELIGIBLE
            isEligible -> STUDENT_GRADE_ELIGIBLE
            else -> STUDENT_GRADE_NOT_ELIGIBLE
        }

        return CourseEligibilityResponse(
            courseCode = courseCode,
            requiredLetterGrade = desiredGrade,
            studentGrade = studentGrade,
            isEligible = isEligible,
            eligibilityInfo = eligibilityInfo,
            isInProgressAllowed = isInProgressAllowed == true,
            isNotTakenAllowed = isNotTakenAllowed == true

        )
    }

    private fun validateAnswers(answers: MutableList<String>, questions: MutableList<Question>) {
        //validateAnswerCount(answers, questions)

        if (answers.any { it.isBlank() }) {
            //throw InvalidInputException("One or more answers are blank.")
        }

        questions.zip(answers).forEach { (question, answer) ->
            validateAnswerType(answer, question)
        }
    }

    private fun validateAnswerCount(answers: List<String>, questions: List<Question>) {
        val dependentQuestions: MutableMap<String, MutableList<Pair<Int, Int>>> = mutableMapOf()
        var idx = 0

        questions.forEach { question ->
            if (question.isConditionalQuestion == true) {
                question.choices?.forEachIndexed { choiceIndex, choice ->
                    val conditionallyOpen = choice.conditionallyOpen
                    if (conditionallyOpen != null) {
                        val map = dependentQuestions.getOrPut( conditionallyOpen) { mutableListOf()}
                        map.add( idx to choiceIndex)
                    }
                }
            }
            idx += 1
        }

        val diff = answers.size - questions.size

        when {
            diff > 0 -> throw InvalidInputException("There are $diff more answers than questions.")
            diff < 0 -> throw InvalidInputException("There are ${-diff} less answers than questions.")
        }
    }

    private fun validateAnswerType(answer: String, question: Question) {
        when (question.type) {
            QuestionType.MULTIPLE_CHOICE -> validateMultipleChoiceAnswer(answer, question)
            QuestionType.NUMERIC -> validateNumericAnswer(answer)
            else -> Unit
        }
    }

    private fun validateNumericAnswer(answerInput: String) {
        answerInput.toLongOrNull() ?: throw InvalidInputException("Answer: $answerInput is in the wrong format.")
    }

    private fun validateMultipleChoiceAnswer(answerInput: String, question: Question) {
        answerInput.toIntOrNull()
            ?: throw InvalidInputException("Answer: $answerInput is in the wrong format.")

        if (answerInput.groupBy { it }.any { it.value.size > 1 } )
            throw InvalidInputException("Answer contains duplicates")

        if (!question.allowMultipleAnswers && answerInput.length > 1) {
            throw InvalidInputException("Multiple answers are not allowed for this question.")
        }

        answerInput.forEach { ans ->
            if (ans.digitToInt() < 0 || ans.digitToInt() >= question.choices!!.size) {
                throw InvalidInputException("Answer to multiple-choice question is out of range.")
            }
        }

    }


    fun getApplicationRequestsByStudentId(searchKey: Any, filterAccepted: Boolean, pageable: Pageable): Page<ApplicationRequestDTOResponse> {
        val studentId: Int = verifyLong(searchKey).toInt()
        val student = studentRepository.findById(studentId).orElseThrow {
            NotFoundException("Student with ID $studentId not found")
        }

        val applicationRequests: Page<ApplicationRequest> =if (filterAccepted) applicationRequestRepository.getApplicationRequestsByStudentAndStatusOrderByCreatedAtDesc(student, ApplicationResult.ACCEPTED, pageable)  else applicationRequestRepository.getApplicationRequestsByStudentOrderByCreatedAtDesc(student, pageable)

        return applicationRequests.map {
            it.run { convertToApplicationRequestDTOResponse(this) }
        }
    }

    fun updateStatus(applicationReqId: Any, to: StatusDTORequest): ApplicationRequestDTOResponse {
        val appId: Long = verifyLong(applicationReqId)
        val applicationRequest = applicationRequestRepository.findById(appId).orElseThrow {
            NotFoundException("ApplicationRequest with ID $appId not found")
        }
        val user = getUser()
        authorizationService.validateAuthorizationToStudentInfo(user, applicationRequest.student!!.userID)

        //applicationRequest.status = ApplicationResult.valueOf(to)
        if (applicationRequest.status == ApplicationResult.ACCEPTED){

        }
        applicationRequest.statusIns = to.status

        val entity = applicationRequestRepository.save(applicationRequest)
        return convertToApplicationRequestDTOResponse(entity)
    }

    fun finalizeAppReqStatus(applicationReqId: Any): ApplicationRequestDTOResponse {
        val appId: Long = verifyLong(applicationReqId)
        val applicationRequest = applicationRequestRepository.findById(appId).orElseThrow {
            NotFoundException("ApplicationRequest with ID $appId not found")
        }
        applicationRequest.status = applicationRequest.statusIns

        val entity = applicationRequestRepository.save(applicationRequest)
        return convertToApplicationRequestDTOResponse(entity)
    }

    fun acceptApplicationRequest(applicationReqId: Any): ApplicationRequestDTOResponse {
        val appId: Long = verifyLong(applicationReqId)
        val applicationRequest = applicationRequestRepository.findById(appId).orElseThrow {
            NotFoundException("ApplicationRequest with ID $appId not found")
        }
        applicationRequest.statusIns = ApplicationResult.ACCEPTED

        val application:Application? = applicationRequest.application
        val student: Student? = applicationRequest.student
        val previousInstructors : Collection<Instructor>? = student?.previousInstructors
        val Instructorss : Collection<Instructor>? = application?.authorizedInstructors
        Instructorss?.map { it ->
            if (previousInstructors?.contains(it) == false) {
                student?.previousInstructors?.add(it)
            }
        }

        val entity = applicationRequestRepository.save(applicationRequest)
        return convertToApplicationRequestDTOResponse(entity)

    }

    fun findByStudentForActiveApplicationRequests(studentId: Int): List<ApplicationRequestDTOResponse> {
        val student = studentRepository.findById(studentId).orElseThrow {
            NotFoundException("Student with ID $studentId not found")
        }
        val applicationRequests: List<ApplicationRequest> =
            applicationRequestRepository.findAllByStatusIsNotAndStatusIsNotAndStudent(
                ApplicationResult.WITHDRAWN,
                ApplicationResult.REJECTED,
                student
            )

        return applicationRequests.map {
            it.run { convertToApplicationRequestDTOResponse(this) }
        }
    }

    fun deleteApplicationRequestById(searchKey: Any, user: User) {
        val appId: Long = verifyLong(searchKey)
        val applicationRequest: ApplicationRequest = applicationRequestRepository.findById(appId).orElseThrow {
            NotFoundException("ApplicationRequest with ID $appId not found")
        }

        authorizationService.validateAuthorizationToStudentInfo(user, applicationRequest.student!!.userID)
        applicationRequestRepository.delete(applicationRequest)

    }

    fun updateApplicationRequest(theApplicationRequest: ApplicationRequestDTORequest, searchKey: Any, studentId: Int): ApplicationRequestDTOResponse {
        val appId: Long = verifyLong(searchKey)
        val now =  LocalDateTime.now()


        var applicationRequest: ApplicationRequest = applicationRequestRepository.findById(appId).orElseThrow {
            NotFoundException("ApplicationRequest with ID $appId not found")
        }

        //authorizationService.validateAuthorizationToStudentInfo(getUser(), applicationRequest.student!!.userID)

        val applicationId:Long = theApplicationRequest.applicationId
        val application = applicationRepository.findById(applicationId).orElseThrow {
            NotFoundException("Application with ID ${theApplicationRequest.applicationId} not found")
        }

        if (now.isAfter(application.lastApplicationDate)){
            throw InvalidInputException("Last application date is past. You can not edit this application request.")
        }


        val student = studentRepository.findById(studentId).orElseThrow {
            NotFoundException("Student with ID ${studentId} not found")
        }

        val applicationRequestsOfStudent = student.applicationRequests

         if (applicationRequest.status != ApplicationResult.IN_PROGRESS) {
            throw InvalidInputException("Application cannot be edited.")
        }

        else if (applicationRequestsOfStudent.any { it.application == application }) {
            

             validateAnswers(theApplicationRequest.answers.map { it.answer }.toMutableList(), application.questions.filter { it.questionId in theApplicationRequest.answers.map { it.questionId } }.toMutableList())

             applicationRequest.answers = theApplicationRequest.answers.map{
                 Answer(
                     it.questionId,
                     it.answer
                 )
             }.toMutableList()
             applicationRequest.updatedAt = now

            val entity = applicationRequestRepository.save(applicationRequest)
            return convertToApplicationRequestDTOResponse(entity)
        }

        else{
            throw InvalidInputException("Student does not have an application request for this announcement.")
        }

    }

    fun updateAppReqWorkHour(appReqId: Long, newWorkHour: Duration) {
        val user = getUser()
        var applicationRequest: ApplicationRequest = applicationRequestRepository.findById(appReqId).orElseThrow {
            NotFoundException("ApplicationRequest with ID $appReqId not found")
        }

        authorizationService.validateAuthorizationToStudentInfo(user, applicationRequest.student!!.userID)

        if (applicationRequest.committed){
            validateWorkHourLimit(applicationRequest.student!!.userID, applicationRequest.application!!.term, newWorkHour,  applicationRequest.weeklyWorkHours)
        }


        applicationRequest.weeklyWorkHours = newWorkHour
        applicationRequestRepository.save(applicationRequest)
    }

    fun withdrawApplicationRequest(applicationReqId: Any, user: User): HttpStatus {
        val appId: Long = verifyLong(applicationReqId)
        val applicationRequest = applicationRequestRepository.findById(appId).orElseThrow {
            NotFoundException("ApplicationRequest with ID $appId not found")
        }

        authorizationService.validateAuthorizationToStudentInfo(user, applicationRequest.student!!.userID)


        applicationRequestRepository.delete(applicationRequest)
        return HttpStatus.OK

    }

    fun checkStudentEligibilityForApplication(studentId: Int, applicationId: Long): EligibilityResponse {
        val now = LocalDateTime.now()


        val application = applicationRepository.findById(applicationId).orElseThrow {
            NotFoundException("Application with ID ${applicationId} not found")
        }

        val student = studentRepository.findById(studentId).orElseThrow {
            NotFoundException("Student with ID ${studentId} not found")
        }

        if (now.isAfter(application.lastApplicationDate)){
            throw InvalidInputException("The last application date is past. You can not apply to this application")
        }

        val applicationRequestsOfStudent = student.applicationRequests

        if (applicationRequestsOfStudent.any { it.application == application}) {
            throw InvalidInputException("Student already has an application request for this announcement.")
        }

        if (student.transcripts.isEmpty()){
            throw InvalidInputException("You do not have any transcript currently")
        }


        val transcript = student.transcripts.last()
        val courseCode = application.course.courseCode


        val response = mutableListOf<CourseEligibilityResponse>()
        var eligibleCount = 0

        val studentMainCourse = getCourseForEligibility(courseCode, transcript)
        val mainCourseEligibility = checkGradesForEligibilityResponse(studentMainCourse?.grade, courseCode, application.minimumRequiredGrade, application.isInprogressAllowed, application.isNotTakenAllowed)

        response.add(mainCourseEligibility)

        if (mainCourseEligibility.isEligible){
            eligibleCount += 1
        }

        application.previousCourseGrades.forEach {courseAndGrade ->
            val studentDesiredCourse = getCourseForEligibility(courseAndGrade.course.courseCode, transcript)
            val courseEligibility = checkGradesForEligibilityResponse(studentDesiredCourse?.grade, courseAndGrade.course.courseCode, courseAndGrade.desiredLetterGrade, courseAndGrade.isInprogressAllowed, courseAndGrade.isNotTakenAllowed)

            response.add(courseEligibility)

            if (courseEligibility.isEligible){
                eligibleCount += 1
            }
        }

        return EligibilityResponse(
            eligibility = response,
            totalCourseCount = response.size,
            eligibleCourseCount = eligibleCount,
            notEligibleCourseCount = response.size - eligibleCount,
            isStudentEligible = response.size == eligibleCount,
            questionCount = application.questions.size

        )
    }

    fun existsByInstructorIdAndStudentId(instructorId: Int, studentId: Int) = applicationRequestRepository.checkByInstructorIdAndStudentId(instructorId, studentId)

    @Transactional
    fun acceptAllRequests(applicationId: Long, userId: Int) {
        val application = applicationRepository.findById(applicationId).orElseThrow {
            NotFoundException("Application with ID $applicationId not found")
        }

        validateAuthorizedInstructor(application.authorizedInstructors, userId)

        application.applicationRequests.forEach {
            it.statusIns = ApplicationResult.ACCEPTED
        }

        applicationRepository.save(application)
    }

    @Transactional
    fun rejectAllRequests(applicationId: Long, userId: Int) {
        val application = applicationRepository.findById(applicationId).orElseThrow {
            NotFoundException("Application with ID $applicationId not found")
        }

        validateAuthorizedInstructor(application.authorizedInstructors, userId)

        application.applicationRequests.forEach {
            it.statusIns = ApplicationResult.REJECTED
        }

        applicationRepository.save(application)
    }

    fun getStudentLaHistory(laHistoryRequest: LaHistoryRequest, pageable: Pageable): Page<ApplicationRequestDTOResponse> {
        studentRepository.findById(laHistoryRequest.studentId).orElseThrow {
            NotFoundException("Student not found")
        }
        val application = applicationRepository.findById(laHistoryRequest.applicationId).orElseThrow {
            NotFoundException("Application not found")
        }

        val applicationRequests: Page<ApplicationRequest> = applicationRequestRepository.getStudentLaHistory(laHistoryRequest.studentId, application.course.courseCode, application.term, pageable)

        return applicationRequests.map {
            it.run { convertToApplicationRequestDTOResponse(this) }
        }
    }

    companion object {
        const val NO_REQUIRED_GRADE = "No required grade for this course."
        const val STUDENT_GRADE_NOT_ELIGIBLE  = "Your grade is lower than the required grade."
        const val STUDENT_GRADE_ELIGIBLE  = "Your grade is eligible for this course."
        const val STUDENT_IP_ELIGIBLE = "The application allows in progress applicants for this course"
        const val STUDENT_IP_NOT_ELIGIBLE = "The application does not allow in progress applicants for this course"
        const val STUDENT_NOT_TAKEN_COURSE = "Your current transcript indicates that you haven't taken the course yet."
        const val STUDENT_NOT_TAKEN_COURSE_ELIGIBLE = "The application allows applicants who have not taken the course yet."


        const val STUDENT_APPLIED_TITLE = "New Student Applied"
        const val WORK_HOUR_LIMIT = 10

    }

    fun commitApplicationRequest(applicationReqId: Any, studentId: Int, decision:Boolean): ApplicationRequestDTOResponse {
        val appId: Long = verifyLong(applicationReqId)
        val applicationRequest = applicationRequestRepository.findById(appId).orElseThrow {
            NotFoundException("ApplicationRequest with ID $appId not found")
        }
        val user = getUser()
        authorizationService.validateAuthorizationToStudentInfo(user, applicationRequest.student!!.userID)

        if (decision){
            validateWorkHourLimit(
                studentId,
                applicationRequest.application!!.term,
                applicationRequest.weeklyWorkHours,
                Duration.ofHours(0L)
            )
            applicationRequest.committed = true
        } else {
            applicationRequest.forgiven = true
        }


        val entity = applicationRequestRepository.save(applicationRequest)
        return convertToApplicationRequestDTOResponse(entity)
    }

    fun updateStatusMultiple(request: List<MultipleStatusDTORequest>): List<ApplicationRequestDTOResponse> {


        val allAppReq = applicationRequestRepository.findAllById(request.map { it.appReqId })
        if (allAppReq.size != request.size){
            throw NotFoundException("At least one of the application request is not found.")
        }

        val user = getUser()
        val requestMap = request.associateBy { it.appReqId }

        allAppReq.forEach {
            authorizationService.validateAuthorizationToStudentInfo(user, it.student!!.userID)
            it.statusIns = requestMap[it.applicationRequestId]!!.status
        }


        val updatedAppReqs = applicationRequestRepository.saveAll(allAppReq)
        return updatedAppReqs.map { convertToApplicationRequestDTOResponse(it) }
    }

    fun changeAppReqWorkHour(applicationReqId: Long, userId: Int, newWorkHour: Duration) {
        val applicationRequest = applicationRequestRepository.findById(applicationReqId).orElseThrow {
            NotFoundException("ApplicationRequest with ID $applicationReqId not found")
        }

        val user = getUser()
        authorizationService.validateAuthorizationToStudentInfo(user, applicationRequest.student!!.userID)

        validateWorkHourLimit(
            userId,
            applicationRequest.application!!.term,
            newWorkHour,
            applicationRequest.weeklyWorkHours
        )

        applicationRequest.weeklyWorkHours = newWorkHour

        applicationRequestRepository.save(applicationRequest)

    }

    fun resetAppReqCommit(applicationReqId: Any): ApplicationRequestDTOResponse {
        val appId: Long = verifyLong(applicationReqId)
        val applicationRequest = applicationRequestRepository.findById(appId).orElseThrow {
            NotFoundException("ApplicationRequest with ID $appId not found")
        }
        val user = getUser()
        authorizationService.validateAuthorizationToStudentInfo(user, applicationRequest.student!!.userID)

        applicationRequest.committed = false
        applicationRequest.forgiven = false

        val entity = applicationRequestRepository.save(applicationRequest)
        return convertToApplicationRequestDTOResponse(entity)
    }

    fun flagTheApplicationRequest(applicationReqId: Any): ApplicationRequestDTOResponse {
        val appId: Long = verifyLong(applicationReqId)
        val applicationRequest = applicationRequestRepository.findById(appId).orElseThrow {
            NotFoundException("ApplicationRequest with ID $appId not found")
        }
        val user = getUser()
        authorizationService.validateAuthorizationToStudentInfo(user, applicationRequest.student!!.userID)

        applicationRequest.redFlagged = true

        val entity = applicationRequestRepository.save(applicationRequest)
        return convertToApplicationRequestDTOResponse(entity)
    }

    fun unflagTheApplicationRequest(applicationReqId: Any): ApplicationRequestDTOResponse {
        val appId: Long = verifyLong(applicationReqId)
        val applicationRequest = applicationRequestRepository.findById(appId).orElseThrow {
            NotFoundException("ApplicationRequest with ID $appId not found")
        }
        val user = getUser()
        authorizationService.validateAuthorizationToStudentInfo(user, applicationRequest.student!!.userID)

        applicationRequest.redFlagged = false

        val entity = applicationRequestRepository.save(applicationRequest)
        return convertToApplicationRequestDTOResponse(entity)
    }




}


