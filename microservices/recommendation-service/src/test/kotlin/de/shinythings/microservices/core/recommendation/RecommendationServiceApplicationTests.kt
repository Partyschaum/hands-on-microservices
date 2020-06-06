package de.shinythings.microservices.core.recommendation

import de.shinythings.api.core.recommendation.Recommendation
import de.shinythings.microservices.core.recommendation.persistence.RecommendationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec
import reactor.core.publisher.Mono.just

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = ["spring.data.mongodb.port: 0"]
)
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RecommendationServiceApplicationTests {

    @Autowired
    private lateinit var client: WebTestClient

    @Autowired
    private lateinit var repository: RecommendationRepository

    @BeforeEach
    fun setupDb() {
        repository.deleteAll()
    }

    @Test
    fun getRecommendationsByProductId() {
        val productId = 1

        postAndVerifyRecommendation(productId, 1, OK)
        postAndVerifyRecommendation(productId, 2, OK)
        postAndVerifyRecommendation(productId, 3, OK)

        assertEquals(3, repository.findByProductId(productId))

        getAndVerifyRecommendationsByProductId(productId, OK)
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[2].productId").isEqualTo(productId)
                .jsonPath("$[2].recommendationId").isEqualTo(3)
    }

    @Test
    fun duplicateError() {
        val productId = 1
        val recommendationId = 1

        assertEquals(0, repository.count())

        postAndVerifyRecommendation(productId, recommendationId, OK)
                .jsonPath("$.productId").isEqualTo(productId)
                .jsonPath("$.recommendationId").isEqualTo(recommendationId)

        assertEquals(1, repository.count())

        postAndVerifyRecommendation(productId, recommendationId, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Duplicate key, Product Id: 1, Recommendation Id: 1")

        assertEquals(1, repository.count())
    }

    @Test
    fun deleteRecommendations() {
        val productId = 1
        val recommendationId = 1

        postAndVerifyRecommendation(productId, recommendationId, OK)
        assertEquals(1, repository.findByProductId(productId))

        deleteAndVerifyRecommendationsByProductId(productId, OK)
        assertEquals(0, repository.findByProductId(productId))

        deleteAndVerifyRecommendationsByProductId(productId, OK)
    }

    @Test
    fun getRecommendationsMissingParameter() {
        getAndVerifyRecommendationsByProductId("", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Required int parameter 'productId' is not present")
    }

    @Test
    fun getRecommendationsInvalidParameter() {
        getAndVerifyRecommendationsByProductId("?productId=no-integer", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Type mismatch.")
    }

    @Test
    fun getRecommendationsNotFound() {
        getAndVerifyRecommendationsByProductId("?productId=113", OK)
                .jsonPath("$.length()").isEqualTo(0)
    }

    @Test
    fun getRecommendationsInvalidParameterNegativeValue() {
        val productIdInvalid = -1

        getAndVerifyRecommendationsByProductId("?productId=$productIdInvalid", UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/recommendation")
                .jsonPath("$.message").isEqualTo("Invalid productId: $productIdInvalid")
    }

    private fun getAndVerifyRecommendationsByProductId(productId: Int, expectedStatus: HttpStatus): BodyContentSpec {
        return getAndVerifyRecommendationsByProductId("?productId=$productId", expectedStatus)
    }

    private fun getAndVerifyRecommendationsByProductId(productIdQuery: String, expectedStatus: HttpStatus): BodyContentSpec {
        return client.get()
                .uri("/recommendation$productIdQuery")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
    }

    private fun postAndVerifyRecommendation(productId: Int, recommendationId: Int, expectedStatus: HttpStatus): BodyContentSpec {
        val recommendation = Recommendation(
                productId = productId,
                recommendationId = recommendationId,
                author = "Author $recommendationId",
                rate = recommendationId,
                content = "Content $recommendationId",
                serviceAddress = "SA"
        )

        return client.post()
                .uri("/recommendation")
                .body(just(recommendation), Recommendation::class.java)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
    }

    private fun deleteAndVerifyRecommendationsByProductId(productId: Int, expectedStatus: HttpStatus): BodyContentSpec {
        return client.delete()
                .uri("/recommendation?productId=$productId")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody()
    }
}
