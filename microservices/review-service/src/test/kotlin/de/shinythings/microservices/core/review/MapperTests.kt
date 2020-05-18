package de.shinythings.microservices.core.review

import de.shinythings.api.core.review.Review
import de.shinythings.microservices.core.review.persistence.ReviewEntity
import de.shinythings.microservices.core.review.services.ReviewMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mapstruct.factory.Mappers

class MapperTests {

    private val mapper: ReviewMapper = Mappers.getMapper(ReviewMapper::class.java)

    @Test
    fun mapperTests() {
        val api = Review(
                productId = 1,
                reviewId = 2,
                author = "a",
                subject = "s",
                content = "C",
                serviceAddress = "adr"
        )

        val entity: ReviewEntity = mapper.apiToEntity(api)

        assertEquals(api.productId, entity.productId)
        assertEquals(api.reviewId, entity.reviewId)
        assertEquals(api.author, entity.author)
        assertEquals(api.subject, entity.subject)
        assertEquals(api.content, entity.content)

        val api2: Review = mapper.entityToApi(entity)

        assertEquals(api.productId, api2.productId)
        assertEquals(api.reviewId, api2.reviewId)
        assertEquals(api.author, api2.author)
        assertEquals(api.subject, api2.subject)
        assertEquals(api.content, api2.content)
        assertNull(api2.serviceAddress)
    }

    @Test
    fun mapperListTests() {
        val api = Review(
                productId = 1,
                reviewId = 2,
                author = "a",
                subject = "s",
                content = "C",
                serviceAddress = "adr"
        )

        val apiList = listOf(api)
        val entityList = mapper.apiListToEntityList(apiList)

        assertEquals(apiList.size, entityList.size)

        val entity: ReviewEntity = entityList[0]

        assertEquals(api.productId, entity.productId)
        assertEquals(api.reviewId, entity.reviewId)
        assertEquals(api.author, entity.author)
        assertEquals(api.subject, entity.subject)
        assertEquals(api.content, entity.content)

        val api2List: List<Review> = mapper.entityListToApiList(entityList)

        assertEquals(apiList.size, api2List.size)

        val api2: Review = api2List[0]

        assertEquals(api.productId, api2.productId)
        assertEquals(api.reviewId, api2.reviewId)
        assertEquals(api.author, api2.author)
        assertEquals(api.subject, api2.subject)
        assertEquals(api.content, api2.content)
        assertNull(api2.serviceAddress)
    }
}
