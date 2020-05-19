package de.shinythings.microservices.core.product.services

import de.shinythings.api.core.product.Product
import de.shinythings.api.core.product.ProductService
import de.shinythings.microservices.core.product.persistence.ProductEntity
import de.shinythings.microservices.core.product.persistence.ProductRepository
import de.shinythings.util.exceptions.InvalidInputException
import de.shinythings.util.exceptions.NotFoundException
import de.shinythings.util.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductServiceImpl(
        private val repository: ProductRepository,
        private val mapper: ProductMapper,
        private val serviceUtil: ServiceUtil
) : ProductService {

    private val logger = LoggerFactory.getLogger(ProductServiceImpl::class.java)

    override fun getProduct(productId: Int): Product {
        logger.debug("/product return the found product for productId={}", productId)

        if (productId < 1) throw InvalidInputException("Invalid productId: $productId")

        val entity = repository.findByProductId(productId)
                ?: throw NotFoundException("No product found for productId: $productId")

        return mapper.entityToApi(entity).copy(serviceAddress = serviceUtil.serviceAddress).also {
            logger.debug("getProduct: found productId: {}", it.productId)
        }
    }

    override fun createProduct(body: Product): Product {
        return try {
            val entity: ProductEntity = mapper.apiToEntity(body)
            val newEntity: ProductEntity = repository.save(entity)

            logger.debug("createProduct: entity created for productId: {}", body.productId)

            mapper.entityToApi(newEntity).copy(serviceAddress = serviceUtil.serviceAddress)
        } catch (dke: DuplicateKeyException) {
            throw InvalidInputException("Duplicate key, Product Id: " + body.productId)
        }
    }

    override fun deleteProduct(productId: Int) {
        logger.debug("deleteProduct: tries to delete an entity with productId: {}", productId)

        repository.findByProductId(productId)?.let {
            repository.delete(it)
        }
    }
}
