package com.pactmock.order_service_demo

import io.mockk.mockk
import io.mockk.spyk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.web.client.RestTemplate

@TestConfiguration
class PactVerificationTestConfig {

    @Bean
    @Primary
    fun mockedRestTemplate(): RestTemplate {
        return mockk<RestTemplate>(relaxed = true)
    }

    @Bean
    @Primary
    fun spyPaymentService(): PaymentService {
        return spyk(PaymentService(), recordPrivateCalls = true)
    }
}

