package com.example.CentralLAApp.service

import com.example.CentralLAApp.dto.request.ApplicationDTORequest
import com.example.CentralLAApp.dto.request.CourseDTORequest
import com.example.CentralLAApp.dto.request.MailDTORequest
import com.example.CentralLAApp.dto.request.QuestionDTORequest
import com.example.CentralLAApp.dto.response.*
import com.example.CentralLAApp.entity.application.Application
import com.example.CentralLAApp.entity.application.ApplicationCourseGrade
import com.example.CentralLAApp.entity.course.Course
import com.example.CentralLAApp.entity.question.Choice
import com.example.CentralLAApp.entity.question.QAndC
import com.example.CentralLAApp.entity.question.Question
import com.example.CentralLAApp.entity.user.Instructor
import com.example.CentralLAApp.entity.user.User
import com.example.CentralLAApp.enums.*
import com.example.CentralLAApp.exception.AlreadyExistsException
import com.example.CentralLAApp.exception.InvalidInputException
import com.example.CentralLAApp.exception.NotFoundException
import com.example.CentralLAApp.repository.*
import com.example.CentralLAApp.service.helper.*
import com.example.CentralLAApp.util.security.getUser
import com.example.CentralLAApp.util.security.validateAuthorizedInstructor
import com.google.gson.JsonParser
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrElse


@Service
class ApplicationService(
    val applicationRepository: ApplicationRepository,
    val courseRepository: CourseRepository,
    val instructorRepository: InstructorRepository,
    val applicationCourseGradeRepository: ApplicationCourseGradeRepository,
    private val coursesService: CoursesService,
    private val applicationRequestService: ApplicationRequestService,
    private val notificationService: NotificationService,
    private val studentRepository: StudentRepository,
    private val httpService: HTTPService,
    private val questionRepository: QuestionRepository,
    private val userRepository: UserRepository
) {
    companion object {
        const val APPLICATION_ADDED_TITLE = "New Announcement"
        const val APPLICATION_DELETED_TITLE = "Announcement Deleted"
        const val APPLICATION_UPDATED_TITLE = "Announcement Updated"
        const val APPLICATION_REQUEST_STATUS_UPDATED_TITLE = "Application Status Updated"
        const val DEFAULT_ACCEPT_MAIL = "<p>Dear [:fullname:],</p><p><br></p><p>Congratulations, you have successfully passed the first stage of the [:course:] LAship application process!&nbsp;</p><p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</p><p>Though, your LAship is not guaranteed yet, since we need to know your personal course schedule to distribute the LAs to the recitation slots. Please check your emails regularly for the next steps.</p><p><br></p><p>Also, check this <a href=\"http://localhost:3000/build/commit\" rel=\"noopener noreferrer\" target=\"_blank\">link</a> to commit your LAship application.</p><p><br></p><p>Best,</p><p>[:course:] Instructors</p>"
        const val DEFAULT_REJECT_MAIL = "<p><span style=\"color: rgb(34, 34, 34);\">Dear </span>[:fullname:]<span style=\"color: rgb(34, 34, 34);\">,</span></p><p><br></p><p class=\"ql-align-justify\"><span style=\"color: rgb(34, 34, 34);\">Thank you for applying to </span>[:course:]<span style=\"color: rgb(34, 34, 34);\"> LAship for the upcoming semester. We regret to inform you that we will pursue the other candidates.&nbsp;</span></p><p class=\"ql-align-justify\"><br></p><p class=\"ql-align-justify\"><span style=\"color: rgb(34, 34, 34);\">We received a huge amount of applications, and only a small portion of the applicants have been qualified to the next stage after a preselection process.&nbsp;</span></p><p class=\"ql-align-justify\"><br></p><p class=\"ql-align-justify\"><span style=\"color: rgb(34, 34, 34);\">We hope that we can work together in the forthcoming semesters.&nbsp;</span></p><p><br></p><p><span style=\"color: rgb(34, 34, 34);\">Best,</span></p><p>[:course:]<span style=\"color: rgb(34, 34, 34);\"> Instructors</span></p>"
    }

    fun getAllApplications(user: User, pageable: Pageable): Page<ApplicationDTOResponse> {
        // Ensure the user is persistent
        val persistentUser = userRepository.findById(user.userID)
            .orElseThrow { NotFoundException("User with id ${user.userID} not found") }

        val applicationsPage = applicationRepository.findAllExcludingApplicants(pageable, persistentUser.userID)
        val followingApplicationsSet = applicationRepository.getApplicationsByFollowersContains(persistentUser)
            .map { it.applicationId }
            .toHashSet()

        val applicationsDTO = applicationsPage.content.map { application ->
            val eligibility = if (persistentUser.role == UserRole.STUDENT) {
                determineStudentEligibility(persistentUser, application)
            } else null
            val isFollowing = application.applicationId in followingApplicationsSet
            convertToApplicationDTOResponse(application, isEligible = eligibility, isFollowing = isFollowing)
        }
        return PageImpl(applicationsDTO, pageable, applicationsPage.totalElements)
    }



    private fun determineStudentEligibility(user: User, application: Application): Eligibility? {
        return try {
            val eligibilityResult = applicationRequestService.checkStudentEligibilityForApplication(user.userID, application.applicationId)
            if (eligibilityResult.isStudentEligible) {
                Eligibility.ELIGIBLE
            } else {
                Eligibility.NOT_ELIGIBLE
            }
        } catch (e: Exception) {
            when (e.message) {
                "The last application date is past. You can not apply to this application" -> Eligibility.DEADLINE_PASSED
                "You do not have any transcript currently" -> Eligibility.NO_TRANSCRIPT
                else -> null
            }
        }
    }
    fun getApplicationById(searchKey: Any, user: User): ApplicationDTOResponse {
        val isStudent = user.role == UserRole.STUDENT
        val appId: Long = verifyLong(searchKey)
        val application: Application = applicationRepository.findById(appId).orElseThrow {
            NotFoundException("Application with ID $appId not found")
        }
        val eligibility = if (isStudent) {
            try {
                if(applicationRequestService.checkStudentEligibilityForApplication(user.userID, appId).isStudentEligible){
                    Eligibility.ELIGIBLE
                }else{
                    Eligibility.NOT_ELIGIBLE
                }

            } catch (e: Exception){
                if (e.message == "The last application date is past. You can not apply to this application"){
                    Eligibility.DEADLINE_PASSED
                } else if (e.message == "You do not have any transcript currently"){
                    Eligibility.NO_TRANSCRIPT
                } else{
                    null
                }
            }
        }else null

        return convertToApplicationDTOResponse(application, isEligible = eligibility)
    }

    fun addApplication(theApplication: ApplicationDTORequest, userId: Int): ApplicationDTOResponse {

        val now = LocalDateTime.now()
        val currentUser = getUser()  // might be a dummy or security context instance
        val user = userRepository.findById(currentUser.userID).orElseThrow {
            NotFoundException("User with id ${currentUser.userID} not found")
        }

        val course: Course = courseRepository.findByCourseCode(theApplication.courseCode).getOrElse {
            val courseResponse = coursesService.addCourse(
                CourseDTORequest(
                    "",
                    theApplication.courseCode,
                    ""
                )
            )

            courseRepository.findById(courseResponse.id).get()
        }

        if (theApplication.authorizedInstructors.isEmpty()) {
            throw InvalidInputException("There should be at least one authorized instruction for the application")
        }

        val instructorsList: MutableList<Instructor> =
            theApplication.authorizedInstructors.map {
                instructorRepository.findById(it).getOrElse {
                    throw NotFoundException("Instructor with id: $it not found.")
                }
            }.toMutableList()

        val alreadyHasApp: MutableList<Int> = mutableListOf()

        theApplication.authorizedInstructors.map {
            if (applicationRepository.existsByCourseCodeAndInstructorId(
                    theApplication.courseCode,
                    it,
                    theApplication.term,
                    theApplication.section
                )
            ) {
                alreadyHasApp.add(it)
            }
        }

        if (alreadyHasApp.isNotEmpty()) {
            throw AlreadyExistsException(
                "Instructor(s) with id(s) ${alreadyHasApp.joinToString(", ")} have already an application for this course in the given term.",
                alreadyHasApp
            )
        }

        val courses = mutableListOf<Course>(course)

        val previousCourseGrades: MutableList<ApplicationCourseGrade> =
            theApplication.previousCourseGrades.map {
                val course = courseRepository.findByCourseCode(it.courseCode).getOrElse {
                    val courseResponse = coursesService.addCourse(
                        CourseDTORequest(
                            "",
                            it.courseCode,
                            ""
                        )
                    )

                    courseRepository.findById(courseResponse.id).get()
                }
                if (course.id == null) {
                    // If the course is newly created, save it in the repository
                    courseRepository.save(course)
                }
                if (course in courses)
                    throw InvalidInputException("There should be at most one grade for each course")
                courses.add(course)

                ApplicationCourseGrade(
                    course = course,
                    desiredLetterGrade = it.grade,
                    isInprogressAllowed = it.isInprogressAllowed,
                    isNotTakenAllowed = it.isNotTakenAllowed

                )
            }.toMutableList().also { courses.clear() }


        val lastDate = parseDateTime(theApplication.lastApplicationDate)

        if (lastDate.isBefore(now)){
            throw InvalidInputException("Cannot open an announcement before the current time.")
        }

        val term = validateTerm(theApplication.term)

        val dependentQuestions: MutableMap<String?, MutableList<Pair<Int, Int>>> = mutableMapOf()


        theApplication.questions.forEachIndexed { idx, q ->
            if (q.isConditionalQuestion == true && q.type == QuestionType.MULTIPLE_CHOICE) {
                q.choices?.forEachIndexed { chIdx, choice ->
                    val key = choice.conditionallyOpen
                    if (key != ""){
                        val list = dependentQuestions.getOrPut(key) { mutableListOf() }
                        list.add(idx to chIdx)
                    }

                }
            }
        }


        val independentQuestions = mutableListOf<QuestionDTORequest>()
        val dependentQuestionsList = mutableListOf<QuestionDTORequest>()

        theApplication.questions.forEachIndexed { idx, q ->
            if (dependentQuestions.containsKey(idx.toString())) {
                dependentQuestionsList.add(q)
            } else {
                independentQuestions.add(q)
            }
        }

        fun saveQuestions(questions: List<QuestionDTORequest>, parentId: List<Pair<Long, Int>>? = null): List<Question> {
            return questions.map {
                validateMultipleChoiceQuestion(it)
                questionRepository.save(createQuestionFromDTO(it, parentId))
            }
        }

        val independentQuestionsSaved = saveQuestions(independentQuestions)


        val questionIdMap = mutableMapOf<Int, Long>()

        independentQuestionsSaved.forEachIndexed { idx, q ->
            questionIdMap[theApplication.questions.indexOf(independentQuestions[idx])] = q.questionId
        }

        val dependentQuestionsSaved = dependentQuestionsList.map { q ->
            val questionIdx = theApplication.questions.indexOf(q)
            val parentChoices = dependentQuestions[questionIdx.toString()]?.mapNotNull { (parentIdx, choiceIdx) ->
                val parentId = questionIdMap[parentIdx]
                if (parentId != null) {
                    parentId to choiceIdx
                } else {
                    null
                }
            }

            val savedQuestion = saveQuestions(listOf(q), parentChoices).first()
            questionIdMap[questionIdx] = savedQuestion.questionId
            savedQuestion
        }

        val savedQuestions = independentQuestionsSaved + dependentQuestionsSaved



        val applicationEntity: Application = Application.builder(
            course,
            term,
            instructorsList,
            ApplicationStatus.OPENED
        )
            .createdAt(now)
            .isTimedOut(now >= lastDate)
            .lastApplicationDate(lastDate)
            .previousCourseGrades(previousCourseGrades)
            .weeklyWorkHours(theApplication.weeklyWorkHours)
            .jobDetails(theApplication.jobDetails)
            .isInprogressAllowed(theApplication.isInprogressAllowed)
            .isNotTakenAllowed(theApplication.isNotTakenAllowed)
            .minimumRequiredGrade(theApplication.minimumRequiredGrade)
            .section(theApplication.section)
            .followers(mutableListOf())
            .acceptEmail(DEFAULT_ACCEPT_MAIL)
            .rejectEmail(DEFAULT_REJECT_MAIL)
            .build()
            .also {
                it.questions = savedQuestions.toMutableList()
                applicationRepository.save(it)
            }



        val followers = studentRepository.getNewAnnouncementFollowers()

        val interestedClients: List<User> = followers
        val coInstructors: List<User> = instructorsList.filter { it.userID != userId }

        notificationService.sendMultipleNotificationsAsync(
            interestedClients=interestedClients,
            title = APPLICATION_ADDED_TITLE,
            description = "A new LA'ship announcement to  ${applicationEntity.course.courseCode} is published for the term ${applicationEntity.term}.",
            notificationType = NotificationType.NEW_ANNOUNCEMENT,
            relation = NotificationRelationType.DIRECT,
            roleBased = true,
            applicationId = applicationEntity.applicationId
        )

        notificationService.sendMultipleNotificationsAsync(
            interestedClients=coInstructors,
            title = APPLICATION_ADDED_TITLE,
            description = "You are added as a co-instructor for  ${applicationEntity.course.courseCode} by ${user._name + " " + user.surname + " (" + user.role + ")"} for the term ${applicationEntity.term}.",
            notificationType = NotificationType.NEW_ANNOUNCEMENT,
            relation = NotificationRelationType.DIRECT,
            roleBased = true,
            applicationId = applicationEntity.applicationId
        )


        return convertToApplicationDTOResponse(
            applicationEntity,
            now
        )

    }


    fun deleteApplicationById(searchKey: Any, user: User): ResponseEntity<Any> {
        val appId = verifyLong(searchKey)

        val applicationEntity: Application = applicationRepository.findById(appId).getOrElse {
            throw NotFoundException("Application with id: $appId not found.")
        }
        validateAuthorizedInstructor(applicationEntity.authorizedInstructors, user.userID)

        applicationEntity.course.removeApplication(applicationEntity)


        applicationEntity.authorizedInstructors.forEach {
            it.removeApplication(applicationEntity)
        }

        applicationRepository.deleteById(appId)


        var interestedClients: List<User> = applicationEntity.authorizedInstructors + applicationEntity.applicationRequests.mapNotNull { it.student }

        var followers: MutableList<User> = applicationEntity.followers

        followers.forEach{
            if (it.userID !in interestedClients.map { it.userID }){
                interestedClients = interestedClients + it
            }
        }

        notificationService.sendMultipleNotificationsAsync(
            interestedClients= interestedClients,
            title = APPLICATION_DELETED_TITLE,
            description = "The LA'ship announcement to  ${applicationEntity.course.courseCode} for the term ${applicationEntity.term} has been deleted by ${user._name + " " + user.surname}.",
            notificationType = NotificationType.ANNOUNCEMENT_UPDATE,
            relation = NotificationRelationType.DIRECT,
            roleBased = true
        )

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    fun updateApplicationById(searchKey: Any, applicationInput: ApplicationDTORequest, user: User): ApplicationDTOResponse {
        val appId = verifyLong(searchKey)

        val application: Application = applicationRepository.findById(appId).orElseThrow {
            NotFoundException("Application with id: $appId not found")
        }

        validateAuthorizedInstructor(application.authorizedInstructors, user.userID)

        val course: Course = courseRepository.findByCourseCode(applicationInput.courseCode).getOrElse {
            throw NotFoundException("No course found for the given code ${applicationInput.courseCode}")
        }

        val instructorsList: MutableList<Instructor> =
            applicationInput.authorizedInstructors.map {
                instructorRepository.findById(it).getOrElse {
                    throw NotFoundException("Instructor with id: $it not found.")
                }
            }.toMutableList()

        val alreadyHasApp: MutableList<Int> = mutableListOf()
        val prevAssignedInstructors: MutableList<Int> = mutableListOf()

        application.authorizedInstructors.forEach {
            prevAssignedInstructors.add(it.userID)
        }

        applicationInput.authorizedInstructors.map {
            if (applicationRepository.existsByCourseCodeAndInstructorId(
                    applicationInput.courseCode,
                    it,
                    applicationInput.term,
                    applicationInput.section
                ) && (it !in prevAssignedInstructors)
            ) {
                alreadyHasApp.add(it)
            }
        }

        if (alreadyHasApp.isNotEmpty()) {
            throw AlreadyExistsException(
                "Instructor(s) with id(s) ${alreadyHasApp.joinToString(", ")} have already an application for this course in the given term.",
                alreadyHasApp
            )
        }

        val courses = mutableListOf<Course>()

        val previousCourseGrades: MutableList<ApplicationCourseGrade> =
            applicationInput.previousCourseGrades.map {
                val course = courseRepository.findByCourseCode(it.courseCode).getOrElse {
                    val courseResponse = coursesService.addCourse(
                        CourseDTORequest(
                            "",
                            it.courseCode,
                            ""
                        )
                    )

                    courseRepository.findById(courseResponse.id).get()
                }

                if (course in courses)
                    throw InvalidInputException("There should be at most one grade for each course")
                courses.add(course)

                ApplicationCourseGrade(
                    course = course,
                    desiredLetterGrade = it.grade,
                    isInprogressAllowed = it.isInprogressAllowed,
                    isNotTakenAllowed = it.isNotTakenAllowed

                )
            }.toMutableList().also { courses.clear() }


        val lastDate = parseDateTime(applicationInput.lastApplicationDate)

        val term = validateTerm(applicationInput.term)

        val updatedCourse: ApplicationDTOResponse = application.let {
            if (application.course.courseCode != applicationInput.courseCode)
                throw InvalidInputException("Course cannot be changed for an application.")

            it.minimumRequiredGrade = applicationInput.minimumRequiredGrade
            it.term = term
            it.lastApplicationDate = lastDate
            it.weeklyWorkHours = applicationInput.weeklyWorkHours
            it.jobDetails = applicationInput.jobDetails
            it.authorizedInstructors = instructorsList
            it.previousCourseGrades = previousCourseGrades
            it.isInprogressAllowed= applicationInput.isInprogressAllowed
            it.isNotTakenAllowed = applicationInput.isNotTakenAllowed
            it.section = applicationInput.section
            applicationRepository.save(it)
            convertToApplicationDTOResponse(it)
        }

        var interestedClients: List<User> = application.authorizedInstructors + application.applicationRequests.mapNotNull { it.student }

        var followers: MutableList<User> = application.followers

        followers.forEach{
            if (it.userID !in interestedClients.map { it.userID }){
                interestedClients = interestedClients + it
            }
        }

        notificationService.sendMultipleNotificationsAsync(
            interestedClients= interestedClients,
            title = APPLICATION_UPDATED_TITLE,
            description = "The LA'ship announcement to  ${application.course.courseCode} for the term ${application.term} has been updated by ${user._name + " " + user.surname}.",
            notificationType = NotificationType.ANNOUNCEMENT_UPDATE,
            relation = NotificationRelationType.DIRECT,
            roleBased = true,
            applicationId = appId
        )

        return updatedCourse
    }

    private fun validateMultipleChoiceQuestion(qDTO: QuestionDTORequest) {
        if (qDTO.type == QuestionType.MULTIPLE_CHOICE && (qDTO.choices == null || qDTO.choices.size > 20 || qDTO.choices.size < 2)) {
            throw InvalidInputException("For multiple-choice questions, there should be at least 2 choices and a maximum of 20 choices.")
        }


        if (qDTO.type == QuestionType.MULTIPLE_CHOICE) {
            val choiceSet = mutableSetOf<String>()
            for (choice in qDTO.choices!!) {
                if (!choiceSet.add(choice.choice)) {
                    throw InvalidInputException("Duplicate choices are not allowed in multiple-choice questions.")
                }
            }
        }

    }
    private fun createQuestionFromDTO(qDTO: QuestionDTORequest, parents: List<Pair<Long, Int>>?): Question {
        return Question(
            type = qDTO.type,
            question = qDTO.question,
            choices = if (qDTO.type == QuestionType.MULTIPLE_CHOICE) qDTO.choices!!.map {
                Choice(
                    choice = it.choice,
                    conditionallyOpen = it.conditionallyOpen
                )
            }.toMutableList() else null,
            allowMultipleAnswers = qDTO.allowMultipleAnswers,
            isConditionalQuestion = qDTO.isConditionalQuestion == true,
            depends = parents?.map {
                QAndC(
                    it.first,
                    it.second
                )
            }


        )
    }
    fun getApplicationRequestsForApplication(searchKey: Any, userId: Int): ApplicationRequestResponse {
        val appId: Long = verifyLong(searchKey)
        val application: Application = applicationRepository.findById(appId).orElseThrow {
            NotFoundException("Application with ID $appId not found")
        }
        validateAuthorizedInstructor(application.authorizedInstructors, userId)


        return convertToApplicationRequestResponse(application)
    }

     fun getPhotoUrlByUserId(userId: String, role: UserRole): String? {

         val photoUrl = "https://mysu.sabanciuniv.edu/apps/getStudentPhotos/ens_getPhotos.php?id=$userId"

        val username = "ens_api"
        val password = "" // TODO

        val headers = HttpHeaders()

         headers.setBasicAuth(username, password)


        try {
            val response: ResponseEntity<String> = httpService.get(photoUrl, headers)
            if (response.statusCode.is2xxSuccessful) {
                response.body?.let {
                    val jsonObject = JsonParser.parseString(it).asJsonObject
                    return jsonObject.get("photo").asString
                }
            }
        } catch (e: Exception) {
            println("Error fetching photo URL: ${e.message}")
        }
        return null
    }

    fun changeApplicationStatus(searchKey: Any, to: ApplicationStatus, userId: Int): ApplicationDTOResponse {
        val appId: Long = verifyLong(searchKey)
        val application: Application = applicationRepository.findById(appId).orElseThrow {
            NotFoundException("Application with ID $appId not found")
        }
        validateAuthorizedInstructor(application.authorizedInstructors, userId)
        application.status = to
        val appEntity: Application = applicationRepository.save(application)
        return convertToApplicationDTOResponse(appEntity)
    }

    fun getAllApplicationsByCourseCode(searchKey: String): Collection<ApplicationDTOResponse> {
        if (!courseRepository.existsByCourseCode(searchKey))
            throw NotFoundException("Course with code: $searchKey not found")

        return applicationRepository.getByCourseCode(searchKey).map { convertToApplicationDTOResponse(it) }
    }

    fun getApplicationsByAuthorizedInstructor(instructorId: Int, pageable: Pageable): Page<ApplicationDTOResponse> {

        val instructor: Instructor = instructorRepository.findById(instructorId).orElseThrow {
            NotFoundException("Instructor with ID $instructorId not found")
        }

        return applicationRepository.getApplicationsByAuthorizedInstructorsOrderByCreatedAtDesc(instructor, pageable)
            .map {
                application ->
                convertToApplicationDTOResponse(application).also {
                    it.applicantCount = application.applicationRequests.size.toLong()
                } }
    }

    fun addFollowerToApplication(searchKey: Any): ApplicationDTOResponse {
        val appId: Long = verifyLong(searchKey)
        val user = userRepository.findById(getUser().userID)
            .orElseThrow { NotFoundException("User not found") }
        //val user = studentRepository.getById(3) as User

        val application: Application = applicationRepository.findById(appId).orElseThrow {
            NotFoundException("Application with ID $appId not found")
        }

        application.followers.map { if (it.userID == user.userID) throw InvalidInputException("You are already following this application") }

        application.addFollower(user)
        val appEntity: Application = applicationRepository.save(application)
        return convertToApplicationDTOResponse(appEntity).also { it.isFollowing = true }
    }

    fun removeFollowerFromApplication(searchKey: Any): ApplicationDTOResponse {
        val appId: Long = verifyLong(searchKey)
        val user = getUser()
        val application: Application = applicationRepository.findById(appId).orElseThrow {
            NotFoundException("Application with ID $appId not found")
        }

        var exist = false
        var existedUser: User? = null
        application.followers.map { if (it.userID == user.userID) {exist=true
        existedUser = it} }
        if (!exist) throw InvalidInputException("You are not following this application")

        application.removeFollower(existedUser!!)
        val appEntity: Application = applicationRepository.save(application)
        return convertToApplicationDTOResponse(appEntity).also { it.isFollowing = false }
    }

    fun getApplicationsByFollower(): Collection<ApplicationDTOResponse> {
        val currentUser = getUser()  // might be a dummy or security context instance
        val user = userRepository.findById(currentUser.userID).orElseThrow {
            NotFoundException("User with id ${currentUser.userID} not found")
        }
        return applicationRepository.getApplicationsByFollowersContains(user).map { convertToApplicationDTOResponse(it) }
    }

    fun finalizeAppReqsStatus(applicationId: Any, mailInput: MailDTORequest , instructorId: Int): List<ApplicationRequestDTOResponse> {
        val appId: Long = verifyLong(applicationId)
        val application = applicationRepository.findById(appId).orElseThrow {
            NotFoundException("Application with ID $appId not found")
        }
        val user = instructorRepository.findById(instructorId).orElseThrow {
            NotFoundException("Instructor with ID $instructorId not found")
        }

        validateAuthorizedInstructor(application.authorizedInstructors, instructorId)

        //var interestedClients: List<User> = listOf()
        var rejectedClients: List<User> = listOf()
        var acceptedClients: List<User> = listOf()
        var newAcceptedMail: String = mailInput.acceptMail
        var newRejectedMail: String = mailInput.rejectMail

        application.applicationRequests.map {
            if (it.status != it.statusIns){
                it.status = it.statusIns
                if (it.statusIns == ApplicationResult.ACCEPTED){
                    acceptedClients = acceptedClients + it.student!!
                    newAcceptedMail = extractSpecialKeywords(mailInput.acceptMail, application.course.courseCode, it.student!!._name, it.student!!.surname)
                }
                else if (it.statusIns == ApplicationResult.REJECTED){
                    rejectedClients = rejectedClients + it.student!!
                    it.committed = false
                    it.forgiven = false
                    newRejectedMail = extractSpecialKeywords(mailInput.rejectMail, application.course.courseCode, it.student!!._name, it.student!!.surname)
                } else {
                    it.committed = false
                    it.forgiven = false
                }

                applicationRequestService.saveApplicationRequest(it)

            }
        }

        val entity = applicationRepository.save(application)


        notificationService.sendMultipleNotificationsAsync(
            interestedClients= acceptedClients,
            title = entity.course.courseCode + " " +APPLICATION_REQUEST_STATUS_UPDATED_TITLE + " - ACCEPTED",
            description = newAcceptedMail,
            notificationType = NotificationType.STUDENT_STATUS_UPDATE,
            relation = NotificationRelationType.DIRECT,
            roleBased = true,
            applicationId = appId

        )

        notificationService.sendMultipleNotificationsAsync(
            interestedClients= rejectedClients,
            title = entity.course.courseCode+ " " +APPLICATION_REQUEST_STATUS_UPDATED_TITLE + " - REJECTED",
            description = newRejectedMail,
            notificationType = NotificationType.STUDENT_STATUS_UPDATE,
            relation = NotificationRelationType.DIRECT,
            roleBased = true,
            applicationId = appId

        )

        return entity.applicationRequests.map {
            convertToApplicationRequestDTOResponse(it)
        }.toList()

    }

    private fun extractSpecialKeywords(mail: String, courseName: String, fname: String, lname: String): String {
        var newMail = mail
        newMail = newMail.replace("[:course:]", courseName)
        newMail = newMail.replace("[:firstname:]", fname)
        newMail = newMail.replace("[:lastname:]", lname)
        newMail = newMail.replace("[:fullname:]", "$fname $lname")
        return newMail
    }


    fun updateApplicationMail(searchKey: Any, mailInput:MailDTORequest, userId: Int): ApplicationDTOResponse {
        val appId: Long = verifyLong(searchKey)
        val application: Application = applicationRepository.findById(appId).orElseThrow {
            NotFoundException("Application with ID $appId not found")
        }
        validateAuthorizedInstructor(application.authorizedInstructors, userId)
        application.acceptEmail = mailInput.acceptMail
        application.rejectEmail = mailInput.rejectMail
        val appEntity: Application = applicationRepository.save(application)
        return convertToApplicationDTOResponse(appEntity)
    }


}