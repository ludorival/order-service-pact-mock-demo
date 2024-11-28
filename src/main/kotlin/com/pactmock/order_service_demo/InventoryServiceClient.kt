package com.pactmock.order_service_demo

import com.pactmock.order_service_demo.models.Item
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class InventoryServiceClient(
    private val restTemplate: RestTemplate,
    @Value("\${inventory.service.url}") private val serviceUrl: String
) {
    fun getItems(): List<Item> {
        val response = restTemplate.getForEntity(
            "$serviceUrl/items",
            Array<Item>::class.java
        )
        return response.body?.toList() ?: emptyList()
    }

    fun bookItem(itemId: Long, quantity: Int): BookingResponse {
        val request = BookingRequest(itemId, quantity)
        val response = restTemplate.postForEntity(
            "$serviceUrl/book",
            request,
            BookingResponse::class.java
        )
        return response.body ?: throw RuntimeException("Error: empty response")
    }

    fun releaseItem(itemId: Long, quantity: Int): ReleaseResponse {
        val request = ReleaseRequest(itemId, quantity)
        val response = restTemplate.postForEntity(
            "$serviceUrl/release",
            request,
            ReleaseResponse::class.java
        )
        return response.body ?: throw RuntimeException("Error: empty response")
    }

}

data class BookingRequest(val itemId: Long, val quantity: Int)
data class BookingResponse(val success: Boolean, val message: String)
data class ReleaseRequest(val itemId: Long, val quantity: Int)
data class ReleaseResponse(val success: Boolean, val message: String)
