package de.shinythings.microservices.core.product.services

import de.shinythings.api.core.product.Product
import de.shinythings.api.core.product.ProductService
import de.shinythings.util.exceptions.InvalidInputException
import de.shinythings.util.exceptions.NotFoundException
import de.shinythings.util.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductServiceImpl(private val serviceUtil: ServiceUtil) : ProductService {

    private val logger = LoggerFactory.getLogger(ProductServiceImpl::class.java)

    override fun getProduct(productId: Int): Product {
        logger.debug("/product return the found product for productId={}", productId)

        if (productId < 1) throw InvalidInputException("Invalid productId: $productId")

        if (productId == 13) throw NotFoundException("No product found for productId: $productId")

        return Product(
                productId = productId,
                name = "name-$productId",
                weight = 123,
                serviceAddress = serviceUtil.serviceAddress
        )
    }
}
