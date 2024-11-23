package com.pactmock.order_service_demo

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/orders")
class OrderController(val inventoryServiceClient: InventoryServiceClient) {

    @GetMapping("/check-stock")
    fun checkStock(@RequestParam productId: Long): ResponseEntity<Map<String, Any>> {
        val stockResponse = inventoryServiceClient.getProductStock(productId)
        return ResponseEntity.ok(
            mapOf(
                "productId" to productId,
                "stockAvailable" to stockResponse.quantity
            )
        )
    }
}