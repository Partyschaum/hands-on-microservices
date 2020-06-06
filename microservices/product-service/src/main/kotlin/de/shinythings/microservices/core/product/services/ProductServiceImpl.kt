package de.shinythings.microservices.core.product.services

import de.shinythings.api.core.product.Product
import de.shinythings.api.core.product.ProductService
import de.shinythings.microservices.core.product.persistence.ProductEntity
import de.shinythings.microservices.core.product.persistence.ProductRepository
import de.shinythings.util.exceptions.InvalidInputException
import de.shinythings.util.exceptions.NotFoundException
import de.shinythings.util.http.ServiceUtil
import org.springframework.dao.DuplicateKeyException
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@RestController
class ProductServiceImpl(
        private val repository: ProductRepository,
        private val mapper: ProductMapper,
        private val serviceUtil: ServiceUtil
) : ProductService {

    override fun getProduct(productId: Int): Mono<Product> {
        if (productId < 1) throw InvalidInputException("Invalid productId: $productId")

        return repository.findByProductId(productId)
                .switchIfEmpty(NotFoundException("No product found for productId: $productId").toMono())
                .log()
                .map { mapper.entityToApi(it!!) }
                .map { it!!.copy(serviceAddress = serviceUtil.serviceAddress) }
    }

    override fun createProduct(body: Product): Product {
        if (body.productId < 1) throw InvalidInputException("Invalid productId: $body.productId")

        val entity: ProductEntity = mapper.apiToEntity(body)

        return repository.save(entity)
                .log()
                .onErrorMap(DuplicateKeyException::class.java) {
                    InvalidInputException("Duplicate key, Product Id: " + body.productId)
                }
                .map { mapper.entityToApi(it) }
                .map { it.copy(serviceAddress = serviceUtil.serviceAddress) }
                .block()!!
    }

    override fun deleteProduct(productId: Int) {
        if (productId < 1) throw InvalidInputException("Invalid productId: $productId")

        repository.findByProductId(productId)
                .log()
                .map { repository.delete(it!!) }
                .block()
    }
}
