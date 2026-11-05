package com.example.data

import android.content.Context
import android.content.SharedPreferences

object ExchangeManager {
    const val PREFS_NAME = "mopolee_exchange_prefs"
    const val MARKETPLACE_NAME = "MOPOLEE AI TRADING AND GOOD SERVICE"
    const val KEY_BANK_NAME = "bank_name"
    const val KEY_ACCOUNT_NUMBER = "account_number"
    const val KEY_ACCOUNT_NAME = "account_name"
    const val KEY_WHATSAPP_NUMBER = "whatsapp_number"
    const val KEY_WHATSAPP_SECONDARY = "whatsapp_number_secondary"
    const val KEY_USDT_ADDRESS = "usdt_trc20_address"

    val defaultRates = listOf(
        GiftCardRate("apple", "Apple / iTunes", "USD", 1480.0, 0.90, "🍏", "Fast 5-min verification • USD/USDT/NGN"),
        GiftCardRate("steam", "Steam Wallet", "USD", 1520.0, 0.92, "🎮", "Instant payout in USD or USDT"),
        GiftCardRate("google", "Google Play", "USD", 1450.0, 0.88, "▶️", "High demand today • Good service guarantee"),
        GiftCardRate("amazon", "Amazon Gift Card", "USD", 1420.0, 0.86, "📦", "Cash receipt & e-codes supported"),
        GiftCardRate("razer", "Razer Gold", "USD", 1500.0, 0.91, "⚡", "Instant automated payout in USDT"),
        GiftCardRate("sephora", "Sephora", "USD", 1400.0, 0.85, "💄", "Physical cards & receipts accepted")
    )

    fun getSavedBankConfig(context: Context): BankAccountConfig {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedWhatsapp = prefs.getString(KEY_WHATSAPP_NUMBER, null)
        val effectiveWhatsapp = if (savedWhatsapp.isNullOrBlank() || savedWhatsapp == "+2348123456789") {
            "+2347088193492"
        } else {
            savedWhatsapp
        }
        val savedSecondary = prefs.getString(KEY_WHATSAPP_SECONDARY, null)
        val effectiveSecondary = if (savedSecondary.isNullOrBlank()) {
            "+2348151745414"
        } else {
            savedSecondary
        }
        val bankName = prefs.getString(KEY_BANK_NAME, "Moniepoint MFB") ?: "Moniepoint MFB"
        val accountNumber = prefs.getString(KEY_ACCOUNT_NUMBER, "8123456789") ?: "8123456789"
        val accountName = prefs.getString(KEY_ACCOUNT_NAME, MARKETPLACE_NAME) ?: MARKETPLACE_NAME
        val usdtAddress = prefs.getString(KEY_USDT_ADDRESS, "TNPx9Vp8qA2K8bNmC1LkQo4U8Z1VpM7J1W") ?: "TNPx9Vp8qA2K8bNmC1LkQo4U8Z1VpM7J1W"

        return BankAccountConfig(
            bankName = bankName,
            accountNumber = accountNumber,
            accountName = accountName,
            whatsappNumber = effectiveWhatsapp,
            whatsappNumberSecondary = effectiveSecondary,
            usdtTrc20Address = usdtAddress
        )
    }

    fun saveBankConfig(context: Context, config: BankAccountConfig) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_BANK_NAME, config.bankName.trim())
            .putString(KEY_ACCOUNT_NUMBER, config.accountNumber.trim())
            .putString(KEY_ACCOUNT_NAME, config.accountName.trim())
            .putString(KEY_WHATSAPP_NUMBER, config.whatsappNumber.trim())
            .putString(KEY_WHATSAPP_SECONDARY, config.whatsappNumberSecondary.trim())
            .putString(KEY_USDT_ADDRESS, config.usdtTrc20Address.trim())
            .apply()
    }
}
