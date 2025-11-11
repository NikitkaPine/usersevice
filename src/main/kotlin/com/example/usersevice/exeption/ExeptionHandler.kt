package com.example.usersevice.exeption

import com.example.usersevice.dto.JSendResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExeptionHandler {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentExeption(
        e: IllegalArgumentException
    ): ResponseEntity<JSendResponse<*>>{
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(JSendResponse.fail(e.message ?: "Bad request"))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handlerValidationExeption(
        e: MethodArgumentNotValidException
    ): ResponseEntity<JSendResponse<*>>{
        val errors = e.bindingResult.fieldErrors
            .associate { it.field to (it.defaultMessage ?: "Invalid") }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(JSendResponse.fail(errors))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(e: Exception):
            ResponseEntity<JSendResponse<*>> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(JSendResponse.error(e.message ?: "Internal server error"))
    }

    @ExceptionHandler(SecurityException::class)
    fun handleUnauthorizedException(e: SecurityException): ResponseEntity<JSendResponse<*>> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(JSendResponse.fail(e.message ?: "Unauthorized"))
    }
}