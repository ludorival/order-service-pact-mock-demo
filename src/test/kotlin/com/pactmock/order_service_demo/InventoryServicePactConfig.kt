package com.pactmock.order_service_demo

import io.github.ludorival.pactjvm.mock.PactConfiguration
import io.github.ludorival.pactjvm.mock.spring.SpringRestTemplateMockAdapter

object InventoryServicePactConfig : PactConfiguration(
    SpringRestTemplateMockAdapter("order-service")
)

