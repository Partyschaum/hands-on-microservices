package de.shinythings.microservices.composite.product.services

import de.shinythings.api.composite.product.*
import de.shinythings.api.core.product.Product
import de.shinythings.api.core.recommendation.Recommendation
import de.shinythings.api.core.review.Review
import de.shinythings.util.exceptions.NotFoundException
import de.shinythings.util.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductCompositeServiceImpl(
        private val integration: ProductCompositeIntegration,
        private val serviceUtil: ServiceUtil
) : ProductCompositeService {

    private val logger = LoggerFactory.getLogger(ProductCompositeServiceImpl::class.java)

    override fun getCompositeProduct(productId: Int): ProductAggregate {
        logger.debug("getCompositeProduct: lookup a product aggregate for productId: {}", productId)

        val product = integration.getProduct(productId)
                ?: throw NotFoundException("No product found for productId: $productId")

        val recommendations = integration.getRecommendations(productId)

        val reviews = integration.getReviews(productId)

        logger.debug("getCompositeProduct: aggregate entity found for productId: {}", productId)

        return createProductAggregate(product, recommendations, reviews, serviceUtil.serviceAddress)
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
