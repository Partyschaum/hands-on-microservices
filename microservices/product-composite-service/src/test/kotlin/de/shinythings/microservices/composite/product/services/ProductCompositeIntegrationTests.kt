//package de.shinythings.microservices.composite.product.services
//
//import de.shinythings.api.core.product.Product
//import de.shinythings.api.core.recommendation.Recommendation
//import de.shinythings.api.core.review.Review
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.TestInstance
//import org.mockito.ArgumentMatchers.any
//import org.mockito.ArgumentMatchers.eq
//import org.mockito.Mockito.*
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.boot.test.mock.mockito.MockBean
//import org.springframework.core.ParameterizedTypeReference
//import org.springframework.http.HttpMethod
//import org.springframework.web.client.RestTemplate
//import org.springframework.web.reactive.function.client.WebClient
//import java.net.URI
//
//@SpringBootTest
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//class ProductCompositeIntegrationTests(
//        @Value("\${app.product-service.host}") private val productServiceHost: String,
//        @Value("\${app.product-service.port}") private val productServicePort: String,
//
//        @Value("\${app.recommendation-service.host}") private val recommendationServiceHost: String,
//        @Value("\${app.recommendation-service.port}") private val recommendationServicePort: String,
//
//        @Value("\${app.review-service.host}") private val reviewServiceHost: String,
//        @Value("\${app.review-service.port}") private val reviewServicePort: String
//) {
//
//    @MockBean
//    private lateinit var restTemplate: RestTemplate
//
//    @MockBean
//    private lateinit var webClientBuilder: WebClient.Builder
//
//    @Autowired
//    private lateinit var integration: ProductCompositeIntegration
//
//    private lateinit var webClient: WebClient
//
//    private val productService: String
//        get() = "$productServiceHost:$productServicePort"
//
//    private val recommendationService: String
//        get() = "$recommendationServiceHost:$recommendationServicePort"
//
//    private val reviewService: String
//        get() = "$reviewServiceHost:$reviewServicePort"
//
//    @BeforeEach
//    fun setUp() {
//        webClient = mock(WebClient::class.java)
//        `when`(webClient.get().uri(any(URI::class.java)).retrieve())
//        `when`(webClientBuilder.build())
//                .thenReturn(webClient)
//    }
//
//    @Test
//    fun getProduct() {
//        integration.getProduct(SOME_PRODUCT_ID)
//
//        val uri = URI("http://$productService/product/$SOME_PRODUCT_ID")
//
//        verify(webClient).get()
//    }
//
//    @Test
//    fun createProduct() {
//        val product = Product(SOME_PRODUCT_ID, "name", 1, null)
//
//        `when`(restTemplate.postForObject(any(URI::class.java), any(), eq(Product::class.java)))
//                .thenReturn(product)
//
//        integration.createProduct(product)
//
//        val uri = URI("http://$productService/product")
//
//        verify(restTemplate).postForObject(uri, product, Product::class.java)
//    }
//
//    @Test
//    fun deleteProduct() {
//        integration.deleteProduct(SOME_PRODUCT_ID)
//
//        val uri = URI("http://$productService/product/$SOME_PRODUCT_ID")
//
//        verify(restTemplate).delete(uri)
//    }
//
//    @Test
//    fun getRecommendation() {
//        integration.getRecommendations(SOME_PRODUCT_ID)
//
//        val uri = URI("http://$recommendationService/recommendation?productId=$SOME_PRODUCT_ID")
//
//        verify(restTemplate).exchange(
//                uri,
//                HttpMethod.GET,
//                null,
//                object : ParameterizedTypeReference<List<Recommendation>>() {}
//        )
//    }
//
//    @Test
//    fun createRecommendation() {
//        val recommendation = Recommendation(SOME_PRODUCT_ID, 1, "author", 1, "content", null)
//
//        `when`(restTemplate.postForObject(any(URI::class.java), any(), eq(Recommendation::class.java)))
//                .thenReturn(recommendation)
//
//        integration.createRecommendation(recommendation)
//
//        val uri = URI("http://$recommendationService/recommendation")
//
//        verify(restTemplate).postForObject(uri, recommendation, Recommendation::class.java)
//    }
//
//    @Test
//    fun deleteRecommendations() {
//        integration.deleteRecommendations(SOME_PRODUCT_ID)
//
//        val uri = URI("http://$recommendationService/recommendation?productId=$SOME_PRODUCT_ID")
//
//        verify(restTemplate).delete(uri)
//    }
//
//    @Test
//    fun getReviews() {
//        integration.getReviews(SOME_PRODUCT_ID)
//
//        val uri = URI("http://$reviewService/review?productId=$SOME_PRODUCT_ID")
//
//        verify(restTemplate).exchange(
//                uri,
//                HttpMethod.GET,
//                null,
//                object : ParameterizedTypeReference<List<Review>>() {}
//        )
//    }
//
//    @Test
//    fun createReview() {
//        val review = Review(SOME_PRODUCT_ID, 1, "author", "subject", "content", null)
//
//        `when`(restTemplate.postForObject(any(URI::class.java), any(), eq(Review::class.java)))
//                .thenReturn(review)
//
//        integration.createReview(review)
//
//        val uri = URI("http://$reviewService/review")
//
//        verify(restTemplate).postForObject(uri, review, Review::class.java)
//    }
//
//    @Test
//    fun deleteReviews() {
//        integration.deleteReviews(SOME_PRODUCT_ID)
//
//        val uri = URI("http://$reviewService/review?productId=$SOME_PRODUCT_ID")
//
//        verify(restTemplate).delete(uri)
//    }
//
//    companion object {
//        private const val SOME_PRODUCT_ID = 1
//    }
//}
