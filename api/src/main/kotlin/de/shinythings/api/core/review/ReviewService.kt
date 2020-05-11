package de.shinythings.api.core.review

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

interface ReviewService {

    @GetMapping(
            value = ["/review"],
            produces = ["application/json"]
    )
    fun getReviews(@RequestParam(value = "productId", required = true) productId: Int): List<Review>
}
