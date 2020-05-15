package de.shinythings.util.http

import de.shinythings.util.exceptions.InvalidInputException
import de.shinythings.util.exceptions.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
internal class GlobalControllerExceptionHandler {

    private val logger = LoggerFactory.getLogger(GlobalControllerExceptionHandler::class.java)

    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(NOT_FOUND)
    @ResponseBody
    fun handleNotFoundExceptions(request: ServerHttpRequest, ex: Exception): HttpErrorInfo {
        return createHttpErrorInfo(NOT_FOUND, request, ex)
    }

    @ExceptionHandler(InvalidInputException::class)
    @ResponseStatus(UNPROCESSABLE_ENTITY)
    @ResponseBody
    fun handleInvalidInputException(request: ServerHttpRequest, ex: Exception): HttpErrorInfo {
        return createHttpErrorInfo(UNPROCESSABLE_ENTITY, request, ex)
    }

    private fun createHttpErrorInfo(httpStatus: HttpStatus, request: ServerHttpRequest, ex: Exception): HttpErrorInfo {
        val path = request.path.pathWithinApplication().value()
        val message = ex.message
        logger.debug("Returning HTTP status: {} for path: {}, message: {}", httpStatus, path, message)
        return HttpErrorInfo(httpStatus, path, message)
    }
}
