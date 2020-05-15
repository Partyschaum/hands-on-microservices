package de.shinythings.microservices.composite.product

import com.fasterxml.jackson.databind.ObjectMapper
import de.shinythings.util.http.HttpErrorInfo
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus

@SpringBootTest
class HttpStatusPropertyDeserializerTest {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun deserializeHttpStatusProperty() {
        val httpErrorInfo = HttpErrorInfo(
                httpStatus = HttpStatus.UNPROCESSABLE_ENTITY,
                path = "some path",
                message = "the real message"
        )

        val serializedHttpErrorInfo = objectMapper.writeValueAsString(httpErrorInfo)
        val deserializedHttpErrorInfo = objectMapper.readValue(serializedHttpErrorInfo, HttpErrorInfo::class.java)

        Assertions.assertEquals(httpErrorInfo.status, deserializedHttpErrorInfo.status)
    }
}
