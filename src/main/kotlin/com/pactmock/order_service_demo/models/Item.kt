package com.pactmock.order_service_demo.models

data class Item(
    val id: Long,
    val name: String,
    val description: String,
    val stockCount: Int
)