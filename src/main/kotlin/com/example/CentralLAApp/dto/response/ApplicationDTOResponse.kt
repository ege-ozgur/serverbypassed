package com.example.CentralLAApp.dto.response

import com.example.CentralLAApp.enums.ApplicationStatus
import com.example.CentralLAApp.enums.Eligibility
import org.apache.kafka.common.protocol.types.Field.Bool
import java.time.Duration

class ApplicationDTOResponse(
    val applicationId: Long,
    val course: CourseDTOResponse,
    val minimumRequiredGrade: String?,
    val questions: List<QuestionDTOResponse>,
    val term: String,
    val createdAt: String,
    val isTimedOut: Boolean,
    val lastApplicationDate: String,
    val authorizedInstructors: List<InstructorDTOResponse>,
    var weeklyWorkHours: Duration,
    val previousCourseGrades: MutableList<CourseGradesDTOResponse>,
    val jobDetails: String?,
    var status: ApplicationStatus,
    var isInprogressAllowed: Boolean?,
    var isNotTakenAllowed: Boolean?,
    var section: String?,
    val isStudentEligible: Eligibility?,
    var isFollowing: Boolean?,
    val followers: List<UserDTO>,
    var applicantCount: Long? = null,
    val acceptEmail: String,
    val rejectEmail: String
    //val applicationRequests: List<ApplicationRequestDTOForApplicationResponse>
)