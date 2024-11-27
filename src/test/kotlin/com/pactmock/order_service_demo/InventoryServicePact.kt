package com.pactmock.order_service_demo

import com.pactmock.order_service_demo.models.Item
import io.mockk.every
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import io.github.ludorival.pactjvm.mockk.pactOptions
import io.github.ludorival.pactjvm.mockk.spring.SpringRestTemplateMockkAdapter
import io.github.ludorival.pactjvm.mockk.willRespond
import io.github.ludorival.pactjvm.mockk.willRespondWith
import io.github.ludorival.pactjvm.mockk.writePacts
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.web.client.HttpClientErrorException

const val SERVICE_URL = "http://localhost:4000/v1"

object InventoryServicePact : AfterAllCallback {
    init {
        pactOptions {
            consumer = "order-service"
            determineProviderFromInteraction = { "inventory-service" }
            // allow to intercept Spring RestTemplate mocks
            addAdapter(SpringRestTemplateMockkAdapter())
        }
    }

    override fun afterAll(context: ExtensionContext?) = writePacts()

 }

fun RestTemplate.givenItemsAreAvailable(items: List<Item>) {
    every {
        getForEntity("$SERVICE_URL/items", Array<Item>::class.java)
    } willRespondWith {
        options {
            description = "Items are available"
            providerStates = listOf("Items are available")
        }
        ResponseEntity.ok(items.toTypedArray())}
}

fun RestTemplate.givenItemBookingSucceeds(itemId: Long, quantity: Int) {
    every {
        postForEntity(
            "$SERVICE_URL/book",
            match<BookingRequest> { 
                it.itemId == itemId && it.quantity == quantity 
            },
            BookingResponse::class.java
        )
    } willRespondWith{
        options {
            description = "item is successfully booked"
            providerStates = listOf("Items are available")
        }
        ResponseEntity.ok(BookingResponse(true, "Booked successfully"))}
}

fun RestTemplate.givenItemBookingFails(itemId: Long, quantity: Int, message: String = "Out of stock") {
    every {
        postForEntity(
            "$SERVICE_URL/book",
            match<BookingRequest> { 
                it.itemId == itemId && it.quantity == quantity 
            },
            BookingResponse::class.java
        )
    } willRespondWith{
        options {
            description = "item is not booked"
            providerStates = listOf("There is an item with 0 stock")
        }
        ResponseEntity.ok(BookingResponse(false, message))}
}

fun RestTemplate.givenItemBookingThrowsException(itemId: Long, quantity: Int, message: String = "Service unavailable") {
    every {
        postForEntity(
            "$SERVICE_URL/book",
            match<BookingRequest> { 
                it.itemId == itemId && it.quantity == quantity 
            },
            BookingResponse::class.java
        )
    } willRespondWith {
        options {
            providerStates = listOf("The request should return a 400 Bad request")
        }
        throw HttpClientErrorException(
            HttpStatus.BAD_REQUEST,
            message
        )
    }
}

fun RestTemplate.givenItemReleaseSucceeds(itemId: Long, quantity: Int) {
    every {
        postForEntity(
            "$SERVICE_URL/release",
            match<ReleaseRequest> { 
                it.itemId == itemId && it.quantity == quantity 
            },
            ReleaseResponse::class.java
        )
    } willRespondWith {
        options {
            description = "item is successfully released"
            providerStates = listOf("Items are available")
        }
        ResponseEntity.ok(ReleaseResponse(true, "Released successfully")) }
}