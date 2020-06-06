package de.shinythings.microservices.composite.product.services

import de.shinythings.api.composite.product.*
import de.shinythings.api.core.product.Product
import de.shinythings.api.core.recommendation.Recommendation
import de.shinythings.api.core.review.Review
import de.shinythings.util.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.core.publisher.zip
import reactor.kotlin.core.publisher.zip
import java.util.function.Consumer
import java.util.function.Function


@RestController
class ProductCompositeServiceImpl(
        private val integration: ProductCompositeIntegration,
        private val serviceUtil: ServiceUtil
) : ProductCompositeService {

    private val logger = LoggerFactory.getLogger(ProductCompositeServiceImpl::class.java)

    override fun getCompositeProduct(productId: Int): Mono<ProductAggregate> {
//        return zip(
//                integration.getProduct(productId),
//                integration.getRecommendations(productId),
//                integration.getReviews(productId)
//        ) { values : Array<*> ->
//            createProductAggregate(
//                    product = values[0] as Product,
//                    recommendations = values[1] as List<Recommendation>,
//                    reviews = values[2] as List<Review>,
//                    serviceAddress = serviceUtil.serviceAddress
//            )
//        }.doOnError { ex -> logger.warn("getCompositeProduct failed: {}", ex.toString()) }
//                .log()
        return Mono.zip(
                Function { values: Array<Any?> -> createProductAggregate((values[0] as Product?)!!, values[1] as List<Recommendation>, values[2] as List<Review>, serviceUtil.serviceAddress) },
                integration.getProduct(productId),
                integration.getRecommendations(productId).collectList(),
                integration.getReviews(productId).collectList())
                .doOnError(Consumer { ex: Throwable -> logger.warn("getCompositeProduct failed: {}", ex.toString()) })
                .log()
//        return whenComplete(
//                integration.getProduct(productId),
//                integration.getRecommendations(productId).toFlux(),
//                integration.getReviews(productId).toFlux()
//        ).map { it -> i }
    }

    override fun createCompositeProduct(body: ProductAggregate) {
        try {
            logger.debug("createCompositeProduct: creates a new composite entity for productId: {}", body.productId)

            val product = Product(
                    productId = body.productId,
                    name = body.name,
                    weight = body.weight,
                    serviceAddress = serviceUtil.serviceAddress
            )

            integration.createProduct(product)

            body.recommendations.forEach {
                integration.createRecommendation(Recommendation(
                        productId = body.productId,
                        recommendationId = it.recommendationId,
                        author = it.author,
                        rate = it.rate,
                        content = it.content,
                        serviceAddress = serviceUtil.serviceAddress
                ))
            }

            body.reviews.forEach {
                integration.createReview(Review(
                        productId = body.productId,
                        reviewId = it.reviewId,
                        author = it.author,
                        subject = it.subject,
                        content = it.content,
                        serviceAddress = null
                ))
            }

            logger.debug("createCompositeProduct: composite entities created for productId: {}", body.productId)
        } catch (re: RuntimeException) {
            logger.warn("createCompositeProduct failed", re)
            throw re
        }
    }

    override fun deleteCompositeProduct(productId: Int) {
        logger.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId)

        integration.deleteProduct(productId)
        integration.deleteRecommendations(productId)
        integration.deleteReviews(productId)

        logger.debug("deleteCompositeProduct: aggregate entities deleted for productId: {}", productId)
    }

    private fun createProductAggregate(
            product: Product,
            recommendations: List<Recommendation>,
            reviews: List<Review>,
            serviceAddress: String
    ): ProductAggregate {

        val recommendationSummaries = recommendations.map {
            RecommendationSummary(
                    recommendationId = it.recommendationId,
                    author = it.author,
                    rate = it.rate,
                    content = it.content
            )
        }

        val reviewSummaries: List<ReviewSummary> = reviews.map {
            ReviewSummary(
                    reviewId = it.reviewId,
                    author = it.author,
                    subject = it.subject,
                    content = it.content
            )
        }

        val productAddress = product.serviceAddress.orEmpty()
        val reviewAddress = if (reviews.isNotEmpty()) reviews[0].serviceAddress.orEmpty() else ""
        val recommendationAddress = if (recommendations.isNotEmpty()) recommendations[0].serviceAddress.orEmpty() else ""
        val serviceAddresses = ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress)

        return ProductAggregate(
                productId = product.productId,
                name = product.name,
                weight = product.weight,
                recommendations = recommendationSummaries,
                reviews = reviewSummaries,
                serviceAddresses = serviceAddresses
        )
    }
}
