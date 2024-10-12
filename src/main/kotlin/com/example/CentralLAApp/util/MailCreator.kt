package com.example.CentralLAApp.util

// from some inputs create a text that will be sent as an email to the students that are accepted

fun createEmailTextForAccept(studentName: String, courseCode: String, body: String): String{
    return "Dear $studentName,\n\n $body"
}

fun createEmailSubject(courseCode: String): String{
    return "Application Status Update for Course $courseCode"
}

fun createEmailTextForReject(studentName: String, courseCode: String, body:String): String{
    return "Dear $studentName,\n\n $body\n\n"
}



