package de.shinythings.microservices.composite.product.services

import com.fasterxml.jackson.databind.ObjectMapper
import de.shinythings.api.core.product.Product
import de.shinythings.api.core.product.ProductService
import de.shinythings.api.core.recommendation.Recommendation
import de.shinythings.api.core.recommendation.RecommendationService
import de.shinythings.api.core.review.Review
import de.shinythings.api.core.review.ReviewService
import de.shinythings.util.exceptions.InvalidInputException
import de.shinythings.util.exceptions.NotFoundException
import de.shinythings.util.http.HttpErrorInfo
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Flux.empty
import reactor.core.publisher.Mono
import java.io.IOException
import java.net.URI

@Component
class ProductCompositeIntegration(
        webClientBuilder: WebClient.Builder,

        private val restTemplate: RestTemplate,
        private val objectMapper: ObjectMapper,

        @Value("\${app.product-service.host}") private val productServiceHost: String,
        @Value("\${app.product-service.port}") private val productServicePort: String,
        @Value("\${app.product-service.https}") private val productServiceHttps: Boolean,

        @Value("\${app.recommendation-service.host}") private val recommendationServiceHost: String,
        @Value("\${app.recommendation-service.port}") private val recommendationServicePort: String,
        @Value("\${app.recommendation-service.https}") private val recommendationServiceHttps: Boolean,

        @Value("\${app.review-service.host}") private val reviewServiceHost: String,
        @Value("\${app.review-service.port}") private val reviewServicePort: String,
        @Value("\${app.review-service.https}") private val reviewServiceHttps: Boolean
) : ProductService, RecommendationService, ReviewService {

    private val webclient: WebClient by lazy { webClientBuilder.build() }

    private val logger = LoggerFactory.getLogger(ProductCompositeIntegration::class.java)

    override fun getProduct(productId: Int): Mono<Product> {
        return webclient.get()
                .uri(productServiceUrl(productId))
                .retrieve()
                .bodyToMono(Product::class.java)
                .log()
                .onErrorMap(WebClientResponseException::class.java) { ex ->
                    handleNonBlockingHttpClientException(ex)
                }
    }

    override fun createProduct(body: Product): Product {
        return try {
            val uri = productServiceUrl()

            logger.debug("Will post a new product to URL: {}", uri)

            restTemplate.postForObject(uri, body, Product::class.java)!!.also {
                logger.debug("Created a product with id: {}", it.productId)
            }
        } catch (ex: HttpClientErrorException) {
            throw handleHttpClientException(ex)
        }
    }

    override fun deleteProduct(productId: Int) {
        try {
            val uri = productServiceUrl(productId)

            logger.debug("Will call the deleteProduct API on URL: {}", uri)

            restTemplate.delete(uri)
        } catch (ex: HttpClientErrorException) {
            throw handleHttpClientException(ex)
        }
    }

    override fun getRecommendations(productId: Int): Flux<Recommendation> {
        val uri = recommendationServiceUrl(productId)

        logger.debug("Will call getRecommendations API on URL: {}", uri)

        return webclient.get()
                .uri(uri)
                .retrieve()
                .bodyToFlux(Recommendation::class.java)
                .log()
                .onErrorResume { empty() }
    }

    override fun createRecommendation(body: Recommendation): Recommendation {
        return try {
            val uri = recommendationServiceUrl()

            logger.debug("Will post a new recommendation to URL: {}", uri)

            restTemplate.postForObject(uri, body, Recommendation::class.java)!!.also {
                logger.debug("Created a recommendation with id: {}", it.productId)
            }
        } catch (ex: HttpClientErrorException) {
            throw handleHttpClientException(ex)
        }
    }

    override fun deleteRecommendations(productId: Int) {
        try {
            val uri = recommendationServiceUrl(productId)

            logger.debug("Will call the deleteRecommendations API on URL: {}", uri)

            restTemplate.delete(uri)
        } catch (ex: HttpClientErrorException) {
            throw handleHttpClientException(ex)
        }
    }

    override fun getReviews(productId: Int): Flux<Review> {
        val uri = reviewServiceUrl(productId)

        logger.debug("Will call getReviews API on URL: {}", uri)

        return webclient.get()
                .uri(uri)
                .retrieve()
                .bodyToFlux(Review::class.java)
                .log()
                .onErrorResume { empty() }
    }

    override fun createReview(body: Review): Review {
        return try {
            val uri = reviewServiceUrl()

            logger.debug("Will post a new review to URL: {}", uri)

            restTemplate.postForObject(uri, body, Review::class.java)!!.also {
                logger.debug("Created a review with id: {}", it.productId)
            }
        } catch (ex: HttpClientErrorException) {
            throw handleHttpClientException(ex)
        }
    }

    override fun deleteReviews(productId: Int) {
        try {
            val url = reviewServiceUrl(productId)

            logger.debug("Will call the deleteReviews API on URL: {}", url)

            restTemplate.delete(url)
        } catch (ex: HttpClientErrorException) {
            throw handleHttpClientException(ex)
        }
    }

    private fun productServiceUrl(productId: Int? = null): URI {
        return UriComponentsBuilder.newInstance().apply {
            scheme(if (productServiceHttps) "https" else "http")
            host(productServiceHost)
            port(productServicePort)
            if (productId != null) path("product/{productId}") else path("product")
        }.build(productId)
    }

    private fun recommendationServiceUrl(productId: Int? = null): URI {
        return UriComponentsBuilder.newInstance().apply {
            scheme(if (recommendationServiceHttps) "https" else "http")
            host(recommendationServiceHost)
            port(recommendationServicePort)
            path("recommendation")
            if (productId != null) query("productId={productId}")
        }.build(productId)
    }

    private fun reviewServiceUrl(productId: Int? = null): URI {
        return UriComponentsBuilder.newInstance().apply {
            scheme(if (reviewServiceHttps) "https" else "http")
            host(reviewServiceHost)
            port(reviewServicePort)
            path("review")
            if (productId != null) query("productId={productId}")
        }.build(productId)
    }

    private fun handleHttpClientException(ex: HttpClientErrorException) = when (ex.statusCode) {
        NOT_FOUND -> NotFoundException(errorMessage(ex))
        UNPROCESSABLE_ENTITY -> InvalidInputException(errorMessage(ex))
        else -> {
            logger.warn("Got a unexpected HTTP error: {}, will rethrow it", ex.statusCode)
            logger.warn("Error body: {}", ex.responseBodyAsString)
            ex
        }
    }

    private fun handleNonBlockingHttpClientException(ex: Throwable): Throwable {
        if (ex !is WebClientResponseException) {
            logger.warn("Got a unexpected error: {}, will rethrow it", ex.toString())
            return ex
        }

        return when (ex.statusCode) {
            NOT_FOUND -> NotFoundException(errorMessage(ex))
            UNPROCESSABLE_ENTITY -> InvalidInputException(errorMessage(ex))
            else -> {
                logger.warn("Got a unexpected HTTP error: {}, will rethrow it", ex.statusCode)
                logger.warn("Error body: {}", ex.responseBodyAsString)
                ex
            }
        }
    }

    private fun errorMessage(ex: HttpClientErrorException): String? = try {
        objectMapper.readValue(ex.responseBodyAsString, HttpErrorInfo::class.java).message
    } catch (_: IOException) {
        logger.warn("Could not deserialize HttpErrorInfo from {}", ex.responseBodyAsString)
        ex.message
    }

    private fun errorMessage(ex: WebClientResponseException): String? = try {
        objectMapper.readValue(ex.responseBodyAsString, HttpErrorInfo::class.java).message
    } catch (_: IOException) {
        logger.warn("Could not deserialize HttpErrorInfo from {}", ex.responseBodyAsString)
        ex.message
    }
}
