package com.example.CentralLAApp.dto

data class Schedule(val day: Int, val place: Int, val start: Int, val duration: Int)

data class Section(val crn: String, val schedule: List<Schedule>, val group: String, val instructors: List<String>)

data class Course(val name: String, val code: String, val classes: List<Section>)

data class CourseData(val courses: List<Course>, val instructors: List<String>)
