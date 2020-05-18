package de.shinythings.microservices.core.recommendation

import de.shinythings.api.core.recommendation.Recommendation
import de.shinythings.microservices.core.recommendation.persistence.RecommendationEntity
import de.shinythings.microservices.core.recommendation.service.RecommendationMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mapstruct.factory.Mappers

class MapperTests {

    private val mapper: RecommendationMapper = Mappers.getMapper(RecommendationMapper::class.java)

    @Test
    fun mapperTests() {
        val api = Recommendation(
                productId = 1,
                recommendationId = 2,
                author = "a",
                rate = 4,
                content = "C",
                serviceAddress = "adr"
        )

        val entity: RecommendationEntity = mapper.apiToEntity(api)

        assertEquals(api.productId, entity.productId)
        assertEquals(api.recommendationId, entity.recommendationId)
        assertEquals(api.author, entity.author)
        assertEquals(api.rate, entity.rating)
        assertEquals(api.content, entity.content)

        val api2: Recommendation = mapper.entityToApi(entity)

        assertEquals(api.productId, api2.productId)
        assertEquals(api.recommendationId, api2.recommendationId)
        assertEquals(api.author, api2.author)
        assertEquals(api.rate, api2.rate)
        assertEquals(api.content, api2.content)
        assertNull(api2.serviceAddress)
    }

    @Test
    fun mapperListTests() {
        val api = Recommendation(
                productId = 1,
                recommendationId = 2,
                author = "a",
                rate = 4,
                content = "C",
                serviceAddress = "adr"
        )

        val apiList = listOf(api)
        val entityList = mapper.apiListToEntityList(apiList)

        assertEquals(apiList.size, entityList.size)

        val entity: RecommendationEntity = entityList[0]

        assertEquals(api.productId, entity.productId)
        assertEquals(api.recommendationId, entity.recommendationId)
        assertEquals(api.author, entity.author)
        assertEquals(api.rate, entity.rating)
        assertEquals(api.content, entity.content)

        val api2List: List<Recommendation> = mapper.entityListToApiList(entityList)

        assertEquals(apiList.size, api2List.size)

        val api2: Recommendation = api2List[0]

        assertEquals(api.productId, api2.productId)
        assertEquals(api.recommendationId, api2.recommendationId)
        assertEquals(api.author, api2.author)
        assertEquals(api.rate, api2.rate)
        assertEquals(api.content, api2.content)
        assertNull(api2.serviceAddress)
    }
}
