package de.shinythings.api.core.recommendation

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

interface RecommendationService {

    @GetMapping(
            value = ["/recommendation"],
            produces = ["application/json"]
    )
    fun getRecommendations(@RequestParam(value = "productId", required = true) productId: Int): List<Recommendation>
}
