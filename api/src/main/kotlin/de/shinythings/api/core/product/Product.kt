package de.shinythings.api.core.product

import com.github.pozo.KotlinBuilder

@KotlinBuilder
data class Product(
        val productId: Int,
        val name: String,
        val weight: Int,
        val serviceAddress: String?
)
