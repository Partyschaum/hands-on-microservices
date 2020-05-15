package de.shinythings.util.http

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import org.springframework.http.HttpStatus
import java.time.ZonedDateTime

data class HttpErrorInfo constructor(
        val timestamp: ZonedDateTime,
        val path: String,
        val message: String?,
        private val httpStatus: HttpStatus
) {

    constructor(
            @JsonProperty("status")
            @JsonDeserialize(using = HttpStatusDeserializer::class)
            httpStatus: HttpStatus,
            path: String,
            message: String?
    ) : this(
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

class HttpStatusDeserializer(vc: Class<*>?) : StdDeserializer<HttpStatus>(vc) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): HttpStatus {
        val status: JsonNode = p.codec.readTree(p)
        return HttpStatus.valueOf(status.asInt())
    }
}
