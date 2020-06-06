package de.shinythings.microservices.core.product

import de.shinythings.microservices.core.product.persistence.ClassToBeFound
import de.shinythings.microservices.core.product.persistence.ProductEntity
import de.shinythings.microservices.core.product.persistence.ProductRepository
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
        val bean = applicationContext.getBean(ClassToBeFound::class.java)

        Assert.notNull(bean, "Bean not found!")
    }
}
