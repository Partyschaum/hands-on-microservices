package de.shinythings.microservices.core.product

import de.shinythings.api.core.product.Product
import de.shinythings.microservices.core.product.persistence.ProductEntity
import de.shinythings.microservices.core.product.services.ProductMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mapstruct.factory.Mappers

class MapperTests {

    private val mapper = Mappers.getMapper(ProductMapper::class.java)

    @Test
    fun mapperTests() {
        val api = Product(
                productId = 1,
                name = "n",
                weight = 1,
                serviceAddress = "sa"
        )

        val entity: ProductEntity = mapper.apiToEntity(api)

        assertEquals(api.productId, entity.productId)
        assertEquals(api.productId, entity.productId)
        assertEquals(api.name, entity.name)
        assertEquals(api.weight, entity.weight)

        val api2: Product = mapper.entityToApi(entity)

        assertEquals(api.productId, api2.productId)
        assertEquals(api.productId, api2.productId)
        assertEquals(api.name, api2.name)
        assertEquals(api.weight, api2.weight)
        assertNull(api2.serviceAddress)
    }
}
