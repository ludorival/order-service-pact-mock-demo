package com.pactmock.order_service_demo

import TestConfig
import au.com.dius.pact.provider.junit5.HttpTestTarget
import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junitsupport.Provider
import au.com.dius.pact.provider.junitsupport.State
import au.com.dius.pact.provider.junitsupport.loader.PactBroker
import au.com.dius.pact.provider.junitsupport.loader.PactBrokerAuth
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.ResponseEntity
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.client.RestTemplate
import java.net.URI

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Provider("order-service")
@PactBroker(
    url = "\${pactbroker.url}",
    authentication = PactBrokerAuth(token = "\${pactbroker.token}")
)
@Import(TestConfig::class)
class PactProviderVerificationTest {

    @Autowired
    private lateinit var restTemplate: RestTemplate

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setUp(context: PactVerificationContext) {
        context.target = HttpTestTarget.fromUrl(URI.create("http://localhost:$port").toURL())

        // Set up the mock response for RestTemplate
        every {
            restTemplate.getForEntity(
                match<String> { it.endsWith("/inventory/product/1") },
                StockResponse::class.java
            )
        } returns ResponseEntity.ok(StockResponse(1L, 10))
    }

    @TestTemplate
    @ExtendWith(PactVerificationSpringProvider::class)
    fun pactVerificationTestTemplate(context: PactVerificationContext) {
        context.verifyInteraction()
    }

    @State("product 1 exists")
    fun `product 1 exists`() {
        // The mock is already set up in setUp()
    }
}
