package com.example.CentralLAApp.entity.application

import com.example.CentralLAApp.entity.transcript.CoursesAndGrades
import com.example.CentralLAApp.entity.transcript.Transcript
import com.example.CentralLAApp.entity.user.Student
import com.example.CentralLAApp.enums.ApplicationResult
import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*
import java.time.Duration
import java.time.LocalDateTime


@Entity
@Table(name = "application_requests")
data class ApplicationRequest (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val applicationRequestId: Long = 0,

    @ManyToOne
    @JoinColumn(name = "application_id")
    var application: Application? = null,

    @ManyToOne
    @JoinColumn(name = "student_id")
    var student: Student? = null,

    @Enumerated(EnumType.STRING)
    var status: ApplicationResult? = ApplicationResult.IN_PROGRESS,

    @Enumerated(EnumType.STRING)
    var statusIns: ApplicationResult? = ApplicationResult.IN_PROGRESS,

    @ElementCollection
    @CollectionTable(
        name = "application_request_answers",
        joinColumns = [JoinColumn(name = "application_request_id")]
    )
    @Column(name = "answers", length = 512)
    var answers: MutableList<Answer> = mutableListOf(),

    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime? = LocalDateTime.now(),

    var committed: Boolean = false,

    var weeklyWorkHours: Duration = Duration.ofHours(10),

    var forgiven: Boolean = false,

    var redFlagged: Boolean = false,

    @ManyToOne
    val transcript: Transcript? = null

    )
    {
       companion object {
           fun builder(

                application: Application? = null,
                student: Student? = null,
                status: ApplicationResult? = ApplicationResult.IN_PROGRESS,
                statusIns: ApplicationResult? = ApplicationResult.IN_PROGRESS,
                transcript: Transcript? = null

              ) = Builder(

                application,
                student,
                status,
                statusIns,

                transcript
           )
       }

        data class Builder(
            private var application: Application? = null,
            private var student: Student? = null,
            private var status: ApplicationResult? = ApplicationResult.IN_PROGRESS,
            private var statusIns: ApplicationResult? = ApplicationResult.IN_PROGRESS,
            private var transcript: Transcript? = null,
            private var createdAt: LocalDateTime = LocalDateTime.now(),
            private var updatedAt: LocalDateTime? = LocalDateTime.now(),
            private var committed: Boolean = false,
            private var forgiven: Boolean = false,
            private var weeklyWorkHours: Duration = Duration.ofHours(10),
            private var redFlagged: Boolean = false

        ){

            fun application(application: Application?) = apply { this.application = application }
            fun student(student: Student?) = apply { this.student = student }
            fun status(status: ApplicationResult?) = apply { this.status = status }
            fun statusIns(statusIns: ApplicationResult?) = apply { this.statusIns = statusIns }

            fun createdAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }

            fun updatedAt(updatedAt: LocalDateTime?) = apply { this.updatedAt = updatedAt }

            fun transcript(transcript: Transcript?) = apply { this.transcript = transcript }

            fun committed(committed: Boolean) = apply { this.committed = committed }

            fun forgiven(forgiven: Boolean) = apply { this.forgiven = forgiven }

            fun weeklyWorkHours(weeklyWorkHours: Duration) = apply { this.weeklyWorkHours = weeklyWorkHours }

            fun redFlagged(redFlagged: Boolean) = apply { this.redFlagged = redFlagged }

            fun build(): ApplicationRequest{
                return ApplicationRequest(
                    application = application,
                    student = student,
                    status = status,
                    statusIns = statusIns,
                    transcript = transcript,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                    committed = committed,
                    forgiven = forgiven,
                    weeklyWorkHours = weeklyWorkHours,
                    redFlagged = redFlagged
                )
            }
        }
    }
@Embeddable
class Answer (
    val questionId: Long,
    val answer: String
){}