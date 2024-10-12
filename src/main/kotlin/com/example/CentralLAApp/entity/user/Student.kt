package com.example.CentralLAApp.entity.user

import com.example.CentralLAApp.entity.application.ApplicationRequest
import com.example.CentralLAApp.entity.transcript.Transcript
import com.example.CentralLAApp.enums.UserRole
import jakarta.persistence.*
import java.security.Principal


@Entity
@DiscriminatorValue("STUDENT")
open class Student : User {

    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH])
    @JoinTable(
        name = "student_instructor",
        joinColumns = [JoinColumn(name = "student_id")],
        inverseJoinColumns = [JoinColumn(name = "instructor_id")]
    )
    open val previousInstructors: MutableSet<Instructor> = mutableSetOf()



    @OneToMany(mappedBy = "student", cascade = [CascadeType.ALL])
    open val applicationRequests: MutableList<ApplicationRequest> = mutableListOf()

    @OneToMany(mappedBy = "student", cascade = [CascadeType.ALL])
    open val transcripts: MutableList<Transcript> = mutableListOf()

    fun removeInstructor(instructor: Instructor){
        this.previousInstructors.remove(instructor)
    }

    protected constructor() {}
    protected constructor(builder: Builder) : super(builder)



    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }

    // Builder pattern
    class Builder : User.Builder<Student, Builder> {
        constructor() : super(Student()) {
            this.role(UserRole.STUDENT)
        }


        override fun build(): Student {
            return user as Student
        }

        override fun self(): Builder {
            return this
        }
    }

}


