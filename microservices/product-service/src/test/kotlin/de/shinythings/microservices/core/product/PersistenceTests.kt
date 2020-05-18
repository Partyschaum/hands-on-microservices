package de.shinythings.microservices.core.product

import de.shinythings.microservices.core.product.persistence.ProductEntity
import de.shinythings.microservices.core.product.persistence.ProductRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull

@DataMongoTest
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class PersistenceTests {

    @Autowired
    private lateinit var repository: ProductRepository

    private lateinit var savedEntity: ProductEntity

    @BeforeEach
    fun setupDb() {
        repository.deleteAll()

        val entity = ProductEntity(productId = 1, name = "n", weight = 1)
        savedEntity = repository.save(entity)

        assertEqualsProduct(entity, savedEntity)
    }

    @Test
    fun create() {
        val newEntity = ProductEntity(productId = 2, name = "n", weight = 2)
        repository.save(newEntity)

        val foundEntity = repository.findByIdOrNull(newEntity.id!!)!!

        assertEqualsProduct(newEntity, foundEntity)
        assertEquals(2, repository.count())
    }

    @Test
    fun update() {
        savedEntity = savedEntity.copy(name = "n2")
        repository.save(savedEntity)

        val foundEntity = repository.findByIdOrNull(savedEntity.id!!)!!

        assertEquals(1, foundEntity.version)
        assertEquals("n2", foundEntity.name)
    }

    @Test
    fun delete() {
        repository.delete(savedEntity)

        assertFalse(repository.existsById(savedEntity.id!!))
    }

    @Test
    fun getByProductId() {
        val entity = repository.findByProductId(savedEntity.productId)!!

        assertEqualsProduct(savedEntity, entity)
    }

    @Test
    fun duplicateError() {
        assertThrows<DuplicateKeyException> {
            val entity = ProductEntity(productId = savedEntity.productId, name = "n", weight = 1)
            repository.save(entity)
        }
    }

    @Test
    fun optimisticLockError() {
        // Store the saved entity in two separate entity objects
        val entity1 = repository.findByIdOrNull(savedEntity.id)!!
        val entity2 = repository.findByIdOrNull(savedEntity.id)!!

        // Update the entity using the first entity object
        repository.save(entity1.copy(name = "n1"))

        // Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        try {
            repository.save(entity2)
            fail("Expected an OptimisticLockingFailureException")
        } catch (e: OptimisticLockingFailureException) {
        }

        // Get the updated entity from the database and verify its new state
        val updatedEntity = repository.findByIdOrNull(savedEntity.id)!!

        assertEquals(1, updatedEntity.version)
        assertEquals("n1", updatedEntity.name)
    }

    @Test
    fun paging() {
        repository.deleteAll()

        val newProducts = (1001..1010).map {
            ProductEntity(productId = it, name = "name $it", weight = it)
        }

        repository.saveAll(newProducts)

        var nextPage: Pageable = PageRequest.of(0, 4, Sort.Direction.ASC, "productId")

        nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]", true)
        nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true)
        testNextPage(nextPage, "[1009, 1010]", false)
    }

    private fun testNextPage(nextPage: Pageable, expectedProductIds: String, expectsNextPage: Boolean): Pageable {
        val productPage = repository.findAll(nextPage)
        assertEquals(expectedProductIds, productPage.content.map { p -> p.productId }.toString())
        assertEquals(expectsNextPage, productPage.hasNext())
        return productPage.nextPageable()
    }

    private fun assertEqualsProduct(expectedEntity: ProductEntity, actualEntity: ProductEntity) {
        assertEquals(expectedEntity.id, actualEntity.id)
        assertEquals(expectedEntity.version, actualEntity.version)
        assertEquals(expectedEntity.productId, actualEntity.productId)
        assertEquals(expectedEntity.name, actualEntity.name)
        assertEquals(expectedEntity.weight, actualEntity.weight)
    }
}
