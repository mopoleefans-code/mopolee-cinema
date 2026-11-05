package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class PaystackRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("mopolee_paystack_prefs", Context.MODE_PRIVATE)

    companion object {
        const val LIVE_PAYSTACK_PAY_PAGE_URL = "https://paystack.shop/pay/mjzyo3ejz0"
        private const val KEY_TRANSACTIONS = "paystack_transactions_json"
        private const val KEY_PROCESSED_REFS = "paystack_processed_refs"
        private const val KEY_VIP_UNLOCKED = "paystack_vip_unlocked"
        private const val KEY_CUSTOM_BACKEND_URL = "paystack_custom_backend_url"
        private const val TAG = "PaystackRepository"
    }

    fun getLivePayPageUrl(): String = LIVE_PAYSTACK_PAY_PAGE_URL

    fun getPublicKey(): String {
        return BuildConfig.PAYSTACK_PUBLIC_KEY.ifBlank { "pk_test_sample_mopolee_cinema" }
    }

    fun getBackendUrl(): String {
        val custom = prefs.getString(KEY_CUSTOM_BACKEND_URL, null)
        if (!custom.isNullOrBlank()) return custom.trim().removeSuffix("/")
        return BuildConfig.PAYSTACK_BACKEND_URL.ifBlank { "https://mopolee-paystack-backend.onrender.com" }.trim().removeSuffix("/")
    }

    fun setBackendUrl(url: String) {
        prefs.edit().putString(KEY_CUSTOM_BACKEND_URL, url.trim().removeSuffix("/")).apply()
    }

    fun isVipUnlocked(): Boolean {
        return prefs.getBoolean(KEY_VIP_UNLOCKED, false)
    }

    fun setVipUnlocked(unlocked: Boolean) {
        prefs.edit().putBoolean(KEY_VIP_UNLOCKED, unlocked).apply()
    }

    fun isReferenceAlreadyProcessed(reference: String): Boolean {
        val processedSet = prefs.getStringSet(KEY_PROCESSED_REFS, emptySet()) ?: emptySet()
        return processedSet.contains(reference)
    }

    fun markReferenceProcessed(reference: String) {
        val current = (prefs.getStringSet(KEY_PROCESSED_REFS, emptySet()) ?: emptySet()).toMutableSet()
        current.add(reference)
        prefs.edit().putStringSet(KEY_PROCESSED_REFS, current).apply()
    }

    @Synchronized
    fun getTransactions(): List<PaystackTransaction> {
        val rawJson = prefs.getString(KEY_TRANSACTIONS, null) ?: return defaultInitialTransactions()
        return try {
            val array = JSONArray(rawJson)
            val list = mutableListOf<PaystackTransaction>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val statusStr = obj.optString("status", PaystackPaymentStatus.PENDING.name)
                val status = try {
                    PaystackPaymentStatus.valueOf(statusStr)
                } catch (e: Exception) {
                    PaystackPaymentStatus.PENDING
                }

                list.add(
                    PaystackTransaction(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        reference = obj.optString("reference", ""),
                        amountKobo = obj.optLong("amountKobo", 250000),
                        amountNgn = obj.optLong("amountNgn", 2500),
                        currency = obj.optString("currency", "NGN"),
                        customerEmail = obj.optString("customerEmail", "customer@mopoleecinema.com"),
                        packageTitle = obj.optString("packageTitle", "VIP Cinema Pass"),
                        status = status,
                        statusMessage = obj.optString("statusMessage", ""),
                        channel = obj.optString("channel", "card"),
                        gatewayResponse = obj.optString("gatewayResponse", ""),
                        timestampMillis = obj.optLong("timestampMillis", System.currentTimeMillis()),
                        verifiedAtMillis = if (obj.has("verifiedAtMillis")) obj.getLong("verifiedAtMillis") else null,
                        isServerVerified = obj.optBoolean("isServerVerified", false)
                    )
                )
            }
            list.sortedByDescending { it.timestampMillis }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing payment history", e)
            defaultInitialTransactions()
        }
    }

    @Synchronized
    fun saveTransaction(tx: PaystackTransaction) {
        val currentList = getTransactions().toMutableList()
        val existingIndex = currentList.indexOfFirst { it.reference == tx.reference }
        if (existingIndex >= 0) {
            currentList[existingIndex] = tx
        } else {
            currentList.add(0, tx)
        }
        persistTransactions(currentList)
    }

    @Synchronized
    fun updateTransactionStatus(
        reference: String,
        status: PaystackPaymentStatus,
        message: String,
        isServerVerified: Boolean = false,
        gatewayResponse: String = ""
    ) {
        val currentList = getTransactions().toMutableList()
        val index = currentList.indexOfFirst { it.reference == reference }
        if (index >= 0) {
            val existing = currentList[index]
            currentList[index] = existing.copy(
                status = status,
                statusMessage = message,
                isServerVerified = isServerVerified,
                gatewayResponse = gatewayResponse.ifBlank { existing.gatewayResponse },
                verifiedAtMillis = if (status == PaystackPaymentStatus.SUCCESSFUL) System.currentTimeMillis() else existing.verifiedAtMillis
            )
            persistTransactions(currentList)
        }
    }

    private fun persistTransactions(list: List<PaystackTransaction>) {
        try {
            val array = JSONArray()
            for (tx in list) {
                val obj = JSONObject().apply {
                    put("id", tx.id)
                    put("reference", tx.reference)
                    put("amountKobo", tx.amountKobo)
                    put("amountNgn", tx.amountNgn)
                    put("currency", tx.currency)
                    put("customerEmail", tx.customerEmail)
                    put("packageTitle", tx.packageTitle)
                    put("status", tx.status.name)
                    put("statusMessage", tx.statusMessage)
                    put("channel", tx.channel)
                    put("gatewayResponse", tx.gatewayResponse)
                    put("timestampMillis", tx.timestampMillis)
                    tx.verifiedAtMillis?.let { put("verifiedAtMillis", it) }
                    put("isServerVerified", tx.isServerVerified)
                }
                array.put(obj)
            }
            prefs.edit().putString(KEY_TRANSACTIONS, array.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving transactions", e)
        }
    }

    private fun defaultInitialTransactions(): List<PaystackTransaction> {
        return emptyList()
    }

    /**
     * Calls secure backend initialization endpoint:
     * POST /api/paystack/initialize
     */
    suspend fun initializePaymentOnServer(
        email: String,
        amountKobo: Long,
        packageTitle: String,
        reference: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val backendUrl = getBackendUrl()
        val endpoint = "$backendUrl/api/paystack/initialize"

        try {
            val url = URL(endpoint)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8000
                readTimeout = 8000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }

            val requestBody = JSONObject().apply {
                put("email", email)
                put("amountKobo", amountKobo)
                put("packageTitle", packageTitle)
                put("reference", reference)
            }

            OutputStreamWriter(conn.outputStream).use { it.write(requestBody.toString()) }

            val responseCode = conn.responseCode
            val inputStream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseText = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }

            if (responseCode in 200..299) {
                val json = JSONObject(responseText)
                val data = json.optJSONObject("data")
                val authUrl = data?.optString("authorization_url")?.ifBlank { LIVE_PAYSTACK_PAY_PAGE_URL } ?: LIVE_PAYSTACK_PAY_PAGE_URL
                Result.success(authUrl)
            } else {
                Log.w(TAG, "Backend returned $responseCode: $responseText")
                // Use live Paystack storefront payment page
                Result.success(LIVE_PAYSTACK_PAY_PAGE_URL)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to connect to backend initialization ($backendUrl): ${e.message}")
            // Return live Paystack storefront payment page
            Result.success(LIVE_PAYSTACK_PAY_PAGE_URL)
        }
    }

    /**
     * Strict Server-Side Verification:
     * Calls GET /api/paystack/verify/:reference on the secure backend API.
     * The server verifies the transaction against Paystack and returns whether payment is verified.
     * We NEVER trust the frontend alone to unlock VIP.
     */
    suspend fun verifyPaymentOnServer(
        reference: String,
        expectedAmountKobo: Long
    ): Result<PaystackTransaction> = withContext(Dispatchers.IO) {
        // Prevent duplicate processing check
        if (isReferenceAlreadyProcessed(reference)) {
            val existing = getTransactions().find { it.reference == reference }
            if (existing != null && existing.isSuccessful) {
                return@withContext Result.success(existing)
            }
        }

        val backendUrl = getBackendUrl()
        val endpoint = "$backendUrl/api/paystack/verify/${reference}?expectedAmount=$expectedAmountKobo"

        try {
            val url = URL(endpoint)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
                setRequestProperty("Accept", "application/json")
            }

            val responseCode = conn.responseCode
            val inputStream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseText = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }

            if (responseCode in 200..299) {
                val json = JSONObject(responseText)
                val isVerified = json.optBoolean("verified", false)
                val statusStr = json.optString("status", "FAILED")

                if (isVerified && statusStr.equals("SUCCESSFUL", ignoreCase = true)) {
                    val txJson = json.optJSONObject("transaction")
                    val verifiedTx = PaystackTransaction(
                        reference = reference,
                        amountKobo = txJson?.optLong("amountKobo", expectedAmountKobo) ?: expectedAmountKobo,
                        amountNgn = (txJson?.optLong("amountKobo", expectedAmountKobo) ?: expectedAmountKobo) / 100,
                        currency = txJson?.optString("currency", "NGN") ?: "NGN",
                        customerEmail = txJson?.optString("email", "mopoleefans@gmail.com") ?: "mopoleefans@gmail.com",
                        packageTitle = txJson?.optString("packageTitle", "VIP Cinema Pass") ?: "VIP Cinema Pass",
                        status = PaystackPaymentStatus.SUCCESSFUL,
                        statusMessage = "Verified by Paystack Server API",
                        channel = txJson?.optString("channel", "card") ?: "card",
                        gatewayResponse = txJson?.optString("gatewayResponse", "Approved") ?: "Approved",
                        timestampMillis = System.currentTimeMillis(),
                        verifiedAtMillis = System.currentTimeMillis(),
                        isServerVerified = true
                    )

                    markReferenceProcessed(reference)
                    saveTransaction(verifiedTx)
                    setVipUnlocked(true)
                    Result.success(verifiedTx)
                } else {
                    val failMsg = json.optString("message", "Payment verification was declined by server")
                    updateTransactionStatus(reference, PaystackPaymentStatus.FAILED, failMsg, isServerVerified = false)
                    Result.failure(Exception(failMsg))
                }
            } else {
                val failMsg = "Backend server error ($responseCode): $responseText"
                updateTransactionStatus(reference, PaystackPaymentStatus.FAILED, failMsg, isServerVerified = false)
                Result.failure(Exception(failMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network exception verifying payment on $backendUrl: ${e.message}")
            // Fallback for offline / simulation if backend is not currently online
            val offlineMsg = "Could not reach verification backend server at $backendUrl (${e.localizedMessage ?: "Connection timed out"}). Please ensure your backend is deployed."
            updateTransactionStatus(reference, PaystackPaymentStatus.FAILED, offlineMsg, isServerVerified = false)
            Result.failure(Exception(offlineMsg))
        }
    }
}
