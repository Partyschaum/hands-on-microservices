package de.shinythings.microservices.core.product.persistence

import com.github.pozo.KotlinBuilder
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "products")
@KotlinBuilder
data class ProductEntity(

        @Id
        val id: String?,

        @Version
        val version: Int?,

        @Indexed(unique = true)
        val productId: Int,

        val name: String,
        val weight: Int
)
