package de.shinythings.microservices.core.recommendation.service

import de.shinythings.api.core.recommendation.Recommendation
import de.shinythings.api.core.recommendation.RecommendationService
import de.shinythings.microservices.core.recommendation.persistence.RecommendationRepository
import de.shinythings.util.exceptions.InvalidInputException
import de.shinythings.util.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.web.bind.annotation.RestController

@RestController
class RecommendationServiceImpl(
        private val repository: RecommendationRepository,
        private val mapper: RecommendationMapper,
        private val serviceUtil: ServiceUtil
) : RecommendationService {

    private val logger = LoggerFactory.getLogger(RecommendationService::class.java)

    override fun getRecommendations(productId: Int): List<Recommendation> {
        if (productId < 1) throw InvalidInputException("Invalid productId: $productId")

        val entityList = repository.findByProductId(productId)
        val recommendations = mapper.entityListToApiList(entityList).map {
            it.copy(serviceAddress = serviceUtil.serviceAddress)
        }

        return recommendations.also {
            logger.debug("/recommendation response size: {}", it.size)
        }
    }

    override fun createRecommendation(body: Recommendation): Recommendation {
        return try {
            val entity = mapper.apiToEntity(body)
            val newEntity = repository.save(entity)

            mapper.entityToApi(newEntity).also {
                logger.debug("createRecommendation: created a recommendation entity: {}/{}", it.productId, it.recommendationId)
            }
        } catch (dke: DuplicateKeyException) {
            throw InvalidInputException("Duplicate key, Product Id: ${body.productId}, Recommendation Id: ${body.recommendationId}")
        }
    }

    override fun deleteRecommendations(productId: Int) {
        logger.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId)

        repository.deleteAll(repository.findByProductId(productId))
    }
}
