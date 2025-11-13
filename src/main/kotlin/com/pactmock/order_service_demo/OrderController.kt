package com.pactmock.order_service_demo

import com.pactmock.order_service_demo.models.Item
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1")
class OrderController(
    private val inventoryServiceClient: InventoryServiceClient,
    private val paymentService: PaymentService
) {
    private val logger = LoggerFactory.getLogger(OrderController::class.java)

    @GetMapping("/items")
    fun getItems(): ResponseEntity<List<Item>> {
        val items = inventoryServiceClient.getItems()
        return ResponseEntity.ok(items)
    }

    @PostMapping("/purchase")
    fun purchaseItem(@RequestBody request: PurchaseRequest): ResponseEntity<PurchaseResponse> {
        try {
            // Step 1: Book the item
            val bookingResponse = inventoryServiceClient.bookItem(request.itemId, request.quantity)
            if (!bookingResponse.success) {
                return ResponseEntity.badRequest()
                    .body(PurchaseResponse(false, bookingResponse.message))
            }

            // Step 2: Process payment
            val paymentResult = paymentService.processPayment(
                request.itemId,
                request.quantity,
                request.amount
            )

            return if (paymentResult.success) {
                ResponseEntity.ok(PurchaseResponse(true, "Purchase completed successfully"))
            } else {
                // Release the inventory since payment failed
                val releaseResponse = inventoryServiceClient.releaseItem(request.itemId, request.quantity)
                if (!releaseResponse.success) {
                    // Log the error but still return payment failure to the client
                    logger.error("Failed to release inventory: ${releaseResponse.message}")
                }

                ResponseEntity.badRequest()
                    .body(PurchaseResponse(false, "Payment failed: ${paymentResult.message}"))
            }
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(PurchaseResponse(false, "Error processing purchase: ${e.message}"))
        }
    }

}

data class PurchaseRequest(
    val itemId: Long,
    val quantity: Int,
    val amount: Double
)

data class PurchaseResponse(
    val success: Boolean,
    val message: String
)