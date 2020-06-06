package de.shinythings.microservices.core.recommendation.service

import de.shinythings.api.core.recommendation.Recommendation
import de.shinythings.api.core.recommendation.RecommendationService
import de.shinythings.microservices.core.recommendation.persistence.RecommendationRepository
import de.shinythings.util.exceptions.InvalidInputException
import de.shinythings.util.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class RecommendationServiceImpl(
        private val repository: RecommendationRepository,
        private val mapper: RecommendationMapper,
        private val serviceUtil: ServiceUtil
) : RecommendationService {

    private val logger = LoggerFactory.getLogger(RecommendationService::class.java)

    override fun getRecommendations(productId: Int): Flux<Recommendation> {
        if (productId < 1) throw InvalidInputException("Invalid productId: $productId")

        return repository.findByProductId(productId)
                .log()
                .map { mapper.entityToApi(it!!) }
                .map { it!!.copy(serviceAddress = serviceUtil.serviceAddress) }
    }

    override fun createRecommendation(body: Recommendation): Recommendation {

        if (body.productId < 1) throw InvalidInputException("Invalid productId: ${body.productId}")

        val entity = mapper.apiToEntity(body)

        return repository.save(entity)
                .log()
                .onErrorMap(DuplicateKeyException::class.java) { InvalidInputException("Duplicate key, Product Id: ${body.productId}, Recommendation Id: ${body.recommendationId}") }
                .map { mapper.entityToApi(it) }
                .block()!!
    }

    override fun deleteRecommendations(productId: Int) {
        logger.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId)

        repository.deleteAll(repository.findByProductId(productId))
    }
}
