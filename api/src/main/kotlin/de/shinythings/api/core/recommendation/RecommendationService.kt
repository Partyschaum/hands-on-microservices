package de.shinythings.api.core.recommendation

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

interface RecommendationService {

    @GetMapping(
            value = ["/recommendation"],
            produces = ["application/json"]
    )
    fun getRecommendations(@RequestParam(value = "productId", required = true) productId: Int): Flux<Recommendation>

    @PostMapping(
            value = ["/recommendation"],
            consumes = ["application/json"],
            produces = ["application/json"]
    )
    fun createRecommendation(@RequestBody body: Recommendation): Recommendation

    @DeleteMapping(
            value = ["/recommendation"]
    )
    fun deleteRecommendations(@RequestParam(value = "productId", required = true) productId: Int)
}
