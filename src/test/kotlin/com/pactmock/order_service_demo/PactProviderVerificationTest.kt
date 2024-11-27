package com.pactmock.order_service_demo

import TestConfig
import au.com.dius.pact.provider.junit5.HttpTestTarget
import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junitsupport.Provider
import au.com.dius.pact.provider.junitsupport.State
import au.com.dius.pact.provider.junitsupport.loader.PactBroker
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerAuth
import au.com.dius.pact.provider.junitsupport.loader.PactFolder

import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider
import com.pactmock.order_service_demo.models.Item
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.client.RestTemplate
import java.net.URI

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Provider("order-service")
@PactBroker(
    url = "\${pactbroker.url}",
    authentication = PactBrokerAuth(token = "\${pactbroker.auth.token}"),
)
@Import(TestConfig::class)
@ExtendWith(InventoryServicePact::class)
class PactProviderVerificationTest {

    @Autowired
    private lateinit var restTemplate: RestTemplate

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUp(context: PactVerificationContext) {
        context.target = HttpTestTarget.fromUrl(URI.create("http://localhost:$port").toURL())
        System.setProperty("pact.verifier.publishResults", System.getenv("CI") ?: "false")
        System.setProperty("pact.provider.version", System.getenv("GIT_COMMIT") ?: "unknown")
    }

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider::class)
    fun pactVerificationTestTemplate(context: PactVerificationContext) {
        context.verifyInteraction()
    }

    @State("There are 2 items")
    fun `There are 2 items`() {
        val mockItems = listOf(
            Item(1L, "Test Item 1", "This is a test item", 5),
            Item(2L, "Test Item 2", "This is another test item", 3)
        )
        restTemplate.givenItemsAreAvailable(mockItems)
    }

    @State("There is an item with stock")
    fun `There is an item with stock`() {
        val mockItem = Item(1L, "Test Item 1", "This is a test item", 5)
        restTemplate.givenItemsAreAvailable(listOf(mockItem))
        restTemplate.givenItemBookingSucceeds(1L, 3)
    }

    @State("There is an error")
    fun `There is an error`() {
        with(restTemplate){
            givenItemBookingFails(1L, 1)
            givenItemReleaseSucceeds(1L, 1)
        }

    }

    @State("There is an item with 0 stock")
    fun `There is an item with 0 stock`() {
        val mockItem = Item(1L, "Out of Stock Item", "This item is out of stock", 0)
        restTemplate.givenItemsAreAvailable(listOf(mockItem))
    }
}
