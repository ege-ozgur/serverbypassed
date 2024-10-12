package com.example.CentralLAApp.service


import com.example.CentralLAApp.entity.user.User
import com.example.CentralLAApp.enums.UserRole
import com.example.CentralLAApp.exception.securityExceptions.UnauthorizedException
import com.example.CentralLAApp.util.*
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.*


@Service
class FileService(private val userService: UserService) {

    @Throws(Exception::class)
    fun storeAndParseFile(file: MultipartFile): Pair<ByteArray, MutableMap<String, Any>> {
        // For the sake of simplicity, we are parsing the PDF content directly from the InputStream.
        // In a real scenario, you might want to store the file somewhere and then parse it.
        val content = file.inputStream.readBytes()

        try {
             PDDocument.load(content.inputStream()).use { document ->
                val pdfStripper = PDFTextStripper()
                val text = pdfStripper.getText(document)
                //println(text)

                return content to getTranscriptInfo(text)
            }

        }catch (e: Exception){
            throw e
        }
    }


}
