package de.shinythings.microservices.core.review.persistence

import com.github.pozo.KotlinBuilder
import javax.persistence.*

@Entity
@Table(
        name = "reviews",
        indexes = [Index(name = "reviews_unique_idx", unique = true, columnList = "productId,reviewId")]
)
@KotlinBuilder
data class ReviewEntity(

        @Id
        @GeneratedValue
        val id: Int?,

        @Version
        val version: Int?,

        val productId: Int,
        val reviewId: Int,
        val author: String,
        val subject: String,
        val content: String
)
