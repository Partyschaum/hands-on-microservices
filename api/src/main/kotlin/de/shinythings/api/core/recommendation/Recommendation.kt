package de.shinythings.api.core.recommendation

import com.github.pozo.KotlinBuilder

@KotlinBuilder
data class Recommendation(
        val productId: Int,
        val recommendationId: Int,
        val author: String,
        val rate: Int,
        val content: String,
        val serviceAddress: String?
)
