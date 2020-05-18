package de.shinythings.microservices.composite.product.services

import de.shinythings.api.composite.product.*
import de.shinythings.api.core.product.Product
import de.shinythings.api.core.recommendation.Recommendation
import de.shinythings.api.core.review.Review
import de.shinythings.util.exceptions.NotFoundException
import de.shinythings.util.http.ServiceUtil
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductCompositeServiceImpl(
        private val integration: ProductCompositeIntegration,
        private val serviceUtil: ServiceUtil
) : ProductCompositeService {

    override fun getProduct(productId: Int): ProductAggregate {
        val product = integration.getProduct(productId)
                ?: throw NotFoundException("No product found for productId: $productId")

        val recommendations = integration.getRecommendations(productId)

        val reviews = integration.getReviews(productId)

        return createProductAggregate(product, recommendations, reviews, serviceUtil.serviceAddress)
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
                    rate = it.rate
            )
        }

        val reviewSummaries: List<ReviewSummary> = reviews.map {
            ReviewSummary(
                    reviewId = it.reviewId,
                    author = it.author,
                    subject = it.subject
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
