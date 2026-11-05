package com.example.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class PaystackPaymentStatus {
    PENDING,
    SUCCESSFUL,
    FAILED
}

data class PaystackTransaction(
    val id: String = java.util.UUID.randomUUID().toString(),
    val reference: String,
    val amountKobo: Long,
    val amountNgn: Long = amountKobo / 100,
    val currency: String = "NGN",
    val customerEmail: String,
    val packageTitle: String,
    val status: PaystackPaymentStatus = PaystackPaymentStatus.PENDING,
    val statusMessage: String = "Payment pending verification",
    val channel: String = "card",
    val gatewayResponse: String = "",
    val timestampMillis: Long = System.currentTimeMillis(),
    val verifiedAtMillis: Long? = null,
    val isServerVerified: Boolean = false
) {
    val formattedNaira: String
        get() = "₦" + String.format(Locale.US, "%,d", amountNgn)

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            return sdf.format(Date(timestampMillis))
        }

    val isPending: Boolean get() = status == PaystackPaymentStatus.PENDING
    val isSuccessful: Boolean get() = status == PaystackPaymentStatus.SUCCESSFUL
    val isFailed: Boolean get() = status == PaystackPaymentStatus.FAILED
}
