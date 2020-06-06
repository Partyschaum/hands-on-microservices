package de.shinythings.microservices.core.recommendation

import de.shinythings.microservices.core.recommendation.persistence.RecommendationRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.util.Assert

@SpringBootTest
class BeanLoadingDebugging {

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Test
    fun test() {
        val bean = applicationContext.getBean(RecommendationRepository::class.java)

        Assert.notNull(bean, "Bean not found!")
    }
}
