package com.pactmock.order_service_demo

import au.com.dius.pact.provider.junit5.HttpTestTarget
import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider
import au.com.dius.pact.provider.junitsupport.Provider
import au.com.dius.pact.provider.junitsupport.State
import au.com.dius.pact.provider.junitsupport.loader.PactBroker
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerAuth
import com.pactmock.order_service_demo.models.Item
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import io.mockk.every
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.web.client.RestTemplate
import java.net.URI

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Provider("order-service")
@PactBroker(
    url = "\${pactbroker.url:}",
    authentication = PactBrokerAuth(token = "\${pactbroker.auth.token:}"),
)
@Import(PactVerificationTestConfig::class)
class PactProviderVerificationTest {

    @Autowired
    private lateinit var restTemplate: RestTemplate

    @Autowired
    private lateinit var paymentService: PaymentService

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUp(context: PactVerificationContext) {
        context.target = HttpTestTarget.fromUrl(URI.create("http://localhost:$port").toURL())
        
        // Configure publishing results (only publish in CI/CD, not locally)
        System.setProperty("pact.verifier.publishResults", System.getenv("CI") ?: "false")
        System.setProperty("pact.provider.version", System.getenv("GITHUB_SHA") ?: "unknown")
        System.setProperty("pact.provider.branch", System.getenv("GITHUB_BRANCH") ?: "unknown")
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider::class)
    fun pactVerificationTestTemplate(context: PactVerificationContext) {
        context.verifyInteraction()
    }

    // Provider state handlers

    @State("items are available")
    fun `items are available`(params: Map<String, Any>) {
        @Suppress("UNCHECKED_CAST")
        val itemIds = params["itemIds"] as? List<Int> ?: emptyList()
        
        val items = itemIds.map { id ->
            when (id) {
                1 -> Item(1L, "Test Item 1", "This is a test item", 5)
                2 -> Item(2L, "Test Item 2", "This is another test item", 3)
                else -> Item(id.toLong(), "Item $id", "Description for item $id", 10)
            }
        }
        
        restTemplate.givenItemsAreAvailable(items)
    }

    @State("purchase can be completed")
    fun `purchase can be completed`(params: Map<String, Any>) {
        val itemId = (params["itemId"] as? Number)?.toLong() ?: 1L
        val quantity = (params["quantity"] as? Number)?.toInt() ?: 3
        
        // Mock inventory service: booking succeeds
        restTemplate.givenItemBookingSucceeds(itemId, quantity)
        
        // Mock payment service: payment succeeds
        every {
            paymentService.processPayment(itemId, quantity, any())
        } returns PaymentResult(true, "Payment processed successfully")
    }

    @State("purchase fails")
    fun `purchase fails`(params: Map<String, Any>) {
        val itemId = (params["itemId"] as? Number)?.toLong() ?: 1L
        
        // Mock inventory service: booking succeeds initially
        restTemplate.givenItemBookingSucceeds(itemId, 1)
        
        // Mock payment service: payment throws exception, causing a server error (500)
        // The controller should catch exceptions and return 500
        every {
            paymentService.processPayment(itemId, any(), any())
        } throws RuntimeException("Payment processing error")
    }

    @State("item is out of stock")
    fun `item is out of stock`(params: Map<String, Any>) {
        val itemId = (params["itemId"] as? Number)?.toLong() ?: 1L
        
        val items = listOf(
            Item(itemId, "Out of Stock Item", "This item is out of stock", 0)
        )
        
        restTemplate.givenItemsAreAvailable(items)
    }
}

