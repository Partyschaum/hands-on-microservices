package de.shinythings.microservices.core.review.services

import de.shinythings.api.core.review.Review
import de.shinythings.api.core.review.ReviewService
import de.shinythings.util.exceptions.InvalidInputException
import de.shinythings.util.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class ReviewServiceImpl(private val serviceUtil: ServiceUtil) : ReviewService {

    private val logger = LoggerFactory.getLogger(ReviewServiceImpl::class.java)

    override fun getReviews(productId: Int): List<Review> {
        if (productId < 1) throw InvalidInputException("Invalid productId: $productId")

        if (productId == 213) {
            logger.debug("No reviews found for productId: {}", productId)
            return ArrayList()
        }

        val list = listOf(
                Review(productId, 1, "Author 1", "Subject 1", "Content 1", serviceUtil.serviceAddress),
                Review(productId, 2, "Author 2", "Subject 2", "Content 2", serviceUtil.serviceAddress),
                Review(productId, 3, "Author 3", "Subject 3", "Content 3", serviceUtil.serviceAddress)
        )

        logger.debug("/reviews response size: {}", list.size)

        return list
    }
}
