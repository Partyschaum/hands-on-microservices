package de.shinythings.microservices.core.review.services

import de.shinythings.api.core.review.Review
import de.shinythings.api.core.review.ReviewService
import de.shinythings.microservices.core.review.persistence.ReviewRepository
import de.shinythings.util.exceptions.InvalidInputException
import de.shinythings.util.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.bind.annotation.RestController

@RestController
class ReviewServiceImpl(
        private val repository: ReviewRepository,
        private val mapper: ReviewMapper,
        private val serviceUtil: ServiceUtil
) : ReviewService {

    private val logger = LoggerFactory.getLogger(ReviewServiceImpl::class.java)

    override fun getReviews(productId: Int): List<Review> {
        if (productId < 1) throw InvalidInputException("Invalid productId: $productId")

        val entityList = repository.findByProductId(productId)
        val reviews = mapper.entityListToApiList(entityList).map {
            it.copy(serviceAddress = serviceUtil.serviceAddress)
        }

        return reviews.also {
            logger.debug("getReviews: response size: {}", reviews.size)
        }
    }

    override fun createReview(body: Review): Review {
        return try {
            val entity = mapper.apiToEntity(body)
            val newEntity = repository.save(entity)

            mapper.entityToApi(newEntity).also {
                logger.debug("createReview: created a review entity: {}/{}", it.productId, it.reviewId)
            }
        } catch (dive: DataIntegrityViolationException) {
            throw InvalidInputException("Duplicate key, Product Id: ${body.productId}, Review Id: ${body.reviewId}")
        }
    }

    override fun deleteReviews(productId: Int) {
        logger.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId)

        repository.deleteAll(repository.findByProductId(productId))
    }
}
