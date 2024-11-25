package com.pactmock.order_service_demo.assumptions

import com.pactmock.order_service_demo.models.Item
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

class InventoryServiceAssumption {
    val restTemplate = mockk<RestTemplate>()
    private val serviceUrl = "http://localhost:8081"

    val inventoryServiceClient = InventoryServiceClient(restTemplate, serviceUrl)

    fun givenItemsAreAvailable(items: List<Item>) {
        every {
            restTemplate.getForEntity("$serviceUrl/items", Array<Item>::class.java)
        } returns ResponseEntity.ok(items.toTypedArray())
    }

    fun givenItemBookingSucceeds(itemId: Long, quantity: Int) {
        every {
            restTemplate.postForEntity(
                "$serviceUrl/book",
                any(),
                BookingResponse::class.java
            )
        } returns ResponseEntity.ok(BookingResponse(true, "Booked successfully"))
    }

    fun givenItemBookingFails(itemId: Long, quantity: Int, message: String = "Out of stock") {
        every {
            restTemplate.postForEntity(
                "$serviceUrl/book",
                any(),
                BookingResponse::class.java
            )
        } returns ResponseEntity.ok(BookingResponse(false, message))
    }

    fun givenItemBookingThrowsException(itemId: Long, quantity: Int, message: String) {
        every {
            restTemplate.postForEntity(
                "$serviceUrl/book",
                any(),
                BookingResponse::class.java
            )
        } throws RuntimeException(message)
    }

    fun givenItemReleaseSucceeds(itemId: Long, quantity: Int) {
        every {
            restTemplate.postForEntity(
                "$serviceUrl/release",
                any(),
                ReleaseResponse::class.java
            )
        } returns ResponseEntity.ok(ReleaseResponse(true, "Released successfully"))
    }
} 