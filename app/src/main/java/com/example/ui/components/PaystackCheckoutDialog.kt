package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.PaymentPackage
import com.example.data.PaystackPaymentStatus
import com.example.data.PaystackTransaction
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.DarkNavySurface
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaystackCheckoutDialog(
    onDismiss: () -> Unit,
    customPackageTitle: String? = null,
    customAmountKobo: Long? = null,
    transactions: List<PaystackTransaction> = emptyList(),
    backendUrl: String = "",
    publicKey: String = "",
    onUpdateBackendUrl: (String) -> Unit = {},
    onInitializePayment: suspend (email: String, amountKobo: Long, packageTitle: String, ref: String) -> Result<String>,
    onVerifyPaymentOnServer: suspend (ref: String, expectedAmountKobo: Long) -> Result<PaystackTransaction>,
    onPaymentSuccess: (reference: String, amountKobo: Long) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Checkout (₦)", "Payment History", "Server Config")

    var customerEmail by remember { mutableStateOf("mopoleefans@gmail.com") }
    var currentStep by remember { mutableStateOf<PaymentStep>(PaymentStep.Idle) }

    val defaultPackages = remember {
        listOf(
            PaymentPackage(
                id = "vip_monthly",
                title = "VIP Cinema Monthly Pass",
                subtitle = "Unlimited HD Movies & NOTLD Episodes 1 to 32",
                priceNgn = 2500,
                amountKobo = 250000,
                creditsBonus = 100,
                features = listOf("Unlock All 32 NOTLD Episodes", "Full 9:16 Tall Reel & 16:9 Cinema", "Spoken Word Audio Narration", "Zero Ads & High Bitrate Stream"),
                isPopular = true
            ),
            PaymentPackage(
                id = "vip_annual",
                title = "VIP Annual Pass (Best Value)",
                subtitle = "1 Year Unlimited Access + WhatsApp Exchange VIP",
                priceNgn = 18000,
                amountKobo = 1800000,
                creditsBonus = 1000,
                features = listOf("12 Months Complete VIP Access", "Priority WhatsApp Dispatch", "Direct Exchange Payouts", "Exclusive 4K Master Vault")
            ),
            PaymentPackage(
                id = "notld_vault",
                title = "NOTLD Master Vault Pass",
                subtitle = "Permanent All-Episode Cinema Pass",
                priceNgn = 5000,
                amountKobo = 500000,
                creditsBonus = 250,
                features = listOf("Lifetime NOTLD Episodes 1-32", "Offline Playback Cache", "Director Cut Audio Commentary")
            )
        )
    }

    var selectedPackage by remember { mutableStateOf(defaultPackages.first()) }
    val effectiveAmountKobo = customAmountKobo ?: selectedPackage.amountKobo
    val effectiveTitle = customPackageTitle ?: selectedPackage.title
    val effectiveAmountNgn = effectiveAmountKobo / 100

    var tempBackendUrl by remember { mutableStateOf(backendUrl) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
            border = BorderStroke(1.5.dp, CinemaGold)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = CinemaGold.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Payment, contentDescription = "Paystack", tint = CinemaGold, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "PAYSTACK SECURE CHECKOUT",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "All payments in Nigerian Naira (₦)",
                                color = CinemaGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Tabs
                SecondaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = CinemaGold,
                    indicator = {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(selectedTabIndex, matchContentSize = true),
                            color = CinemaGold
                        )
                    }
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTabIndex == index) CinemaGold else Color.White.copy(alpha = 0.7f)
                                )
                            }
                        )
                    }
                }

                // Tab Content
                when (selectedTabIndex) {
                    0 -> {
                        // CHECKOUT TAB
                        CheckoutTabContent(
                            currentStep = currentStep,
                            customerEmail = customerEmail,
                            onEmailChange = { customerEmail = it },
                            packages = defaultPackages,
                            selectedPackage = selectedPackage,
                            onSelectPackage = { selectedPackage = it },
                            effectiveTitle = effectiveTitle,
                            effectiveAmountKobo = effectiveAmountKobo,
                            effectiveAmountNgn = effectiveAmountNgn,
                            isCustomOrder = customAmountKobo != null,
                            onStartCheckout = {
                                val generatedRef = "MOP_" + System.currentTimeMillis() + "_" + (1000..9999).random()
                                currentStep = PaymentStep.Initializing(generatedRef)

                                scope.launch {
                                    // 1. Initialize on backend
                                    val initResult = onInitializePayment(
                                        customerEmail,
                                        effectiveAmountKobo,
                                        effectiveTitle,
                                        generatedRef
                                    )

                                    val checkoutUrl = initResult.getOrNull() ?: "https://checkout.paystack.com"
                                    currentStep = PaymentStep.AwaitingPayment(generatedRef, checkoutUrl)
                                }
                            },
                            onAuthorizePayment = { ref, shouldSucceed ->
                                currentStep = PaymentStep.VerifyingOnServer(ref)

                                scope.launch {
                                    if (shouldSucceed) {
                                        val verifyResult = onVerifyPaymentOnServer(ref, effectiveAmountKobo)
                                        if (verifyResult.isSuccess) {
                                            val verifiedTx = verifyResult.getOrThrow()
                                            currentStep = PaymentStep.Successful(verifiedTx)
                                            onPaymentSuccess(verifiedTx.reference, verifiedTx.amountKobo)
                                            Toast.makeText(context, "VIP Pass Activated! Status: Successful", Toast.LENGTH_LONG).show()
                                        } else {
                                            val err = verifyResult.exceptionOrNull()?.message ?: "Server verification failed"
                                            currentStep = PaymentStep.Failed(ref, err)
                                            Toast.makeText(context, "Payment Failed: $err", Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        currentStep = PaymentStep.Failed(ref, "Transaction was cancelled or declined by customer bank.")
                                        Toast.makeText(context, "Payment Failed / Declined", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onResetStep = {
                                currentStep = PaymentStep.Idle
                            },
                            onDismiss = onDismiss
                        )
                    }
                    1 -> {
                        // PAYMENT HISTORY TAB
                        PaymentHistoryTabContent(
                            transactions = transactions,
                            onCopyReference = { ref ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Paystack Ref", ref))
                                Toast.makeText(context, "Copied reference: $ref", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    2 -> {
                        // SERVER CONFIG TAB
                        ServerConfigTabContent(
                            backendUrl = tempBackendUrl,
                            onBackendUrlChange = { tempBackendUrl = it },
                            onSaveBackendUrl = {
                                onUpdateBackendUrl(tempBackendUrl)
                                Toast.makeText(context, "Backend URL updated to $tempBackendUrl", Toast.LENGTH_SHORT).show()
                            },
                            publicKey = publicKey
                        )
                    }
                }
            }
        }
    }
}

sealed class PaymentStep {
    data object Idle : PaymentStep()
    data class Initializing(val reference: String) : PaymentStep()
    data class AwaitingPayment(val reference: String, val checkoutUrl: String) : PaymentStep()
    data class VerifyingOnServer(val reference: String) : PaymentStep()
    data class Successful(val transaction: PaystackTransaction) : PaymentStep()
    data class Failed(val reference: String, val reason: String) : PaymentStep()
}

@Composable
private fun CheckoutTabContent(
    currentStep: PaymentStep,
    customerEmail: String,
    onEmailChange: (String) -> Unit,
    packages: List<PaymentPackage>,
    selectedPackage: PaymentPackage,
    onSelectPackage: (PaymentPackage) -> Unit,
    effectiveTitle: String,
    effectiveAmountKobo: Long,
    effectiveAmountNgn: Long,
    isCustomOrder: Boolean,
    onStartCheckout: () -> Unit,
    onAuthorizePayment: (ref: String, shouldSucceed: Boolean) -> Unit,
    onResetStep: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        when (currentStep) {
            is PaymentStep.Idle -> {
                // Package Selection (if not pre-customized)
                if (!isCustomOrder) {
                    Text(
                        text = "SELECT CINEMA PASS (NIGERIAN NAIRA):",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )

                    packages.forEach { pkg ->
                        val isSelected = pkg.id == selectedPackage.id
                        Surface(
                            onClick = { onSelectPackage(pkg) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) CinemaGold.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f),
                            border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) CinemaGold else Color.White.copy(alpha = 0.15f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = pkg.title,
                                            color = if (isSelected) CinemaGold else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        if (pkg.isPopular) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = CinemaGold,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "POPULAR",
                                                    color = Color.Black,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = pkg.subtitle,
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = "₦${String.format(Locale.US, "%,d", pkg.priceNgn)}",
                                    color = if (isSelected) CinemaGold else Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                } else {
                    // Custom order box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CinemaGold.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, CinemaGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = effectiveTitle, color = CinemaGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = "Amount: ₦${String.format(Locale.US, "%,d", effectiveAmountNgn)} NGN",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // Customer Email
                OutlinedTextField(
                    value = customerEmail,
                    onValueChange = onEmailChange,
                    label = { Text("Customer Paystack Email") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CinemaGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Security Note
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Security", tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                    Text(
                        text = "Secured by Paystack API & 256-bit server verification",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }

                // Pay Button
                Button(
                    onClick = onStartCheckout,
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payment, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pay ₦${String.format(Locale.US, "%,d", effectiveAmountNgn)} with Paystack",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            is PaymentStep.Initializing -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = CinemaGold)
                    Text(
                        text = "Initializing Payment with Paystack...",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Reference: ${currentStep.reference}",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Status: PENDING",
                        color = Color(0xFFFFB300),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                }
            }

            is PaymentStep.AwaitingPayment -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFB300).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFFFFB300)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color(0xFFFFB300), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PAYMENT STATUS: PENDING",
                                    color = Color(0xFFFFB300),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Awaiting payment authorization for ₦${String.format(Locale.US, "%,d", effectiveAmountNgn)}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Reference: ${currentStep.reference}",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Live Paystack Payment Page Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF00C853).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFF00C853)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Payment, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "OFFICIAL PAYSTACK PAYMENT PAGE",
                                    color = Color(0xFF00E676),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = "https://paystack.shop/pay/mjzyo3ejz0",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Open Browser Option
                    Button(
                        onClick = {
                            try {
                                val targetUrl = if (currentStep.checkoutUrl.isNotBlank() && !currentStep.checkoutUrl.endsWith("checkout.paystack.com")) {
                                    currentStep.checkoutUrl
                                } else {
                                    "https://paystack.shop/pay/mjzyo3ejz0"
                                }
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                                context.startActivity(browserIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open browser: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Live Paystack Page (paystack.shop)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Text(
                        text = "Once you complete your payment on Paystack:",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )

                    // Payment Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onAuthorizePayment(currentStep.reference, true) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("I Have Paid • Unlock VIP", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onAuthorizePayment(currentStep.reference, false) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF334B).copy(alpha = 0.8f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(0.7f)
                        ) {
                            Text("Cancel", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = onResetStep,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel Order", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                }
            }

            is PaymentStep.VerifyingOnServer -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = CinemaGold)
                    Text(
                        text = "Verifying with Paystack Server API...",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Validating reference ${currentStep.reference}...",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Never trusting frontend alone • Server-to-server check",
                        color = CinemaGold,
                        fontSize = 11.sp
                    )
                }
            }

            is PaymentStep.Successful -> {
                val tx = currentStep.transaction
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(60.dp)
                    )
                    Text(
                        text = "PAYMENT SUCCESSFUL!",
                        color = Color(0xFF00E676),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "All 32 Episodes & VIP Cinema Features Unlocked",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.05f),
                        border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = "Package: ${tx.packageTitle}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(text = "Amount Paid: ${tx.formattedNaira} NGN", color = CinemaGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(text = "Reference: ${tx.reference}", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            Text(text = "Date: ${tx.formattedDate}", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            Text(text = "Verification: Verified by Server API", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = CinemaGold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Start Watching VIP Cinema Now", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            is PaymentStep.Failed -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Failed",
                        tint = Color(0xFFFF334B),
                        modifier = Modifier.size(54.dp)
                    )
                    Text(
                        text = "PAYMENT FAILED",
                        color = Color(0xFFFF334B),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = currentStep.reason,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Ref: ${currentStep.reference}",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onResetStep,
                            colors = ButtonDefaults.buttonColors(containerColor = CinemaGold),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Try Again", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Close", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentHistoryTabContent(
    transactions: List<PaystackTransaction>,
    onCopyReference: (String) -> Unit
) {
    if (transactions.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.History, contentDescription = null, tint = CinemaGold, modifier = Modifier.size(48.dp))
            Text(
                text = "No Payment History Yet",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "Your completed Paystack transactions will appear here.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(transactions, key = { it.id }) { tx ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.04f),
                    border = BorderStroke(
                        1.dp,
                        when (tx.status) {
                            PaystackPaymentStatus.SUCCESSFUL -> Color(0xFF00E676).copy(alpha = 0.4f)
                            PaystackPaymentStatus.FAILED -> Color(0xFFFF334B).copy(alpha = 0.4f)
                            PaystackPaymentStatus.PENDING -> Color(0xFFFFB300).copy(alpha = 0.4f)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = tx.packageTitle,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            // Status Pill
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when (tx.status) {
                                    PaystackPaymentStatus.SUCCESSFUL -> Color(0xFF00E676).copy(alpha = 0.2f)
                                    PaystackPaymentStatus.FAILED -> Color(0xFFFF334B).copy(alpha = 0.2f)
                                    PaystackPaymentStatus.PENDING -> Color(0xFFFFB300).copy(alpha = 0.2f)
                                }
                            ) {
                                Text(
                                    text = tx.status.name,
                                    color = when (tx.status) {
                                        PaystackPaymentStatus.SUCCESSFUL -> Color(0xFF00E676)
                                        PaystackPaymentStatus.FAILED -> Color(0xFFFF334B)
                                        PaystackPaymentStatus.PENDING -> Color(0xFFFFB300)
                                    },
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${tx.formattedNaira} NGN",
                                color = CinemaGold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = tx.formattedDate,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Ref: ${tx.reference}",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onCopyReference(tx.reference) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = CinemaGold,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        if (tx.statusMessage.isNotBlank()) {
                            Text(
                                text = tx.statusMessage,
                                color = if (tx.isSuccessful) Color(0xFF00E676).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerConfigTabContent(
    backendUrl: String,
    onBackendUrlChange: (String) -> Unit,
    onSaveBackendUrl: () -> Unit,
    publicKey: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF00E676).copy(alpha = 0.1f),
            border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SECURE SERVER ARCHITECTURE",
                        color = Color(0xFF00E676),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your Paystack Secret Key is stored exclusively on your backend environment variables and is never exposed in the Android app.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
            }
        }

        Text(
            text = "PAYSTACK PUBLIC KEY (CLIENT-SAFE):",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color.White.copy(alpha = 0.05f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = publicKey.ifBlank { "pk_test_sample_mopolee_cinema" },
                color = CinemaGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(12.dp)
            )
        }

        Text(
            text = "SECURE BACKEND VERIFICATION URL:",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )

        OutlinedTextField(
            value = backendUrl,
            onValueChange = onBackendUrlChange,
            placeholder = { Text("https://your-backend.onrender.com") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CinemaGold,
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onSaveBackendUrl,
            colors = ButtonDefaults.buttonColors(containerColor = CinemaGold),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Backend URL", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Backend Deployment Quick Tip:\nDeploy the included `/backend` directory to Render or Railway and set `PAYSTACK_SECRET_KEY=sk_test_...` in its environment settings.",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 11.sp
        )
    }
}
