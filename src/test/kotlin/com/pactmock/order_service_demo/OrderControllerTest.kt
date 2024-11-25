package com.pactmock.order_service_demo

import com.pactmock.order_service_demo.models.Item
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


    private val paymentService = mockk<PaymentService>()
    private val orderController = OrderController(inventoryServiceClient, paymentService)


    @Test
    fun `getItems returns list of items successfully`() {
        val mockItems = listOf(
            Item(1L, "Item 1", "Description", 10),
            Item(2L, "Item 2", "Description",20)
        )

        every {
            restTemplate.getForEntity("$serviceUrl/items", Array<Item>::class.java)
        } returns ResponseEntity.ok(mockItems.toTypedArray())

        val response = orderController.getItems()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(mockItems, response.body)
    }

    @Test
    fun `purchaseItem completes successfully when booking and payment succeed`() {
        val request = PurchaseRequest(1L, 2, 20.0)

        every {
            inventoryServiceClient.bookItem(1L, 2)
        } returns BookingResponse(true, "Booked successfully")

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
        
        every { 
            inventoryServiceClient.bookItem(1L, 2)
        } returns BookingResponse(false, "Out of stock")

        val response = orderController.purchaseItem(request)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(false, response.body?.success)
        assertEquals("Out of stock", response.body?.message)
    }

    @Test
    fun `purchaseItem fails and releases inventory when payment fails`() {
        val request = PurchaseRequest(1L, 2, 20.0)

        every {
            inventoryServiceClient.bookItem(1L, 2)
        } returns BookingResponse(true, "Booked successfully")

        every {
            paymentService.processPayment(1L, 2, 20.0)
        } returns PaymentResult(false, "Insufficient funds")

        every {
            inventoryServiceClient.releaseItem(1L, 2)
        } returns ReleaseResponse(true, "Released successfully")

        val response = orderController.purchaseItem(request)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(false, response.body?.success)
        assertEquals("Payment failed: Insufficient funds", response.body?.message)
    }

    @Test
    fun `purchaseItem handles exceptions gracefully`() {
        val request = PurchaseRequest(1L, 2, 20.0)

        every {
            inventoryServiceClient.bookItem(1L, 2)
        } throws RuntimeException("Service unavailable")

        val response = orderController.purchaseItem(request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals(false, response.body?.success)
        assertEquals("Error processing purchase: Service unavailable", response.body?.message)
    }

} 