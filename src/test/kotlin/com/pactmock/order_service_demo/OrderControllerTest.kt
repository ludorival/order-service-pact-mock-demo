package com.pactmock.order_service_demo

import com.pactmock.order_service_demo.models.Item
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals

@ExtendWith(InventoryServicePact::class)
class OrderControllerTest {

    private val restTemplate = mockk<RestTemplate>()
    private val inventoryServiceClient = InventoryServiceClient(restTemplate, SERVICE_URL)


    private val paymentService = mockk<PaymentService>()
    private val orderController = OrderController(inventoryServiceClient, paymentService)


    @Test
    fun `getItems returns list of items successfully`() {
        val mockItems = listOf(
            Item(1L, "Item 1", "Description", 10),
            Item(2L, "Item 2", "Description",20)
        )

        restTemplate.givenItemsAreAvailable(mockItems)

        val response = orderController.getItems()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(mockItems, response.body)
    }

    @Test
    fun `purchaseItem completes successfully when booking and payment succeed`() {
        val request = PurchaseRequest(1L, 2, 20.0)

        restTemplate.givenItemBookingSucceeds(request.itemId, request.quantity)

        every {
            paymentService.processPayment(1L, 2, 20.0)
        } returns PaymentResult(true, "Payment processed")

        val response = orderController.purchaseItem(request)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(true, response.body?.success)
        assertEquals("Purchase completed successfully", response.body?.message)
    }

    @Test
    fun `purchaseItem fails when booking fails`() {
        val request = PurchaseRequest(1L, 2, 20.0)
        
        restTemplate.givenItemBookingFails(request.itemId, request.quantity)

        val response = orderController.purchaseItem(request)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(false, response.body?.success)
        assertEquals("Out of stock", response.body?.message)
    }

    @Test
    fun `purchaseItem fails and releases inventory when payment fails`() {
        val request = PurchaseRequest(1L, 2, 20.0)

        restTemplate.givenItemBookingSucceeds(1L, 2)

        every {
            paymentService.processPayment(1L, 2, 20.0)
        } returns PaymentResult(false, "Insufficient funds")

        restTemplate.givenItemReleaseSucceeds(1L, 2)

        val response = orderController.purchaseItem(request)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(false, response.body?.success)
        assertEquals("Payment failed: Insufficient funds", response.body?.message)
    }

    @Test
    fun `purchaseItem handles exceptions gracefully`() {
        val request = PurchaseRequest(1L, 2, 20.0)

        restTemplate.givenItemBookingThrowsException(1L, 2)

        val response = orderController.purchaseItem(request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals(false, response.body?.success)
        assertEquals("Error processing purchase: 400 Service unavailable", response.body?.message)
    }

} 