package de.shinythings.microservices.core.recommendation

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan("de.shinythings")
class RecommendationServiceApplication

fun main(args: Array<String>) {
	runApplication<RecommendationServiceApplication>(*args)
}
