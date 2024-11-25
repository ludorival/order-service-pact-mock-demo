 package com.pactmock.order_service_demo

import com.pactmock.order_service_demo.models.Item
import io.mockk.every
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

    const val SERVICE_URL = "http://localhost:4000/v1"

    fun RestTemplate.givenItemsAreAvailable(items: List<Item>) {
        every {
            getForEntity("$SERVICE_URL/items", Array<Item>::class.java)
        } returns ResponseEntity.ok(items.toTypedArray())
    }

    fun RestTemplate.givenItemBookingSucceeds(itemId: Long, quantity: Int) {
        every {
            postForEntity(
                "$SERVICE_URL/book",
                any(),
                BookingResponse::class.java
            )
        } returns ResponseEntity.ok(BookingResponse(true, "Booked successfully"))
    }

    fun RestTemplate.givenItemBookingFails(itemId: Long, quantity: Int, message: String = "Out of stock") {
        every {
            postForEntity(
                "$SERVICE_URL/book",
                any(),
                BookingResponse::class.java
            )
        } returns ResponseEntity.ok(BookingResponse(false, message))
    }

    fun RestTemplate.givenItemBookingThrowsException(itemId: Long, quantity: Int, message: String = "Service unavailable") {
        every {
            postForEntity(
                "$SERVICE_URL/book",
                any(),
                BookingResponse::class.java
            )
        } throws RuntimeException(message)
    }

    fun RestTemplate.givenItemReleaseSucceeds(itemId: Long, quantity: Int) {
        every {
            postForEntity(
                "$SERVICE_URL/release",
                any(),
                ReleaseResponse::class.java
            )
        } returns ResponseEntity.ok(ReleaseResponse(true, "Released successfully"))
    }
