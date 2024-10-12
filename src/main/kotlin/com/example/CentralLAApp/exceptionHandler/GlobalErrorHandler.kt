package com.example.CentralLAApp.exceptionHandler

import com.example.CentralLAApp.exception.*
import com.example.CentralLAApp.exception.securityExceptions.UnauthorizedException
import mu.KLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler


@Component
@ControllerAdvice
class GlobalErrorHandler : ResponseEntityExceptionHandler() {

    companion object: KLogging()


    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {

        logger.error("MethodArgumentNotValidException observed : ${ex.message}",ex)
        val errors = ex.bindingResult.allErrors
            .map { error -> error.defaultMessage!! }
            .sorted()

        logger.info("Errors : $errors")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(errors.joinToString (","){it})
    }


    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(ex: NotFoundException, request: WebRequest): ResponseEntity<Any> {
        logger.error("Exception observed: ${ex.message}",ex)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ex.message?.let {
                CustomErrorResponse(message = it, error = "${HttpStatus.BAD_REQUEST}")
            })


    }
    @ExceptionHandler(InvalidInputException::class)
    fun handleInvalidInputException(ex: InvalidInputException, request: WebRequest): ResponseEntity<Any> {
        logger.error("Exception observed: ${ex.message}",ex)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ex.message?.let {
                CustomErrorResponse(message = it, error = "${HttpStatus.BAD_REQUEST}")
            })


    }

    @ExceptionHandler(AlreadyExistsException::class)
    fun handleAlreadyExistsException(ex: AlreadyExistsException, request: WebRequest): ResponseEntity<Any> {
        logger.error("Exception observed: ${ex.message}",ex)
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ex.message?.let {
                AlreadyExistsResponse(message = it, error = "${HttpStatus.CONFLICT}", alreadyExistsList = ex.existList)
            })


    }
    @ExceptionHandler(TokenIsNotValidException::class)
    fun handleTokenIsNotValidException(ex: TokenIsNotValidException, request: WebRequest): ResponseEntity<Any> {
        logger.error("Exception observed: ${ex.message}",ex)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ex.message?.let {
                CustomErrorResponse(message = it, error = "${HttpStatus.UNAUTHORIZED}")
            })


    }

    @ExceptionHandler(LocalDateTimeParseException::class)
    fun handleLocalDateTimeParseException(ex: LocalDateTimeParseException, request: WebRequest): ResponseEntity<Any> {
        logger.error("Exception observed: ${ex.message}",ex)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ex.message?.let {
                CustomErrorResponse(message = it, error = "${HttpStatus.BAD_REQUEST}")
            })


    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleTokenIsNotValidException(ex: UnauthorizedException, request: WebRequest): ResponseEntity<Any> {
        logger.error("Exception observed: ${ex.message}",ex)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ex.message?.let {
                CustomErrorResponse(message = it, error = "${HttpStatus.UNAUTHORIZED}")
            })


    }

    @ExceptionHandler(java.lang.Exception::class)
    fun handleAllExceptions(ex: Exception, request: WebRequest): ResponseEntity<Any> {
        logger.error("Exception observed : ${ex.message}",ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ex.message)

    }
}