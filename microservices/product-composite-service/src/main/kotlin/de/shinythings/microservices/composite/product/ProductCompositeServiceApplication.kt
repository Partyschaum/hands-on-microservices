package de.shinythings.microservices.composite.product

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.client.RestTemplate
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2WebFlux

@EnableSwagger2WebFlux
@SpringBootApplication
@ComponentScan("de.shinythings")
class ProductCompositeServiceApplication(
        @Value("\${api.common.version}") private val apiVersion: String,
        @Value("\${api.common.title}") private val apiTitle: String,
        @Value("\${api.common.description}") private val apiDescription: String,
        @Value("\${api.common.termsOfServiceUrl}") private val apiTermsOfServiceUrl: String,
        @Value("\${api.common.license}") private val apiLicense: String,
        @Value("\${api.common.licenseUrl}") private val apiLicenseUrl: String,
        @Value("\${api.common.contact.name}") private val apiContactName: String,
        @Value("\${api.common.contact.url}") private val apiContactUrl: String,
        @Value("\${api.common.contact.email}") private val apiContactEmail: String
) {
    @Bean
    fun restTemplate() = RestTemplate()

    @Bean
    fun apiDocumentation(): Docket = Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage("de.shinythings.microservices.composite.product"))
            .paths(PathSelectors.any())
            .build()
            .globalResponseMessage(RequestMethod.GET, emptyList())
            .apiInfo(
                    ApiInfo(
                            apiTitle,
                            apiDescription,
                            apiVersion,
                            apiTermsOfServiceUrl,
                            Contact(apiContactName, apiContactUrl, apiContactEmail),
                            apiLicense,
                            apiLicenseUrl,
                            emptyList()
                    )
            )
}

fun main(args: Array<String>) {
    runApplication<ProductCompositeServiceApplication>(*args)
}
