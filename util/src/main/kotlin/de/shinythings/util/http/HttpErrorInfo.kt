package de.shinythings.util.http

import org.springframework.http.HttpStatus
import java.time.ZonedDateTime

class HttpErrorInfo(
        val timestamp: ZonedDateTime,
        val path: String,
        val message: String?,
        private val httpStatus: HttpStatus
) {

    constructor(httpStatus: HttpStatus, path: String, message: String?) : this(
            timestamp = ZonedDateTime.now(),
            httpStatus = httpStatus,
            path = path,
            message = message
    )

    val status: Int
        get() = httpStatus.value()

    val error: String
        get() = httpStatus.reasonPhrase
}
