package de.shinythings.api.core.review

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

interface ReviewService {

    @GetMapping(
            value = ["/review"],
            produces = ["application/json"]
    )
    fun getReviews(@RequestParam(value = "productId", required = true) productId: Int): Flux<Review>

    @PostMapping(
            value = ["/review"],
            consumes = ["application/json"],
            produces = ["application/json"]
    )
    fun createReview(@RequestBody body: Review): Review

    @DeleteMapping(
            value = ["/review"]
    )
    fun deleteReviews(@RequestParam(value = "productId", required = true) productId: Int)
}
