package de.shinythings.microservices.core.review

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.core.env.get
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import java.util.concurrent.Executors

private val logger = LoggerFactory.getLogger(ReviewServiceApplication::class.java)

@SpringBootApplication
@ComponentScan("de.shinythings")
class ReviewServiceApplication(
        @Value("\${spring.datasource.maximum-pool-size:10}") private val connectionPoolSize: Int
) {

    @Bean
    fun jdbcScheduler(): Scheduler {
        logger.info("Creates a jdbcScheduler with connectionPoolSize = $connectionPoolSize")

        return Schedulers.fromExecutor(Executors.newFixedThreadPool(connectionPoolSize))
    }
}

fun main(args: Array<String>) {
    val ctx = runApplication<ReviewServiceApplication>(*args)

    val mysqlUri = ctx.environment["spring.datasource.url"]
            ?: error("No value set for 'spring.datasource.url'.")

    logger.info("Connected to MySQL: $mysqlUri")
}
