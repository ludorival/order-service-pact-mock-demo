package com.pactmock.order_service_demo

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals

class OrderControllerTest {

    private val restTemplate = mockk<RestTemplate>()
    private val serviceUrl = "http://localhost:8081"
    private val inventoryServiceClient = InventoryServiceClient(restTemplate, serviceUrl)
    private val orderController = OrderController(inventoryServiceClient)

    @Test
    fun `checkStock should return product stock information`() {
        // Arrange
        val productId = 1L
        val expectedQuantity = 10
        val stockResponse = StockResponse(productId, expectedQuantity)
        
        every { 
            restTemplate.getForEntity(
                "http://localhost:8081/inventory/product/$productId",
                StockResponse::class.java
            )
        } returns ResponseEntity.ok(stockResponse)

        // Act
        val result = orderController.checkStock(productId)

        // Assert
        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(productId, result.body?.get("productId"))
        assertEquals(expectedQuantity, result.body?.get("stockAvailable"))
    }
} 