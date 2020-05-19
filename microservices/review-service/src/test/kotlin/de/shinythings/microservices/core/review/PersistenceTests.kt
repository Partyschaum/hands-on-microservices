package de.shinythings.microservices.core.review

import de.shinythings.microservices.core.review.persistence.ReviewEntity
import de.shinythings.microservices.core.review.persistence.ReviewRepository
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED
import org.springframework.transaction.annotation.Transactional

@DataJpaTest
@Transactional(propagation = NOT_SUPPORTED)
class PersistenceTests {

    @Autowired
    private lateinit var repository: ReviewRepository

    private lateinit var savedEntity: ReviewEntity

    @BeforeEach
    fun setupDb() {
        repository.deleteAll()

        val entity = ReviewEntity(productId = 1, reviewId = 2, author = "a", subject = "s", content = "c")
        savedEntity = repository.save(entity)

        assertEqualsReview(entity, savedEntity)
    }

    @Test
    fun create() {
        val newEntity = ReviewEntity(productId = 1, reviewId = 3, author = "a", subject = "s", content = "c")
        repository.save(newEntity)

        val foundEntity = repository.findByIdOrNull(newEntity.id!!)!!

        assertEqualsReview(newEntity, foundEntity)
        assertEquals(2, repository.count())
    }

    @Test
    fun update() {
        repository.save(savedEntity.copy(author = "a2"))

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
        val entityList = repository.findByProductId(savedEntity.productId)

        assertThat(entityList, hasSize(1))
        assertEqualsReview(savedEntity, entityList[0])
    }

    @Test
    fun duplicateError() {
        assertThrows<DataIntegrityViolationException> {
            val entity = ReviewEntity(productId = 1, reviewId = 2, author = "a", subject = "s", content = "c")
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

    private fun assertEqualsReview(expectedEntity: ReviewEntity, actualEntity: ReviewEntity) {
        assertEquals(expectedEntity.id, actualEntity.id)
        assertEquals(expectedEntity.version, actualEntity.version)
        assertEquals(expectedEntity.productId, actualEntity.productId)
        assertEquals(expectedEntity.reviewId, actualEntity.reviewId)
        assertEquals(expectedEntity.author, actualEntity.author)
        assertEquals(expectedEntity.subject, actualEntity.subject)
        assertEquals(expectedEntity.content, actualEntity.content)
    }
}
