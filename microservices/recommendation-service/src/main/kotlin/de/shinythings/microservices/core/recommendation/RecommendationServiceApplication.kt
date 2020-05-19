package de.shinythings.microservices.core.recommendation

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.core.env.get

@SpringBootApplication
@ComponentScan("de.shinythings")
class RecommendationServiceApplication

fun main(args: Array<String>) {
	val logger = LoggerFactory.getLogger(RecommendationServiceApplication::class.java)

	val ctx = runApplication<RecommendationServiceApplication>(*args)

	val mongoDbHost = ctx.environment["spring.data.mongodb.host"]
			?: error("No value set for 'spring.data.mongodb.host'.")
	val mongoDbPort = ctx.environment["spring.data.mongodb.port"]
			?: error("No value set for 'spring.data.mongodb.port'.")

	logger.info("Connected to MongoDB: $mongoDbHost:$mongoDbPort")
}
