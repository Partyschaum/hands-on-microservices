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
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.io.IOException

@Component
class ProductCompositeIntegration(
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

    private val logger = LoggerFactory.getLogger(ProductCompositeIntegration::class.java)

    private val productServiceUrlBuilder = UriComponentsBuilder.newInstance().apply {
        scheme(if (productServiceHttps) "https" else "http")
        host(productServiceHost)
        port(productServicePort)
        path("product/{productId}")
    }

    private val recommendationServiceUrlBuilder = UriComponentsBuilder.newInstance().apply {
        scheme(if (recommendationServiceHttps) "https" else "http")
        host(recommendationServiceHost)
        port(recommendationServicePort)
        path("recommendation")
        query("productId={productId}")
    }

    private val reviewServiceUrlBuilder = UriComponentsBuilder.newInstance().apply {
        scheme(if (reviewServiceHttps) "https" else "http")
        host(reviewServiceHost)
        port(reviewServicePort)
        path("review")
        query("productId={productId}")
    }

    override fun getProduct(productId: Int): Product? {
        return try {
            restTemplate.getForObject(productServiceUrlBuilder.build(productId), Product::class.java)
        } catch (ex: HttpClientErrorException) {
            when (ex.statusCode) {
                NOT_FOUND -> throw NotFoundException(errorMessage(ex))
                UNPROCESSABLE_ENTITY -> throw InvalidInputException(errorMessage(ex))
                else -> {
                    logger.warn("Got a unexpected HTTP error: {}, will rethrow it", ex.statusCode)
                    logger.warn("Error body: {}", ex.responseBodyAsString)
                    throw ex
                }
            }
        }
    }

    override fun getRecommendations(productId: Int): List<Recommendation> {
        return try {
            val uri = recommendationServiceUrlBuilder.build(productId)

            logger.debug("Will call getRecommendations API on URL: {}", uri)

            val recommendations = restTemplate.exchange(
                    uri,
                    GET,
                    null,
                    object : ParameterizedTypeReference<List<Recommendation>>() {}
            ).body ?: emptyList()

            logger.debug("Found {} recommendations for a product with id: {}", recommendations.size, productId)

            recommendations
        } catch (ex: Exception) {
            logger.warn("Got an exception while requesting recommendations, return zero recommendations: {}", ex.message)
            return emptyList()
        }
    }

    override fun getReviews(productId: Int): List<Review> {
        return try {
            val uri = reviewServiceUrlBuilder.build(productId)

            logger.debug("Will call getReviews API on URL: {}", uri)

            val reviews = restTemplate.exchange(
                    uri,
                    GET,
                    null,
                    object : ParameterizedTypeReference<List<Review>>() {}
            ).body ?: emptyList()

            logger.debug("Found {} recommendations for a product with id: {}", reviews.size, productId)

            reviews
        } catch (ex: Exception) {
            logger.warn("Got an exception while requesting reviews, return zero reviews: {}", ex.message)
            return emptyList()
        }
    }

    private fun errorMessage(ex: HttpClientErrorException): String? = try {
        objectMapper.readValue(ex.responseBodyAsString, HttpErrorInfo::class.java).message
    } catch (_: IOException) {
        ex.message
    }
}
