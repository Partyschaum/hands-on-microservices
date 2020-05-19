package de.shinythings.microservices.core.recommendation.persistence

import com.github.pozo.KotlinBuilder
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "recommendations")
@CompoundIndex(name = "product-recommendation-id", unique = true, def = "{'productId': 1, 'recommendationId': 1}")
@KotlinBuilder
data class RecommendationEntity(

        @Id
        var id: String? = null,

        @Version
        var version: Int? = null,

        val productId: Int,
        val recommendationId: Int,
        val author: String,
        val rating: Int,
        val content: String
)
