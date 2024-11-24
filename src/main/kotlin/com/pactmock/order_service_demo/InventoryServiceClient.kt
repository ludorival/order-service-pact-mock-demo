package com.pactmock.order_service_demo

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class InventoryServiceClient(
    private val restTemplate: RestTemplate,
    @Value("\${inventory.service.url}") private val serviceUrl: String
) {
    fun getProductStock(productId: Long): StockResponse {
        val response = restTemplate.getForEntity(
            "$serviceUrl/inventory/product/$productId",
            StockResponse::class.java
        )
        return response.body ?: throw RuntimeException("Error: empty response")
    }
}
data class StockResponse(val productId: Long, val quantity: Int)
