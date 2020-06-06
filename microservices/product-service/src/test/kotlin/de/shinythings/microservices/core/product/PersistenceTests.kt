package de.shinythings.microservices.core.product

import de.shinythings.microservices.core.product.persistence.ProductEntity
import de.shinythings.microservices.core.product.persistence.ProductRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.OptimisticLockingFailureException
import reactor.test.StepVerifier

@DataMongoTest
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class PersistenceTests {

    @Autowired
    private lateinit var repository: ProductRepository

    private lateinit var savedEntity: ProductEntity

    @BeforeEach
    fun setupDb() {
        StepVerifier.create(repository.deleteAll()).verifyComplete()

        val entity = ProductEntity(productId = 1, name = "n", weight = 1)

        StepVerifier.create(repository.save(entity))
                .expectNextMatches { savedEntity ->
                    areProductsEqual(entity, savedEntity)
                }
                .verifyComplete()
    }

    @Test
    fun create() {
        val newEntity = ProductEntity(productId = 2, name = "n", weight = 2)

        StepVerifier.create(repository.save(newEntity))
                .expectNextMatches { savedEntity ->
                    newEntity.productId == savedEntity.productId
                }
                .verifyComplete()

        StepVerifier.create(repository.findById(newEntity.id!!))
                .expectNextMatches { foundEntity ->
                    areProductsEqual(newEntity, foundEntity)
                }
                .verifyComplete()

        StepVerifier.create(repository.count()).expectNext(2).verifyComplete()
    }

    @Test
    fun update() {
        savedEntity = savedEntity.copy(name = "n2")

        StepVerifier.create(repository.save(savedEntity))
                .expectNextMatches { savedEntity ->
                    savedEntity.version == 1 && savedEntity.name == "n2"
                }
                .verifyComplete()

        StepVerifier.create(repository.findById(savedEntity.id!!))
                .expectNextMatches { foundEntity ->
                    foundEntity.version == 1 && foundEntity.name == "n2"
                }
                .verifyComplete()
    }

    @Test
    fun delete() {
        StepVerifier.create(repository.delete(savedEntity)).verifyComplete()
        StepVerifier.create(repository.existsById(savedEntity.id!!)).expectNext(false).verifyComplete()
    }

    @Test
    fun getByProductId() {
        StepVerifier.create(repository.findByProductId(savedEntity.productId))
                .expectNextMatches { foundEntity ->
                    areProductsEqual(savedEntity, foundEntity)
                }
                .verifyComplete()
    }

    @Test
    fun duplicateError() {
        val entity = ProductEntity(productId = savedEntity.productId, name = "n", weight = 1)

        StepVerifier.create(repository.save(entity))
                .expectError(DuplicateKeyException::class.java)
                .verify()
    }

    @Test
    fun optimisticLockError() {
        // Store the saved entity in two separate entity objects
        val entity1 = repository.findById(savedEntity.id!!).block()!!
        val entity2 = repository.findById(savedEntity.id!!).block()!!

        // Update the entity using the first entity object
        repository.save(entity1.copy(name = "n1")).block()

        // Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        StepVerifier.create(repository.save(entity2))
                .expectError(OptimisticLockingFailureException::class.java)
                .verify()

        // Get the updated entity from the database and verify its new state
        StepVerifier.create(repository.findById(savedEntity.id!!))
                .expectNextMatches { foundEntity ->
                    foundEntity.version == 1 && foundEntity.name == "n1"
                }
    }

    private fun areProductsEqual(expectedEntity: ProductEntity, actualEntity: ProductEntity): Boolean {
        return expectedEntity.id == actualEntity.id &&
                expectedEntity.version == actualEntity.version &&
                expectedEntity.productId == actualEntity.productId &&
                expectedEntity.name == actualEntity.name &&
                expectedEntity.weight == actualEntity.weight
    }
}
