package de.shinythings.microservices.core.recommendation.service

import de.shinythings.api.core.recommendation.Recommendation
import de.shinythings.api.core.recommendation.RecommendationService
import de.shinythings.util.exceptions.InvalidInputException
import de.shinythings.util.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class RecommendationServiceImpl(private val serviceUtil: ServiceUtil) : RecommendationService {

    private val logger = LoggerFactory.getLogger(RecommendationService::class.java)

    override fun getRecommendations(productId: Int): List<Recommendation> {
        if (productId < 1) throw InvalidInputException("Invalid productId: $productId")

        if (productId == 113) {
            logger.debug("No recommendations found for productId: {}", productId)
            return ArrayList()
        }

        val list = listOf(
                Recommendation(productId, 1, "Author 1", 1, "Content 1", serviceUtil.serviceAddress),
                Recommendation(productId, 2, "Author 2", 2, "Content 2", serviceUtil.serviceAddress),
                Recommendation(productId, 3, "Author 3", 3, "Content 3", serviceUtil.serviceAddress)
        )

        logger.debug("/recommendation response size: {}", list.size)

        return list
    }
}
