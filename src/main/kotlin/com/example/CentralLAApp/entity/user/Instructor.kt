package com.example.CentralLAApp.entity.user

import com.example.CentralLAApp.entity.application.Application
import com.example.CentralLAApp.entity.course.Course
import com.example.CentralLAApp.enums.UserRole
import jakarta.persistence.*
import java.security.Principal


@Entity
@DiscriminatorValue("INSTRUCTOR")
open class Instructor : User {

    @ManyToMany(mappedBy = "authorizedInstructors")
    open var applications: MutableList<Application> = mutableListOf()

    @ManyToMany(mappedBy = "previousInstructors")
    open var students: MutableList<Student> = mutableListOf()

    fun removeApplication(application: Application){
        applications.remove(application)
    }
    protected constructor() {}
    protected constructor(builder: Builder) : super(builder)



    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }

    // Builder pattern
    class Builder : User.Builder<Instructor, Builder> {
        constructor() : super(Instructor()){
            this.role(UserRole.INSTRUCTOR)
        }


        override fun build(): Instructor {
            return user as Instructor
        }

        override fun self(): Builder {
            return this
        }
    }

}