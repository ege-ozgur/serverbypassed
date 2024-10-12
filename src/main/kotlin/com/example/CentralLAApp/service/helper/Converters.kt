package com.example.CentralLAApp.service.helper

import com.example.CentralLAApp.dto.response.*
import com.example.CentralLAApp.entity.application.Application
import com.example.CentralLAApp.entity.application.ApplicationCourseGrade
import com.example.CentralLAApp.entity.application.ApplicationRequest
import com.example.CentralLAApp.entity.course.Course
import com.example.CentralLAApp.entity.question.Question
import com.example.CentralLAApp.entity.transcript.CoursesAndGrades
import com.example.CentralLAApp.entity.transcript.Transcript
import com.example.CentralLAApp.entity.user.Instructor
import com.example.CentralLAApp.entity.user.Student
import com.example.CentralLAApp.entity.user.User
import com.example.CentralLAApp.enums.ApplicationResult
import com.example.CentralLAApp.enums.Eligibility
import java.time.LocalDateTime


fun convertToApplicationDTOResponse(theApplication: Application, now: LocalDateTime = LocalDateTime.now(), isEligible: Eligibility? = null, isFollowing: Boolean? = null): ApplicationDTOResponse = ApplicationDTOResponse(
    applicationId = theApplication.applicationId,
    course = theApplication.course.run { convertToCourseDTOResponse(this) },
    questions = theApplication.questions.map {convertToQuestionDTOResponse(it) },
    term = theApplication.term,
    createdAt = theApplication.createdAt?.toString() ?: now.toString(),
    minimumRequiredGrade = theApplication.minimumRequiredGrade?.value,
    status = theApplication.status,
    isTimedOut = theApplication.isTimedOut,
    authorizedInstructors = theApplication.authorizedInstructors.map { convertToInstructorDTOResponse(it) },
    lastApplicationDate = theApplication.lastApplicationDate.toString(),
    weeklyWorkHours = theApplication.weeklyWorkHours,
    previousCourseGrades = theApplication.previousCourseGrades.map { convertToCourseGradesDTOResponse(it) }.toMutableList(),
    jobDetails = theApplication.jobDetails,
    isInprogressAllowed = theApplication.isInprogressAllowed,
    isNotTakenAllowed = theApplication.isNotTakenAllowed,
    section = theApplication.section,
    isStudentEligible = isEligible,
    isFollowing = isFollowing,
    followers = theApplication.followers.map { convertToUserDTOResponse(it) },
    acceptEmail = theApplication.acceptEmail,
    rejectEmail = theApplication.rejectEmail

    //applicationRequests = theApplication.applicationRequests.map { convertToApplicationRequestDTOForApplicationResponse(it) }

)

fun convertToQuestionDTOResponse(question: Question) = QuestionDTOResponse(
    question.questionId,
    question.type,
    question.question,
    question.choices,
    question.allowMultipleAnswers,
    question.isConditionalQuestion,
    question.depends?.map {
        QAndCDTO(
            it.dependsOnQuestion,
            it.dependsOnChoice
        )
    }
)

fun convertToApplicationRequestResponse(application: Application): ApplicationRequestResponse {
    return ApplicationRequestResponse(
        course = convertToCourseDTOResponse(application.course),
        applicationRequests = application.applicationRequests.map {
            it.run { convertToApplicationRequestDTOForApplicationResponse(this) }
        }
    )
}

fun convertToCourseDTOResponse(theCourse: Course): CourseDTOResponse = CourseDTOResponse(
    theCourse.id!!,
    theCourse.courseTitle,
    theCourse.courseCode,
    theCourse.unit
)


fun convertToStudentDTOResponse(theStudent: Student): StudentDTOResponse = StudentDTOResponse(
    user = UserDTO(
        theStudent.userID,
        theStudent.email,
        theStudent._name,
        theStudent.surname,
        theStudent.graduationType,
        theStudent.role,
        theStudent.notificationPreferences,
        theStudent.photoUrl,
        theStudent.universityId
    )
)

fun convertToInstructorDTOResponse(theInstructor: Instructor): InstructorDTOResponse = InstructorDTOResponse(
    user = UserDTO(
        theInstructor.userID,
        theInstructor.email,
        theInstructor._name,
        theInstructor.surname,
        theInstructor.graduationType,
        theInstructor.role,
        theInstructor.notificationPreferences,
        theInstructor.photoUrl,
        theInstructor.universityId
    )
)

fun convertToUserDTOResponse(theUser: User): UserDTO = UserDTO(
    theUser.userID,
    theUser.email,
    theUser._name,
    theUser.surname,
    theUser.graduationType,
    theUser.role,
    theUser.notificationPreferences,
    theUser.photoUrl,
    theUser.universityId
)

fun convertToApplicationRequestDTOResponse(theApplicationRequest: ApplicationRequest, now:LocalDateTime = LocalDateTime.now()): ApplicationRequestDTOResponse = ApplicationRequestDTOResponse(
    applicationRequestId = theApplicationRequest.applicationRequestId,
    application = theApplicationRequest.application?.run { convertToApplicationDTOResponse(this) },
    student = theApplicationRequest.student?.run { convertToStudentDTOResponse(this) },
    status = formatStudentStatus(theApplicationRequest.status!!),
    statusIns = theApplicationRequest.statusIns,
    qAndA = theApplicationRequest.answers.map {  answer -> QuestionAndAnswer(convertToQuestionDTOResponse(theApplicationRequest.application!!.questions.find { answer.questionId == it.questionId }!!), answer.answer) }.toList(),
    createdAt = theApplicationRequest.createdAt?.toString() ?: now.toString(),
    updatedAt = theApplicationRequest.updatedAt?.toString() ?: now.toString(),
    transcript = theApplicationRequest.transcript?.run { convertToTranscriptDTOResponse(this, theApplicationRequest.student!!) },
    committed = theApplicationRequest.committed,
    forgiven = theApplicationRequest.forgiven,
    weeklyWorkHours = theApplicationRequest.weeklyWorkHours,
    redFlagged = theApplicationRequest.redFlagged
)

fun formatStudentStatus(status: ApplicationResult) = if (status in listOf(ApplicationResult.ACCEPTED, ApplicationResult.REJECTED)) status else ApplicationResult.IN_PROGRESS




fun convertToApplicationRequestDTOForApplicationResponse(theApplicationRequest: ApplicationRequest): ApplicationRequestDTOForApplicationResponse = ApplicationRequestDTOForApplicationResponse(
    applicationRequestId = theApplicationRequest.applicationRequestId,
    student = theApplicationRequest.student?.run { convertToStudentDTOResponse(this) },
    status = theApplicationRequest.status,
    statusIns = theApplicationRequest.statusIns,
    qAndA = theApplicationRequest.answers.map {  answer -> QuestionAndAnswer(convertToQuestionDTOResponse(theApplicationRequest.application!!.questions.find { answer.questionId == it.questionId }!!), answer.answer) }.toList(),
    createdAt = theApplicationRequest.createdAt?.toString() ?: LocalDateTime.now().toString(),
    transcript = theApplicationRequest.transcript?.run { convertToTranscriptDTOResponse(this, theApplicationRequest.student!!) },
    updatedAt = theApplicationRequest.updatedAt?.toString() ?: LocalDateTime.now().toString(),
    committed = theApplicationRequest.committed,
    forgiven = theApplicationRequest.forgiven,
    weeklyWorkHours = theApplicationRequest.weeklyWorkHours,
    redFlagged = theApplicationRequest.redFlagged,
    //application = theApplicationRequest.application?.run { convertToApplicationDTOResponse(this) },
)




fun convertToCourseGradesDTOResponse(previousCourseGrades: ApplicationCourseGrade) = CourseGradesDTOResponse(
    id = previousCourseGrades.id,
    course = previousCourseGrades.course.run { convertToCourseDTOResponse(this) },
    grade = previousCourseGrades.desiredLetterGrade?.value,
    isInprogressAllowed = previousCourseGrades.isInprogressAllowed ?: false,
    isNotTakenAllowed = previousCourseGrades.isNotTakenAllowed ?: false
)

fun convertToTranscriptDTOResponse(theTranscript: Transcript, theStudent:Student) : TranscriptDTOResponse= TranscriptDTOResponse(
    transcriptId = theTranscript.transcriptId,
    studentId = theStudent.userID ,
    studentSuId = theTranscript.studentId,
    term = theTranscript.term,
    year = theTranscript.year,
    studentName = theTranscript.studentName,
    program = convertToProgramsResponse(theTranscript.program),
    course = convertToCoursesAndGradesResponse(theTranscript.courses),
    faculty = theTranscript.faculty,
    cumulativeGPA = theTranscript.cumulativeGPA,
    cumulativeCredits = theTranscript.cumulativeCredits,
    photoUrl = theStudent.photoUrl ?: ""
)

fun convertToCoursesAndGradesResponse(courses: MutableList<CoursesAndGrades>):List<CoursesAndGradesResponse> = courses.map {
    CoursesAndGradesResponse(
        courseCode = it.courseCode,
        grade = it.grade,
        term = it.term
    )
}.toList()

fun convertToProgramsResponse(programs: MutableList<String>): ProgramsResponse {
    val majors = mutableListOf<Any>()
    val minors = mutableListOf<Any>()
    programs.forEach {
        if (it.contains("Minor")) {
            var minor = it.split("-")[1].trim()
            if (minor.contains(",")) {
                var minorList = minor.split(",")
                minorList.forEach { minor1 ->
                    if (minor1.contains("\n"))
                        minors.add(minor1.split("\n")[0].trim() + " " + minor1.split("\n")[1].trim())
                    else
                        minors.add(minor1)
                }

            } else{
                if (minor.contains("\n"))
                    minors.add(minor.split("\n")[0].trim() + " " + minor.split("\n")[1].trim())
                else
                    minors.add(minor)
                }
            } else {
                if (it.contains("Double")) {
                    var major = it.split("-")[1].trim()
                    if (major.contains("\n"))
                        major = major.split("\n")[0].trim() + " " + major.split("\n")[1].trim()
                    else
                        majors.add(major)
                    majors.add(major)
                } else
                    majors.add(it)
            }
        }



    return ProgramsResponse(majors, minors)

}
