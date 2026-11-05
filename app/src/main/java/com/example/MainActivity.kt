package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.FilmDetailScreen
import com.example.ui.FilmHomeScreen
import com.example.ui.FilmViewModel
import com.example.ui.MopoleeLoginScreen
import com.example.ui.TwilioGlobalHubScreen
import com.example.ui.WhatsAppAiExchangeDashboard
import com.example.ui.components.InsideSettingsDialog
import com.example.ui.components.PaystackCheckoutDialog
import com.example.ui.components.SharePromoDialog
import com.example.ui.components.UploadFilmDialog
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: FilmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val currentColorTheme by viewModel.currentColorTheme.collectAsStateWithLifecycle()
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = currentColorTheme.backgroundColor
                ) {
                    MopoleeFilmApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MopoleeFilmApp(viewModel: FilmViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val isExchangeDashboardOpen by viewModel.isExchangeDashboardOpen.collectAsStateWithLifecycle()
    val isTwilioHubOpen by viewModel.isTwilioHubOpen.collectAsStateWithLifecycle()
    val customPaymentPackage by viewModel.customPaymentPackage.collectAsStateWithLifecycle()
    val films by viewModel.films.collectAsStateWithLifecycle()
    val activeFilm by viewModel.activeFilm.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isUploadDialogOpen by viewModel.isUploadDialogOpen.collectAsStateWithLifecycle()
    val isPaystackDialogOpen by viewModel.isPaystackDialogOpen.collectAsStateWithLifecycle()
    val isSharePromoDialogOpen by viewModel.isSharePromoDialogOpen.collectAsStateWithLifecycle()
    val isVipMember by viewModel.isVipMember.collectAsStateWithLifecycle()
    val isInsideSettingsOpen by viewModel.isInsideSettingsOpen.collectAsStateWithLifecycle()
    val currentAspectRatio by viewModel.currentAspectRatio.collectAsStateWithLifecycle()
    val isBigScreenMode by viewModel.isBigScreenMode.collectAsStateWithLifecycle()
    val currentColorTheme by viewModel.currentColorTheme.collectAsStateWithLifecycle()
    val ambientGlowEnabled by viewModel.ambientGlowEnabled.collectAsStateWithLifecycle()
    val autoTalkAloud by viewModel.autoTalkAloud.collectAsStateWithLifecycle()
    val voiceSpeed by viewModel.voiceSpeed.collectAsStateWithLifecycle()

    if (!isLoggedIn) {
        MopoleeLoginScreen(
            onLogin = { email, name -> viewModel.login(email, name) },
            onGoogleLogin = { viewModel.loginWithGoogle() },
            currentColorTheme = currentColorTheme,
            onColorThemeSelected = { viewModel.setColorTheme(it) }
        )
        return
    }

    when {
        activeFilm != null -> {
            FilmDetailScreen(
                film = activeFilm!!,
                allFilms = films,
                onBack = { viewModel.selectFilm(null) },
                onToggleFavorite = { viewModel.toggleFavorite(it) },
                onSelectFilm = { viewModel.selectFilm(it) },
                onDeleteFilm = { viewModel.deleteFilm(it) },
                aspectRatio = currentAspectRatio,
                onAspectRatioChanged = { viewModel.setAspectRatio(it) },
                isBigScreenMode = isBigScreenMode,
                currentColorTheme = currentColorTheme,
                ambientGlowEnabled = ambientGlowEnabled,
                autoTalkAloud = autoTalkAloud,
                voiceSpeed = voiceSpeed,
                onOpenInsideSettings = { viewModel.setInsideSettingsOpen(true) },
                isVipMember = isVipMember,
                onRequestPaystack = { viewModel.startPaystackOrder("VIP Cinema Pass (Episodes 1 to 32)", 250000) }
            )
        }
        isExchangeDashboardOpen -> {
            WhatsAppAiExchangeDashboard(
                onBack = { viewModel.setExchangeDashboardOpen(false) }
            )
        }
        isTwilioHubOpen -> {
            TwilioGlobalHubScreen(
                onBack = { viewModel.setTwilioHubOpen(false) },
                onBuyCredits = { title, amountKobo ->
                    viewModel.startPaystackOrder(title, amountKobo)
                }
            )
        }
        else -> {
            FilmHomeScreen(
                films = films,
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.setCategory(it) },
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                onFilmClick = { viewModel.selectFilm(it) },
                onPlayClick = { viewModel.selectFilm(it) },
                onToggleFavorite = { viewModel.toggleFavorite(it) },
                onDeleteFilm = { viewModel.deleteFilm(it) },
                onUploadClick = { viewModel.setUploadDialogOpen(true) },
                isVipMember = isVipMember,
                userEmail = userEmail,
                onOpenPaystack = { viewModel.setPaystackDialogOpen(true) },
                onOpenSharePromo = { viewModel.setSharePromoDialogOpen(true) },
                onOpenExchange = { viewModel.setExchangeDashboardOpen(true) },
                onOpenTwilio = { viewModel.setTwilioHubOpen(true) },
                onOpenInsideSettings = { viewModel.setInsideSettingsOpen(true) },
                currentColorTheme = currentColorTheme,
                onColorThemeSelected = { viewModel.setColorTheme(it) },
                isBigScreenMode = isBigScreenMode,
                onToggleBigScreenMode = { viewModel.toggleBigScreenMode() },
                userName = userName,
                onLogout = { viewModel.logout() },
                onOpenLogin = { viewModel.logout() }
            )
        }
    }

    // Dialogs
    if (isInsideSettingsOpen) {
        InsideSettingsDialog(
            currentAspectRatio = currentAspectRatio,
            onAspectRatioSelected = { viewModel.setAspectRatio(it) },
            isBigScreenMode = isBigScreenMode,
            onToggleBigScreenMode = { viewModel.setBigScreenMode(it) },
            currentColorTheme = currentColorTheme,
            onColorThemeSelected = { viewModel.setColorTheme(it) },
            ambientGlowEnabled = ambientGlowEnabled,
            onToggleAmbientGlow = { viewModel.setAmbientGlow(it) },
            autoTalkAloud = autoTalkAloud,
            onToggleAutoTalkAloud = { viewModel.setAutoTalkAloud(it) },
            voiceSpeed = voiceSpeed,
            onVoiceSpeedChanged = { viewModel.setVoiceSpeed(it) },
            onDismiss = { viewModel.setInsideSettingsOpen(false) },
            onRequestTwilioHub = {
                viewModel.setInsideSettingsOpen(false)
                viewModel.setTwilioHubOpen(true)
            },
            onRequestExchangeDashboard = {
                viewModel.setInsideSettingsOpen(false)
                viewModel.setExchangeDashboardOpen(true)
            },
            isVipMember = isVipMember,
            onRequestPaystack = {
                viewModel.setInsideSettingsOpen(false)
                viewModel.setPaystackDialogOpen(true)
            }
        )
    }

    if (isUploadDialogOpen) {
        UploadFilmDialog(
            onDismiss = { viewModel.setUploadDialogOpen(false) },
            onUploadFilm = { film ->
                viewModel.uploadFilm(film)
                viewModel.setUploadDialogOpen(false)
            }
        )
    }

    if (isPaystackDialogOpen) {
        val paymentHistory by viewModel.paymentHistory.collectAsStateWithLifecycle()
        val backendUrl by viewModel.backendUrl.collectAsStateWithLifecycle()
        val publicKey by viewModel.publicKey.collectAsStateWithLifecycle()

        PaystackCheckoutDialog(
            onDismiss = { viewModel.setPaystackDialogOpen(false) },
            customPackageTitle = customPaymentPackage?.first,
            customAmountKobo = customPaymentPackage?.second,
            transactions = paymentHistory,
            backendUrl = backendUrl,
            publicKey = publicKey,
            onUpdateBackendUrl = { viewModel.updateBackendUrl(it) },
            onInitializePayment = { email, amountKobo, title, ref ->
                viewModel.initializePaymentOnServer(email, amountKobo, title, ref)
            },
            onVerifyPaymentOnServer = { ref, amountKobo ->
                viewModel.verifyPaymentOnServer(ref, amountKobo)
            },
            onPaymentSuccess = { ref, amountKobo ->
                viewModel.onPaymentCompleted(ref, amountKobo)
            }
        )
    }

    if (isSharePromoDialogOpen) {
        SharePromoDialog(
            onDismiss = { viewModel.setSharePromoDialogOpen(false) }
        )
    }
}
