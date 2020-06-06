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

@DataMongoTest
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class PersistenceTests {

    @Autowired
    private lateinit var repository: RecommendationRepository

    private lateinit var savedEntity: RecommendationEntity

    @BeforeEach
    fun setupDb() {
        repository.deleteAll().block()

        val entity = RecommendationEntity(productId = 1, recommendationId = 2, author = "a", rating = 3, content = "c")
        savedEntity = repository.save(entity).block()!!

        assertEqualsRecommendation(entity, savedEntity)
    }

    @Test
    fun create() {
        val newEntity = RecommendationEntity(productId = 1, recommendationId = 3, author = "a", rating = 3, content = "c")
        repository.save(newEntity).block()

        val foundEntity = repository.findById(newEntity.id!!).block()!!

        assertEqualsRecommendation(newEntity, foundEntity)
        assertEquals(2, repository.count())
    }

    @Test
    fun update() {
        savedEntity = savedEntity.copy(author = "a2")
        repository.save(savedEntity).block()

        val foundEntity = repository.findById(savedEntity.id!!).block()!!

        assertEquals(1, foundEntity.version)
        assertEquals("a2", foundEntity.author)
    }

    @Test
    fun delete() {
        repository.delete(savedEntity).block()

        assertFalse(repository.existsById(savedEntity.id!!).block()!!)
    }

    @Test
    fun getByProductId() {
        val newEntity = RecommendationEntity(productId = 1, recommendationId = 3, author = "a", rating = 3, content = "c")
        repository.save(newEntity).block()

        val entityList = repository.findByProductId(savedEntity.productId).collectList().block()!!

        assertThat(entityList, hasSize(2))
        assertEqualsRecommendation(savedEntity, entityList[0])
    }

    @Test
    fun duplicateError() {
        assertThrows<DuplicateKeyException> {
            val entity = RecommendationEntity(productId = 1, recommendationId = 2, author = "a", rating = 3, content = "c")
            repository.save(entity).block()
        }
    }

    @Test
    fun optimisticLockError() {
        // Store the saved entity in two separate entity objects
        val entity1 = repository.findById(savedEntity.id!!).block()!!
        val entity2 = repository.findById(savedEntity.id!!).block()!!

        // Update the entity using the first entity object
        repository.save(entity1.copy(author = "a1")).block()

        // Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        try {
            repository.save(entity2.copy(author = "a2")).block()
            fail("Expected an OptimisticLockingFailureException")
        } catch (e: OptimisticLockingFailureException) {
        }

        // Get the updated entity from the database and verify its new sate
        val updatedEntity = repository.findById(savedEntity.id!!).block()!!

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
