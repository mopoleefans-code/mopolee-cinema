package com.example.data

import android.content.Context
import android.content.SharedPreferences

object TwilioManager {
    const val PREFS_NAME = "mopolee_twilio_prefs"
    const val KEY_ACCOUNT_SID = "twilio_account_sid"
    const val KEY_AUTH_TOKEN = "twilio_auth_token"
    const val KEY_PHONE_NUMBER = "twilio_phone_number"
    const val KEY_SECONDARY_PHONE = "twilio_secondary_phone"
    const val KEY_USER_CREDITS = "twilio_user_credits"

    val worldwideNumbers = listOf(
        VirtualCountryNumber(
            id = "num_nigeria_primary",
            countryName = "Nigeria (Your Primary Hotline)",
            countryCode = "+234",
            flagEmoji = "🇳🇬",
            phoneNumber = "+234 708 819 3492",
            costCredits = 0,
            priceLabel = "⭐ ACTIVE (07088193492)",
            durationLabel = "Primary Line",
            supportedServices = listOf("Twilio SMS Live", "WhatsApp AI", "Paystack Alerts", "Moniepoint", "OPay"),
            isActive = true
        ),
        VirtualCountryNumber(
            id = "num_nigeria_secondary",
            countryName = "Nigeria (Your Secondary Hotline)",
            countryCode = "+234",
            flagEmoji = "🇳🇬",
            phoneNumber = "+234 815 174 5414",
            costCredits = 0,
            priceLabel = "⭐ ACTIVE (08151745414)",
            durationLabel = "Secondary Line",
            supportedServices = listOf("WhatsApp AI Desk", "2FA OTP", "Gift Card Trading", "Telegram"),
            isActive = true
        ),
        VirtualCountryNumber(
            id = "num_usa",
            countryName = "United States",
            countryCode = "+1",
            flagEmoji = "🇺🇸",
            phoneNumber = "+1 (202) 555-0192",
            costCredits = 250000,
            priceLabel = "₦2,500",
            durationLabel = "$2.00 USD",
            supportedServices = listOf("WhatsApp", "Telegram", "Google", "PayPal", "TikTok"),
            isActive = false
        ),
        VirtualCountryNumber(
            id = "num_uk",
            countryName = "United Kingdom",
            countryCode = "+44",
            flagEmoji = "🇬🇧",
            phoneNumber = "+44 7911 123456",
            costCredits = 300000,
            priceLabel = "₦3,000",
            durationLabel = "$2.50 USD",
            supportedServices = listOf("WhatsApp", "Revolut", "Telegram", "Instagram", "Banking"),
            isActive = false
        ),
        VirtualCountryNumber(
            id = "num_canada",
            countryName = "Canada",
            countryCode = "+1",
            flagEmoji = "🇨🇦",
            phoneNumber = "+1 (416) 555-0143",
            costCredits = 250000,
            priceLabel = "₦2,500",
            durationLabel = "$2.00 USD",
            supportedServices = listOf("WhatsApp", "Amazon", "Discord", "Apple ID"),
            isActive = false
        ),
        VirtualCountryNumber(
            id = "num_germany",
            countryName = "Germany",
            countryCode = "+49",
            flagEmoji = "🇩🇪",
            phoneNumber = "+49 151 23456789",
            costCredits = 350000,
            priceLabel = "₦3,500",
            durationLabel = "$3.00 USD",
            supportedServices = listOf("WhatsApp", "Telegram", "eBay", "Crypto 2FA"),
            isActive = false
        ),
        VirtualCountryNumber(
            id = "num_france",
            countryName = "France",
            countryCode = "+33",
            flagEmoji = "🇫🇷",
            phoneNumber = "+33 6 12 34 56 78",
            costCredits = 350000,
            priceLabel = "₦3,500",
            durationLabel = "$3.00 USD",
            supportedServices = listOf("WhatsApp", "Vinted", "Google", "Facebook"),
            isActive = false
        )
    )

    val smsPacks = listOf(
        SmsCreditPackage("sms_50", "Starter Pack (50 SMS)", 50, 200000, "₦2,000 ($1.50)", "₦40 / SMS"),
        SmsCreditPackage("sms_200", "Pro Sender (200 SMS)", 200, 650000, "₦6,500 ($5.00)", "₦32 / SMS", isBestValue = true),
        SmsCreditPackage("sms_500", "Business Blast (500 SMS)", 500, 1500000, "₦15,000 ($11.00)", "₦30 / SMS")
    )

    val sampleLiveOtps = listOf(
        ReceivedSmsOtp("otp_0", "WhatsApp Business (Hotline 1)", "WhatsApp", "+234 708 819 3492", "918-204", "Your MOPOLEE WhatsApp business verification code is: 918-204. Registered for 07088193492."),
        ReceivedSmsOtp("otp_1", "WhatsApp Business (Hotline 2)", "WhatsApp", "+234 815 174 5414", "341-890", "Your MOPOLEE secondary dispatch hotline verification code is: 341-890. Registered for 08151745414."),
        ReceivedSmsOtp("otp_2", "Twilio Live SMS", "Twilio", "+234 708 819 3492", "58201", "Twilio API Auth verified: SID AC64f8e093a33d7919c1caa3acbae7ae05A active on MOPOLEE."),
        ReceivedSmsOtp("otp_3", "Google & Banking 2FA", "Google", "+1 (416) 555-0143", "G-502194", "G-502194 is your Google verification code for MOPOLEE CINEMA account.")
    )

    fun getTwilioConfig(context: Context): TwilioConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedSid = prefs.getString(KEY_ACCOUNT_SID, null)
        val effectiveSid = if (savedSid.isNullOrBlank() || savedSid.startsWith("AC_demo")) {
            "AC64f8e093a33d7919c1caa3acbae7ae05A"
        } else {
            savedSid
        }
        val savedToken = prefs.getString(KEY_AUTH_TOKEN, null)
        val effectiveToken = if (savedToken.isNullOrBlank() || savedToken.startsWith("auth_demo")) {
            "2fdaceec0dc2ac11a2225a662e37b757"
        } else {
            savedToken
        }
        val savedPhone = prefs.getString(KEY_PHONE_NUMBER, null)
        val effectivePhone = if (savedPhone.isNullOrBlank() || savedPhone == "+12025550192") {
            "+2347088193492"
        } else {
            savedPhone
        }
        val savedSecondary = prefs.getString(KEY_SECONDARY_PHONE, null)
        val effectiveSecondary = if (savedSecondary.isNullOrBlank()) {
            "+2348151745414"
        } else {
            savedSecondary
        }
        return TwilioConfig(effectiveSid, effectiveToken, effectivePhone, effectiveSecondary)
    }

    fun saveTwilioConfig(context: Context, config: TwilioConfig) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_ACCOUNT_SID, config.accountSid.trim())
            .putString(KEY_AUTH_TOKEN, config.authToken.trim())
            .putString(KEY_PHONE_NUMBER, config.twilioPhoneNumber.trim())
            .putString(KEY_SECONDARY_PHONE, config.secondaryPhoneNumber.trim())
            .apply()
    }

    fun getSmsCredits(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_USER_CREDITS, 25)
    }

    fun addSmsCredits(context: Context, creditsToAdd: Int): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getInt(KEY_USER_CREDITS, 25)
        val updated = current + creditsToAdd
        prefs.edit().putInt(KEY_USER_CREDITS, updated).apply()
        return updated
    }

    fun deductSmsCredit(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getInt(KEY_USER_CREDITS, 25)
        if (current <= 0) return false
        prefs.edit().putInt(KEY_USER_CREDITS, current - 1).apply()
        return true
    }
}
