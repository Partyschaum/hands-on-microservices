package de.shinythings.api.core.review

import org.springframework.web.bind.annotation.*

interface ReviewService {

    @GetMapping(
            value = ["/review"],
            produces = ["application/json"]
    )
    fun getReviews(@RequestParam(value = "productId", required = true) productId: Int): List<Review>

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
