package de.shinythings.api.core.review

import com.github.pozo.KotlinBuilder

@KotlinBuilder
data class Review(
        val productId: Int,
        val reviewId: Int,
        val author: String,
        val subject: String,
        val content: String,
        val serviceAddress: String?
)
