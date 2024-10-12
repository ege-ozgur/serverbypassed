package com.example.CentralLAApp.entity.course

import com.example.CentralLAApp.entity.application.Application
import com.example.CentralLAApp.entity.application.ApplicationCourseGrade
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "courses")
class Course(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "course_title")
    var courseTitle: String? = null,

    @Column(name = "course_code", unique = true)
    val courseCode: String = "",

    @OneToMany(mappedBy = "course", cascade = [CascadeType.ALL], orphanRemoval = true)
    var applications: MutableList<Application> = mutableListOf(),

    @Column
    var unit: String? = null,

    @OneToMany(mappedBy = "course", cascade = [CascadeType.ALL], orphanRemoval = true)
    val applicationCourseGrades: MutableList<ApplicationCourseGrade> = mutableListOf(),


    ) {
    fun removeApplication(application: Application) {
        applications.remove(application)
    }


    constructor() : this(0)


    class Builder {
        private var courseTitle: String? = null
        private var courseCode: String = ""
        private var unit: String? = null

        fun courseTitle(courseTitle: String?) = apply { this.courseTitle = courseTitle }
        fun courseCode(courseCode: String) = apply { this.courseCode = courseCode }

        fun unit(unit: String?) = apply { this.unit = unit }

        fun build(): Course {
            return Course(courseTitle = courseTitle, courseCode = courseCode, unit = unit)
        }
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }
}
