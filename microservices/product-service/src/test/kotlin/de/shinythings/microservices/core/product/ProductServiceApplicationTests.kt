package de.shinythings.microservices.core.product

import de.shinythings.api.core.product.Product
import de.shinythings.microservices.core.product.persistence.ProductRepository
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
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
class ProductServiceApplicationTests {

    @Autowired
    private lateinit var client: WebTestClient

    @Autowired
    private lateinit var repository: ProductRepository

    @BeforeEach
    fun setupDb() {
        repository.deleteAll()
    }

    @Test
    fun getProductById() {
        val productId = 1

        postAndVerifyProduct(productId, OK)
        assertNotNull(repository.findByProductId(productId))

        getAndVerifyProduct(productId, OK)
                .jsonPath("$.productId").isEqualTo(productId)
    }

    @Test
    fun duplicateError() {
        val productId = 1

        postAndVerifyProduct(productId, OK)
        assertNotNull(repository.findByProductId(productId))

        postAndVerifyProduct(productId, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/product")
                .jsonPath("$.message").isEqualTo("Duplicate key, Product Id: $productId")
    }

    @Test
    fun deleteProduct() {
        val productId = 1

        postAndVerifyProduct(productId, OK)
        assertNotNull(repository.findByProductId(productId))

        deleteAndVerifyProduct(productId, OK)
        assertNull(repository.findByProductId(productId))

        deleteAndVerifyProduct(productId, OK)
    }

    @Test
    fun getProductInvalidParameter() {
        getAndVerifyProduct("/no-integer", BAD_REQUEST)
                .jsonPath("$.path").isEqualTo("/product/no-integer")
                .jsonPath("$.message").isEqualTo("Type mismatch.")
    }

    @Test
    fun getProductNotFound() {
        val productIdNotFound = 13

        getAndVerifyProduct(productIdNotFound, NOT_FOUND)
                .jsonPath("$.path").isEqualTo("/product/$productIdNotFound")
                .jsonPath("$.message").isEqualTo("No product found for productId: $productIdNotFound")
    }

    @Test
    fun getProductInvalidParameterNegativeValue() {
        val productIdInvalid = -1

        getAndVerifyProduct(productIdInvalid, UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/product/$productIdInvalid")
                .jsonPath("$.message").isEqualTo("Invalid productId: $productIdInvalid")
    }

    private fun getAndVerifyProduct(productId: Int, expectedStatus: HttpStatus): BodyContentSpec {
        return getAndVerifyProduct("/$productId", expectedStatus)
    }

    private fun getAndVerifyProduct(productIdPath: String, expectedStatus: HttpStatus): BodyContentSpec {
        return client.get()
                .uri("/product$productIdPath")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
    }

    private fun postAndVerifyProduct(productId: Int, expectedStatus: HttpStatus): BodyContentSpec {
        val product = Product(
                productId = productId,
                name = "Name $productId",
                weight = productId,
                serviceAddress = "SA"
        )

        return client.post()
                .uri("/product")
                .body(just(product), Product::class.java)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(APPLICATION_JSON)
                .expectBody()
    }

    private fun deleteAndVerifyProduct(productId: Int, expectedStatus: HttpStatus): BodyContentSpec {
        return client.delete()
                .uri("/product/$productId")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectBody()
    }
}
