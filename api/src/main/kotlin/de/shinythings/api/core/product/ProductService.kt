package de.shinythings.api.core.product

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

interface ProductService {

    @GetMapping(
            value = ["/product/{productId}"],
            produces = ["application/json"]
    )
    fun getProduct(@PathVariable productId: Int): Mono<Product>

    @PostMapping(
            value = ["/product"],
            consumes = ["application/json"],
            produces = ["application/json"]
    )
    fun createProduct(@RequestBody body: Product): Product

    @DeleteMapping(
            value = ["/product/{productId}"]
    )
    fun deleteProduct(@PathVariable productId: Int)
}
