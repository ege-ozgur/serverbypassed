package com.example.CentralLAApp.entity.application

import com.example.CentralLAApp.entity.course.Course
import com.example.CentralLAApp.entity.question.Question
import com.example.CentralLAApp.entity.user.Instructor
import com.example.CentralLAApp.entity.user.User
import com.example.CentralLAApp.enums.ApplicationStatus
import com.example.CentralLAApp.enums.LetterGrade
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.*
import jakarta.validation.constraints.Size
import org.hibernate.annotations.Formula
import java.time.Duration
import java.time.LocalDateTime



@Entity
@Table(name = "applications")
data class Application(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val applicationId: Long = 0,

    @ManyToOne
    @JoinColumn(name = "course_id")
    val course: Course,

    @OneToMany(mappedBy = "application",cascade = [CascadeType.ALL])
    var applicationRequests: MutableList<ApplicationRequest> = mutableListOf(),


    @Enumerated(EnumType.STRING)
    var minimumRequiredGrade: LetterGrade? = null,

    var isInprogressAllowed: Boolean? = null,

    var isNotTakenAllowed: Boolean? = null,

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    var questions: MutableList<Question> = mutableListOf(),

    var term: String,

    val createdAt: LocalDateTime? = LocalDateTime.now(),


    @Formula("(CASE WHEN CURRENT_TIMESTAMP > last_application_date THEN true ELSE false END)")
    var isTimedOut: Boolean = false,

    @Column(name = "last_application_date")
    var lastApplicationDate: LocalDateTime? = null,

    var weeklyWorkHours: Duration = Duration.ofHours(10),

    var section: String? = null,

    @Size(max = 2048)
    @Column(length = 2048)
    var jobDetails : String? = null,

    @ManyToMany(targetEntity = Instructor::class, fetch = FetchType.EAGER)
    @JoinTable(
        name = "application_instructors",
        joinColumns = [JoinColumn(name = "application_id")],
        inverseJoinColumns = [JoinColumn(name = "instructor_id")]
    )
    var authorizedInstructors: MutableList<Instructor> = mutableListOf(),

    @ManyToMany(targetEntity = User::class, fetch = FetchType.EAGER)
    @JoinTable(
        name = "application_followers",
        joinColumns = [JoinColumn(name = "application_id")],
        inverseJoinColumns = [JoinColumn(name = "user_id")]
    )
    var followers: MutableList<User> = mutableListOf(),


    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @JoinColumn(name = "application_id")
    var previousCourseGrades: MutableList<ApplicationCourseGrade> = mutableListOf(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ApplicationStatus = ApplicationStatus.OPENED,

    @Column(nullable = true, length = 2048)
    @Size(max = 2048)

    var acceptEmail:String,

    @Column(nullable = true, length = 2048)
    @Size(max = 2048)
    var rejectEmail:String,


) {
    fun removeInstructor(instructor: Instructor) {
        this.authorizedInstructors.remove(instructor)
    }

    fun addFollower(follower: User) {
        this.followers.add(follower)
    }

    fun removeFollower(follower: User) {
        this.followers.remove(follower)
    }

    fun removeApplicationRequest(applicationRequest: ApplicationRequest) {
        this.applicationRequests.remove(applicationRequest)
    }


    companion object {
        fun builder(
            course: Course,
            term: String,
            authorizedInstructors: MutableList<Instructor>,
            status: ApplicationStatus = ApplicationStatus.OPENED
        ) =
            Builder(course, term, authorizedInstructors, status)
    }

    data class Builder(
        var course: Course,
        var term: String,
        var authorizedInstructors: MutableList<Instructor>,
        var status: ApplicationStatus = ApplicationStatus.OPENED
    ) {
        private var createdAt: LocalDateTime? = LocalDateTime.now()
        private var lastApplicationDate: LocalDateTime? = null
        private var weeklyWorkHours: Duration = Duration.ofHours(10)
        private var laFeeMonthly: Double = 2000.0
        private var jobDetails : String? = null
        private var isTimedOut: Boolean = false
        private var previousCourseGrades: MutableList<ApplicationCourseGrade> = mutableListOf()
        private var isInprogressAllowed: Boolean? = null
        private var isNotTakenAllowed: Boolean? = null
        private var section : String? = null
        private var minimumRequiredGrade: LetterGrade? = null
        private var followers: MutableList<User> = mutableListOf()
        private var acceptEmail:String = ""
        private var rejectEmail:String = ""

        fun previousCourseGrades(previousCourseGrades: MutableList<ApplicationCourseGrade>) = apply { this.previousCourseGrades = previousCourseGrades }

        fun jobDetails(jobDetails: String?) = apply { this.jobDetails = jobDetails }

        fun weeklyWorkHours(weeklyWorkHours: Duration) = apply { this.weeklyWorkHours = weeklyWorkHours }

        fun laFeeMonthly(laFeeMonthly: Double) = apply { this.laFeeMonthly = laFeeMonthly }

        fun createdAt(createdAt: LocalDateTime?) = apply { this.createdAt = createdAt }

        fun isTimedOut(isTimedOut: Boolean) = apply { this.isTimedOut = isTimedOut }

        fun lastApplicationDate(lastApplicationDate: LocalDateTime?) =
            apply { this.lastApplicationDate = lastApplicationDate }

        fun section(section: String?) =
            apply { this.section = section }

        fun isInprogressAllowed(isInprogressAllowed: Boolean?) = apply { this.isInprogressAllowed = isInprogressAllowed }

        fun isNotTakenAllowed(isNotTakenAllowed: Boolean?) = apply { this.isNotTakenAllowed = isNotTakenAllowed }

        fun minimumRequiredGrade(minimumRequiredGrade: LetterGrade?) = apply { this.minimumRequiredGrade = minimumRequiredGrade }

        fun followers(followers: MutableList<User>) = apply { this.followers = followers }

        fun acceptEmail(acceptEmail: String) = apply { this.acceptEmail = acceptEmail }

        fun rejectEmail(rejectEmail: String) = apply { this.rejectEmail = rejectEmail }


        fun build(): Application {
            return Application(
                course = course,
                minimumRequiredGrade = minimumRequiredGrade,
                term = term,
                createdAt = createdAt,
                lastApplicationDate = lastApplicationDate,
                authorizedInstructors = authorizedInstructors,
                status = status,
                weeklyWorkHours = weeklyWorkHours,
                previousCourseGrades = previousCourseGrades,
                jobDetails = jobDetails,
                isTimedOut = isTimedOut,
                isInprogressAllowed = isInprogressAllowed,
                isNotTakenAllowed = isNotTakenAllowed,
                section = section,
                followers = followers,
                acceptEmail = acceptEmail,
                rejectEmail = rejectEmail
            )
        }
    }
}