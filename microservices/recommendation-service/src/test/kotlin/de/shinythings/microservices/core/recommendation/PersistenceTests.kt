package de.shinythings.microservices.core.recommendation

import de.shinythings.microservices.core.recommendation.persistence.RecommendationEntity
import de.shinythings.microservices.core.recommendation.persistence.RecommendationRepository
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.repository.findByIdOrNull

@DataMongoTest
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class PersistenceTests {

    @Autowired
    private lateinit var repository: RecommendationRepository

    private lateinit var savedEntity: RecommendationEntity

    @BeforeEach
    fun setupDb() {
        repository.deleteAll()

        val entity = RecommendationEntity(productId = 1, recommendationId = 2, author = "a", rating = 3, content = "c")
        savedEntity = repository.save(entity)

        assertEqualsRecommendation(entity, savedEntity)
    }

    @Test
    fun create() {
        val newEntity = RecommendationEntity(productId = 1, recommendationId = 3, author = "a", rating = 3, content = "c")
        repository.save(newEntity)

        val foundEntity = repository.findByIdOrNull(newEntity.id!!)!!

        assertEqualsRecommendation(newEntity, foundEntity)
        assertEquals(2, repository.count())
    }

    @Test
    fun update() {
        savedEntity = savedEntity.copy(author = "a2")
        repository.save(savedEntity)

        val foundEntity = repository.findByIdOrNull(savedEntity.id!!)!!

        assertEquals(1, foundEntity.version)
        assertEquals("a2", foundEntity.author)
    }

    @Test
    fun delete() {
        repository.delete(savedEntity)

        assertFalse(repository.existsById(savedEntity.id!!))
    }

    @Test
    fun getByProductId() {
        val newEntity = RecommendationEntity(productId = 1, recommendationId = 3, author = "a", rating = 3, content = "c")
        repository.save(newEntity)

        val entityList = repository.findByProductId(savedEntity.productId)

        assertThat(entityList, hasSize(2))
        assertEqualsRecommendation(savedEntity, entityList[0])
    }

    @Test
    fun duplicateError() {
        assertThrows<DuplicateKeyException> {
            val entity = RecommendationEntity(productId = 1, recommendationId = 2, author = "a", rating = 3, content = "c")
            repository.save(entity)
        }
    }

    @Test
    fun optimisticLockError() {
        // Store the saved entity in two separate entity objects
        val entity1 = repository.findByIdOrNull(savedEntity.id!!)!!
        val entity2 = repository.findByIdOrNull(savedEntity.id!!)!!

        // Update the entity using the first entity object
        repository.save(entity1.copy(author = "a1"))

        // Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        try {
            repository.save(entity2.copy(author = "a2"))
            fail("Expected an OptimisticLockingFailureException")
        } catch (e: OptimisticLockingFailureException) {
        }

        // Get the updated entity from the database and verify its new sate
        val updatedEntity = repository.findByIdOrNull(savedEntity.id!!)!!

        assertEquals(1, updatedEntity.version)
        assertEquals("a1", updatedEntity.author)
    }

    private fun assertEqualsRecommendation(expectedEntity: RecommendationEntity, actualEntity: RecommendationEntity) {
        assertEquals(expectedEntity.id, actualEntity.id)
        assertEquals(expectedEntity.version, actualEntity.version)
        assertEquals(expectedEntity.productId, actualEntity.productId)
        assertEquals(expectedEntity.recommendationId, actualEntity.recommendationId)
        assertEquals(expectedEntity.author, actualEntity.author)
        assertEquals(expectedEntity.rating, actualEntity.rating)
        assertEquals(expectedEntity.content, actualEntity.content)
    }
}
