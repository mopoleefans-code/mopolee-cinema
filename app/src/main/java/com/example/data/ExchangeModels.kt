package com.example.data

data class BankAccountConfig(
    val bankName: String,
    val accountNumber: String,
    val accountName: String,
    val whatsappNumber: String = "+2347088193492",
    val whatsappNumberSecondary: String = "+2348151745414",
    val usdtTrc20Address: String = "TNPx9Vp8qA2K8bNmC1LkQo4U8Z1VpM7J1W"
)

data class GiftCardRate(
    val brandId: String,
    val brandName: String,
    val currency: String,
    val rateNgn: Double,
    val rateUsd: Double,
    val iconEmoji: String,
    val details: String
)

data class PaymentPackage(
    val id: String,
    val title: String,
    val subtitle: String,
    val priceNgn: Long,
    val amountKobo: Long,
    val creditsBonus: Int,
    val features: List<String>,
    val isPopular: Boolean = false
)

data class SmsCreditPackage(
    val id: String,
    val title: String,
    val credits: Int,
    val amountKobo: Long,
    val priceLabel: String,
    val perSmsLabel: String,
    val isBestValue: Boolean = false
)

data class VirtualCountryNumber(
    val id: String,
    val countryName: String,
    val countryCode: String,
    val flagEmoji: String,
    val phoneNumber: String,
    val costCredits: Int = 0,
    val priceLabel: String = "",
    val durationLabel: String = "",
    val supportedServices: List<String> = emptyList(),
    val isActive: Boolean = false
)

data class ReceivedSmsOtp(
    val id: String,
    val title: String,
    val senderName: String,
    val recipientPhone: String,
    val otpCode: String,
    val messageBody: String,
    val timestampMillis: Long = System.currentTimeMillis()
)

data class PaystackConfig(
    val publicKey: String = "pk_live_mopolee_cinema_exchange_2026_secured",
    val businessName: String = "MOPOLEE ENTERTAINMENT & FX HUB",
    val businessEmail: String = "support@mopoleecinema.com",
    val defaultCurrency: String = "NGN",
    val isLiveMode: Boolean = true,
    val instantSettlementBank: String = "Access Bank PLC",
    val instantSettlementAccount: String = "0708819349"
)

data class TwilioConfig(
    val accountSid: String = "AC64f8e093a33d7919c1caa3acbae7ae05A",
    val authToken: String = "2fdaceec0dc2ac11a2225a662e37b757",
    val twilioPhoneNumber: String = "+2347088193492",
    val secondaryPhoneNumber: String = "+2348151745414"
)
