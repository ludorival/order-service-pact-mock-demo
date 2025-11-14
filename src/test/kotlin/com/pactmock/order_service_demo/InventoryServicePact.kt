package com.pactmock.order_service_demo

import com.pactmock.order_service_demo.models.Item
import io.github.ludorival.pactjvm.mock.mockk.uponReceiving
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

const val SERVICE_URL = "http://localhost:4000/inventory-service/v1"

fun RestTemplate.givenItemsAreAvailable(items: List<Item>) {
    uponReceiving {
        getForEntity("$SERVICE_URL/items", Array<Item>::class.java)
    } given {
        state("items are available")
    } returns ResponseEntity.ok(items.toTypedArray())
}

fun RestTemplate.givenItemBookingSucceeds(itemId: Long, quantity: Int) {
    uponReceiving {
        postForEntity(
            "$SERVICE_URL/book",
            match<BookingRequest> { 
                it.itemId == itemId && it.quantity == quantity 
            },
            BookingResponse::class.java
        )
    } given {
        state("item is available for booking", mapOf("itemId" to itemId, "quantity" to quantity))
    } returns ResponseEntity.ok(BookingResponse(true, "Booked successfully"))
}

fun RestTemplate.givenItemBookingFails(itemId: Long, quantity: Int, message: String = "Out of stock") {
    uponReceiving {
        postForEntity(
            "$SERVICE_URL/book",
            match<BookingRequest> { 
                it.itemId == itemId && it.quantity == quantity 
            },
            BookingResponse::class.java
        )
    } given {
        state("item is out of stock", mapOf("itemId" to itemId, "quantity" to quantity))
    } returns ResponseEntity.ok(BookingResponse(false, message))
}

fun RestTemplate.givenItemBookingThrowsException(itemId: Long, quantity: Int, message: String = "Service unavailable") {
    uponReceiving {
        postForEntity(
            "$SERVICE_URL/book",
            match<BookingRequest> { 
                it.itemId == itemId && it.quantity == quantity 
            },
            BookingResponse::class.java
        )
    } given {
        state("inventory service is unavailable")
    } throws RuntimeException(message)
}

fun RestTemplate.givenItemReleaseSucceeds(itemId: Long, quantity: Int) {
    uponReceiving {
        postForEntity(
            "$SERVICE_URL/release",
            match<ReleaseRequest> { 
                it.itemId == itemId && it.quantity == quantity 
            },
            ReleaseResponse::class.java
        )
    } given {
        state("item is booked and can be released", mapOf("itemId" to itemId, "quantity" to quantity))
    } returns ResponseEntity.ok(ReleaseResponse(true, "Released successfully"))
}
