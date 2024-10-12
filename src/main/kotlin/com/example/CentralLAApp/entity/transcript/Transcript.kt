package com.example.CentralLAApp.entity.transcript

import com.example.CentralLAApp.entity.user.Student
import jakarta.persistence.*


@Entity
@Table(name = "transcripts")
data class Transcript(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var transcriptId: Long = 0,

    @Column(name = "student_number")
    var studentId: String = "",

    @Column(name = "term")
    var term: String = "",

    @Column(name = "year")
    var year: String = "",

    @Column(name = "student_name", columnDefinition = "VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    var studentName: String = "",

    @ElementCollection
    @CollectionTable(
        name = "transcript_programs",
        joinColumns = [JoinColumn(name = "transcript_id")],
    )
    @Column( name = "program")
    var program: MutableList<String> = mutableListOf(),

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER, mappedBy = "transcript", orphanRemoval = true)
    val courses: MutableList<CoursesAndGrades> = mutableListOf(),

    @Column(name = "cumulative_GPA")
    var cumulativeGPA: String = "",

    @Column(name = "cumulative_credits")
    var cumulativeCredits: String = "",

    @Column(name = "faculty")
    var faculty: String = "",

    @ManyToOne
    @JoinColumn(name = "student_id")
    var student: Student? = null,

    /*@OneToOne(mappedBy = "transcript", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    var pdfFile: TranscriptFile? = null*/
    ) {


    companion object{
        fun builder(
            studentId: String = "",
            term: String = "",
            year: String = "",
            studentName: String = "",
            program: MutableList<String> = mutableListOf(),
            course: MutableList<CoursesAndGrades> = mutableListOf(),
            cumulativeGPA: String = "",
            cumulativeCredits: String = "",
            faculty: String = "",
            student: Student? = null,
        ) = Builder(

            studentId,
            term,
            year,
            studentName,
            program,
            course,
            cumulativeGPA,
            cumulativeCredits,
            faculty,
            student
        )
    }

    data class Builder(

        private var studentId: String = "",
        private var term: String = "",
        private var year: String = "",
        private var studentName: String = "",
        private var program: MutableList<String> = mutableListOf(),
        private var course:  MutableList<CoursesAndGrades> = mutableListOf(),
        private var cumulativeGPA: String = "",
        private var cumulativeCredits: String = "",
        private var faculty: String = "",
        private var student: Student? = null,

    ){

        fun studentId(studentId: String) = apply { this.studentId = studentId }
        fun term(term: String) = apply { this.term = term }
        fun year(year: String) = apply { this.year = year }
        fun studentName(studentName: String) = apply { this.studentName = studentName }
        fun program(program: MutableList<String>) = apply { this.program = program }
        fun course(course:  MutableList<CoursesAndGrades>) = apply { this.course = course }
        fun cumulativeGPA(cumulativeGPA: String) = apply { this.cumulativeGPA = cumulativeGPA }
        fun cumulativeCredits(cumulativeCredits: String) = apply { this.cumulativeCredits = cumulativeCredits }
        fun faculty(faculty: String) = apply { this.faculty = faculty }
        fun student(student: Student?) = apply { this.student = student }
        fun build(): Transcript{
            return Transcript(
                studentId=studentId,
                term = term,
                year = year,
                studentName = studentName,
                program = program,
                courses = course,
                cumulativeGPA = cumulativeGPA,
                cumulativeCredits = cumulativeCredits,
                faculty = faculty,
                student = student
            )
        }
    }

    override fun toString(): String {
        return "Transcript(transcriptId=$transcriptId, studentId='$studentId', term='$term', year='$year', studentName='$studentName', program=$program, cumulativeGPA='$cumulativeGPA',faculty='$faculty' , cumulativeCredits='$cumulativeCredits')"
    }

}