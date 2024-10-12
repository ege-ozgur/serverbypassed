package com.example.CentralLAApp.service

import com.example.CentralLAApp.dto.request.CoursesRequest
import com.example.CentralLAApp.dto.response.CoursesAndGradesResponse
import com.example.CentralLAApp.dto.response.CurrentTranscriptStatusResponse
import com.example.CentralLAApp.dto.response.TranscriptDTOResponse
import com.example.CentralLAApp.entity.transcript.CoursesAndGrades
import com.example.CentralLAApp.entity.transcript.Transcript
import com.example.CentralLAApp.entity.user.Student
import com.example.CentralLAApp.entity.user.User
import com.example.CentralLAApp.enums.LetterGrade
import com.example.CentralLAApp.enums.UserRole
import com.example.CentralLAApp.exception.InvalidInputException
import com.example.CentralLAApp.exception.NotFoundException
import com.example.CentralLAApp.exception.securityExceptions.UnauthorizedException
import com.example.CentralLAApp.repository.StudentRepository
import com.example.CentralLAApp.repository.TranscriptRepository
import com.example.CentralLAApp.service.helper.convertToCoursesAndGradesResponse
import com.example.CentralLAApp.service.helper.convertToTranscriptDTOResponse
import com.example.CentralLAApp.service.helper.verifyInt
import com.example.CentralLAApp.service.helper.verifyLong
import com.example.CentralLAApp.util.TermUtils
import com.example.CentralLAApp.util.getYearInfo
import com.example.CentralLAApp.util.security.getId
import mu.KLogging
import org.springframework.stereotype.Service

@Service
class TranscriptService(
    val transcriptRepository: TranscriptRepository,
    val studentRepository: StudentRepository,
    private val suService: SuService,
    private val applicationService: ApplicationService,
    //val transcriptFileRepository: TranscriptFileRepository
) {
    companion object : KLogging()

    fun addTranscript(transcriptInfo: Pair<ByteArray, MutableMap<String, Any>>, studentId: Any, user: User): Transcript {
        val theMap = transcriptInfo.second
        val content = transcriptInfo.first

        val studentidd: Int = verifyInt(studentId)
        val transcriptStuId = theMap["studentNumber"] as String
        if (user.universityId != transcriptStuId){
            throw InvalidInputException("Uploaded transcript does not match the actual user.")
        }

        val student: Student = studentRepository.findById(studentidd).orElseThrow {
            Exception("Student with ID ${theMap["studentId"]} not found")
        }


        //if (transcriptRepository.existsByStudentIdAndTerm(theMap["studentNumber"] as String, theMap["currentTerm"] as String))
        //    throw Exception("Transcript for student ${theMap["name"]} ${theMap["studentNumber"]} already exists for term ${theMap["currentTerm"]}")

        val transcript =
            transcriptRepository.findByStudentAndTerm(student, theMap["currentTerm"] as String).firstOrNull()


        // update
        transcript?.let {
            it.studentId = theMap["studentNumber"] as String
            it.term = theMap["currentTerm"] as String
            it.year = getYearInfo(theMap["totalCredit"] as String)
            it.studentName = theMap["name"] as String
            it.program = theMap["program"] as MutableList<String>
            it.cumulativeGPA = theMap["cumulativeGPA"] as String
            it.cumulativeCredits = theMap["totalCredit"] as String
            println(theMap["faculty"] as String)
            it.faculty = theMap["faculty"] as String
            it.student = student
            it.courses.clear()
            val courses = theMap["courseCodesAndGrades"] as MutableList<String>
            courses.forEach { course ->
                val (courseCode, grade, term) = course.split("/")

                val coursesAndGrades = CoursesAndGrades(
                    transcript = it,
                    courseCode = courseCode,
                    grade = LetterGrade.fromString(grade)!!,
                    term = term
                )
                it.courses.add(coursesAndGrades)
                logger.info { "Course and grade: $coursesAndGrades" }
            }

            /*
            it.pdfFile?.let { ts ->
                ts.content = content
                ts.transcript= it
                transcriptFileRepository.save(ts)
            }*/
            transcriptRepository.save(it)
            return it
        }


        // create new one
        return Transcript.builder(
            studentId = theMap["studentNumber"] as String,
            term = theMap["currentTerm"] as String,
            year = getYearInfo(theMap["totalCredit"] as String),
            studentName = theMap["name"] as String,
            program = theMap["program"] as MutableList<String>,
            cumulativeGPA = theMap["cumulativeGPA"] as String,
            cumulativeCredits = theMap["totalCredit"] as String,
            faculty = theMap["faculty"] as String,
            student = student
        ).build().also {
            val courses = theMap["courseCodesAndGrades"] as MutableList<String>
            courses.forEach { course ->
                val (courseCode, grade, term) = course.split("/")
                logger.info { "Grade is $grade and the converted LG is ${LetterGrade.fromString(grade)}" }
                val coursesAndGrades = CoursesAndGrades(
                    transcript = it,
                    courseCode = courseCode,
                    grade = LetterGrade.fromString(grade)!!,
                    term = term
                )
                it.courses.add(coursesAndGrades)
                logger.info { "Course and grade: $coursesAndGrades" }
            }


            logger.info { "Transcript before saving: $it" }


            transcriptRepository.save(it)
        }
    }

    fun getTranscriptByStudentId(studentId: Any): Collection<TranscriptDTOResponse> {
        val studentidd = verifyInt(studentId)
        val student = studentRepository.findById(studentidd).orElseThrow {
            Exception("Student with ID $studentId not found")
        }
        val transcripts = transcriptRepository.findByStudent(student)
        return transcripts.map { it.run { convertToTranscriptDTOResponse(this, student) } }
    }

    fun getTranscriptByStudentIdAndTerm(studentId: Int, term: String): Collection<TranscriptDTOResponse> {
        val studentidd = verifyInt(studentId)
        val student = studentRepository.findById(studentidd).orElseThrow {
            Exception("Student with ID $studentId not found")
        }
        return (transcriptRepository.findByStudentAndTerm(student, term)).map {
            it.run { convertToTranscriptDTOResponse(this, student) }
        }
    }

    fun getLastTranscript(searchKey: Any): TranscriptDTOResponse {

        val userId = verifyInt(searchKey)
        val student = studentRepository.findById(userId).orElseThrow {
            NotFoundException("Student with ID $userId not found")
        }
        val lastTranscript = student.transcripts.sortedWith { t1, t2 ->
            TermUtils.compareTwoTerms(t2.term, t1.term)
        }.firstOrNull()
            ?: throw NotFoundException("No transcript found for this student")
        return convertToTranscriptDTOResponse(lastTranscript, student)
    }

    fun getCourseGrades(searchKey: Any, courses: CoursesRequest): List<CoursesAndGradesResponse> {
        val userId = verifyInt(searchKey)
        val student = studentRepository.findById(userId).orElseThrow {
            NotFoundException("Student with ID $userId not found")
        }

        val coursesAndGrades = student.transcripts
            .maxByOrNull { it.term }
            ?.courses
            ?.filter { it.courseCode in courses.courses }
            ?.toMutableList() ?: mutableListOf()

        return convertToCoursesAndGradesResponse(coursesAndGrades)
    }

    /* fun getTranscriptFileByTranscriptId(key: Any): TranscriptFile {
         val transcriptId = verifyLong(key)
         val transcript = transcriptRepository.findById(transcriptId).orElseThrow{
             NotFoundException("Transcript with ID $transcriptId not found")
         }

         return transcript.pdfFile!!
     }*/

    fun deleteTranscriptById(key: Any) {
        val transcriptId = verifyLong(key)
        val userId = getId(UserRole.STUDENT)
        val transcript = transcriptRepository.findById(transcriptId).orElseThrow {
            NotFoundException("Transcript with ID $transcriptId not found")
        }

        if (transcript.student?.userID != userId) {
            throw UnauthorizedException()
        }

        return transcriptRepository.delete(transcript)

    }

    fun getCurrentTranscriptStatus(studentId: Int): CurrentTranscriptStatusResponse {
        val student = studentRepository.findById(studentId).orElseThrow {
            NotFoundException("Student with ID $studentId not found")
        }
        val transcript = student.transcripts.maxByOrNull { it.term }
        val currentTerm = suService.getCurrenTerm()
        val uploadedTerm = transcript?.term?.let { suService.getTermByTermDesc(it) }

        return CurrentTranscriptStatusResponse(
            currentTerm = currentTerm.term_desc,
            isUploadedAnyTranscript = transcript != null,
            isUploadedValidTranscript = uploadedTerm?.let { it.term_code > currentTerm.term_code } ?: false,
            lastUploadedTerm = transcript?.term
        )
    }


}