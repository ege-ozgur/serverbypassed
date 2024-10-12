package com.example.CentralLAApp.util

import com.example.CentralLAApp.exception.InvalidInputException
import mu.KLogging
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File
import java.util.regex.Pattern


fun extractTextFromPDF(pdfPath:String): String{
    PDDocument.load(File(pdfPath)).use { doc ->
        val stripper = PDFTextStripper()
        //stripper.startPage = 1
        //stripper.endPage = 1
        return stripper.getText(doc)
    }
}

fun getStudentNumber(text: String): String{
    val regex = """\bStudent Number\s*:\s*([\s\S]*?)(?=Faculty|${'$'})""".toRegex()
    val regexMatch = regex.find(text)
    return regexMatch?.groupValues?.get(1)?.trim() ?: ""
}

fun getName(text: String): String{
    val regex = """\bName\s*:\s*([\s\S]*?)(?=Level|${'$'})""".toRegex()
    val regexMatch = regex.find(text)
    return regexMatch?.groupValues?.get(1)?.trim() ?: ""
}

fun getProgram(text: String): List<String> {
    val regex = """\bProgram\(s\)\s*:\s*([\s\S]*?)(?=Admit|${'$'})""".toRegex()
    val regexMatch = regex.find(text)
    return (regexMatch?.groupValues?.get(1)?.trim() ?: "").split(";")
}

fun getFaculty(text: String): String{
    val regex = """\bFaculty\s*:\s*([\s\S]*?)(?=Birth|${'$'})""".toRegex()
    val regexMatch = regex.find(text)
    return regexMatch?.groupValues?.get(1)?.trim() ?: ""
}

fun getCourseCodesAndGrades(text: String): List<String>{
    val regex = """([A-Z]+ \d{3}).*?(?<=^|[\s,])(?:[A-D][-+]?|F|S|U)(?=[-+.]\B|[\s,]|${'$'})""".toRegex()
    val list = mutableListOf<String>()
    regex.findAll(text).forEach {
        val listRes = it.value.split(" ")
        //println(listRes)
        val courseCode = listRes[0] + " " + listRes[1]
        val letterGrade = listRes.last()
        val result = courseCode + " " + letterGrade
        list.add(result)
    }
    //println(list)
    return list
}

// Cumulative : 418.10 110.00 3.80 115.00 219.00 find this part from a text until \n
fun getCumulative(text: String): List<String>{
    val regex = """Cumulative\s*:\s*([\s\S]*?)(?=\n|${'$'})""".toRegex()
    val regexMatch = regex.find(text)
    val takenString = regexMatch?.groupValues?.get(1)?.trim() ?: ""
    val myList = takenString.split(" ")
    return myList
}

fun getCumulativeGPA(text: String): String{
    val myList = getCumulative(text)
    return myList[2]
}

fun getCumulativeCredits(text: String): String{
    val myList = getCumulative(text)
    return myList[3]
}



// find "Fall 2022-2023" or "Spring 2022-2023" or "Summer 2022-2023" from a text
fun getTerm(text: String): MutableList<String> {
    val regex = """(Fall|Spring|Summer)\s*\d{4}-\d{4}""".toRegex()
    //val regexMatch = regex.find(text)
    val list = mutableListOf<String>()
    regex.findAll(text).forEach {
        list.add(it.value)
    }
    return list
}


fun isThatATranscript(text: String): Boolean{
    val regex = """\bStudent Number\s*:\s*([\s\S]*?)(?=Faculty|${'$'})""".toRegex()
    val regexMatch = regex.find(text)
    return regexMatch != null
}


fun getTranscriptInfo(text: String): MutableMap<String,Any>{
    if (!isThatATranscript(text)){
        throw InvalidInputException("This is not a transcript file")
    }
    try {
        val studentNumber = getStudentNumber(text)
        val name = getName(text)
        val program = getProgram(text)
        val faculty = getFaculty(text)

        //val registeredCourses = getRegisteredCourses(text)
        val cumulativeGPA = getCumulativeGPA(text)
        val term = getTerm(text).last()
        val credit = getCumulativeCredits(text)

        val termPattern = Pattern.compile("""(Fall|Spring|Summer)\s*\d{4}-\d{4}""")
        val courseAndGradePattern = Pattern.compile("""([A-Z]+ \d{3}).*?(?<=^|[\s,])(?:[A-D][-+]?|F|S|U)(?=[-+.]\B|[\s,]|${'$'})""")

        val registeredPattern = Pattern.compile(""".*Registered${'$'}""")



        var currentTerm = ""
        val courseCodesAndGrades = mutableListOf<String>()
        var ip = false

        text.lines().forEach { line ->
            val termMatcher = termPattern.matcher(line)
            val courseMatcher = courseAndGradePattern.matcher(line)
            val registeredMatcher = registeredPattern.matcher(line)

            if (line == "COURSES IN PROGRESS"){
                ip = true
            }

            if (termMatcher.find()) {
                currentTerm = termMatcher.group()
            } else if (courseMatcher.find()) {

                val courseCode = courseMatcher.group().split(" ")[0].trim() + " " + courseMatcher.group().split(" ")[1].trim()

                val grade = courseMatcher.group().split(" ").last()
                val res = "$courseCode/$grade/$currentTerm"

                courseCodesAndGrades.add(res)
            }else if (ip){

                if(registeredMatcher.find()){
                    //println(registeredMatcher.group())

                    val courseCode = registeredMatcher.group().split(" ")[0].trim() + " " + registeredMatcher.group().split(" ")[1].trim()
                    val res = "$courseCode/IP/$currentTerm"
                    //println(res)

                    courseCodesAndGrades.add(res)
                }
            }

        }





        val transcriptMap = mutableMapOf<String,Any>()
       
        
        transcriptMap["studentNumber"] = studentNumber
        transcriptMap["name"] = name
        transcriptMap["program"] = program
        transcriptMap["faculty"] = faculty
        transcriptMap["cumulativeGPA"] = cumulativeGPA
        transcriptMap["totalCredit"] = credit
        transcriptMap["currentTerm"] = term
        transcriptMap["courseCodesAndGrades"] = courseCodesAndGrades

        /*list.add(studentNumber)
        list.add(name)
        list.add(program)
        list.add(faculty)
        list.add(courseCodesAndGrades)
        list.add(cumulativeGPA)*/
        // print the list
        println(faculty)
        return transcriptMap
    }catch (e: Exception) {
        throw InvalidInputException("Parsing Error")
    }

}



fun getYearInfo(totalCredit:Any): String{
    val credit = totalCredit.toString().toDouble()
    if(credit < 34){
        return "Freshman"
    }
    else if(credit < 64){
        return "Sophomore"
    }
    else if(credit < 94){
        return "Junior"
    }
    else{
        return "Senior"
    }
}

