package com.pactmock.order_service_demo

import org.springframework.stereotype.Service

@Service
class PaymentService {
    fun processPayment(itemId: Long, quantity: Int, amount: Double): PaymentResult {
        // Simulate payment processing
        val success = (itemId + quantity).toInt() % 2 == 0 
        return if (success) {
            PaymentResult(true, "Payment processed successfully")
        } else {
            PaymentResult(false, "Payment failed")
        }
    }
}

data class PaymentResult(val success: Boolean, val message: String)