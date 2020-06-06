package de.shinythings.microservices.core.review.services

import de.shinythings.api.core.review.Review
import de.shinythings.api.core.review.ReviewService
import de.shinythings.microservices.core.review.persistence.ReviewRepository
import de.shinythings.util.exceptions.InvalidInputException
import de.shinythings.util.http.ServiceUtil
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.scheduler.Scheduler
import java.util.function.Supplier
import java.util.logging.Level

@RestController
class ReviewServiceImpl(
        private val repository: ReviewRepository,
        private val mapper: ReviewMapper,
        private val serviceUtil: ServiceUtil,
        private val scheduler: Scheduler
) : ReviewService {

    private val logger = LoggerFactory.getLogger(ReviewServiceImpl::class.java)

    override fun getReviews(productId: Int): Flux<Review> {
        if (productId < 1) throw InvalidInputException("Invalid productId: $productId")

        return asyncFlux(Supplier<Publisher<Review>> { Flux.fromIterable(getByProductId(productId)) }).log(null, Level.FINE)
    }

    private fun asyncFlux(publisherSupplier: Supplier<Publisher<Review>>): Flux<Review> {
        return Flux.defer(publisherSupplier).subscribeOn(scheduler)
    }

    private fun getByProductId(productId: Int): List<Review> {
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
