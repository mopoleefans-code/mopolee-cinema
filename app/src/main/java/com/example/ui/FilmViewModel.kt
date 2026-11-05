package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CinemaColorTheme
import com.example.data.Film
import com.example.data.FilmRepository
import com.example.data.MovieAspectRatio
import com.example.data.PaystackPaymentStatus
import com.example.data.PaystackRepository
import com.example.data.PaystackTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FilmViewModel(application: Application) : AndroidViewModel(application) {

    private val paystackRepository = PaystackRepository(application)

    private val _films = MutableStateFlow<List<Film>>(FilmRepository.initialFilms)
    val films: StateFlow<List<Film>> = _films.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeFilm = MutableStateFlow<Film?>(null)
    val activeFilm: StateFlow<Film?> = _activeFilm.asStateFlow()

    private val _isUploadDialogOpen = MutableStateFlow(false)
    val isUploadDialogOpen: StateFlow<Boolean> = _isUploadDialogOpen.asStateFlow()

    private val _isPaystackDialogOpen = MutableStateFlow(false)
    val isPaystackDialogOpen: StateFlow<Boolean> = _isPaystackDialogOpen.asStateFlow()

    private val _isSharePromoDialogOpen = MutableStateFlow(false)
    val isSharePromoDialogOpen: StateFlow<Boolean> = _isSharePromoDialogOpen.asStateFlow()

    private val _isExchangeDashboardOpen = MutableStateFlow(false)
    val isExchangeDashboardOpen: StateFlow<Boolean> = _isExchangeDashboardOpen.asStateFlow()

    private val _isTwilioHubOpen = MutableStateFlow(false)
    val isTwilioHubOpen: StateFlow<Boolean> = _isTwilioHubOpen.asStateFlow()

    private val _customPaymentPackage = MutableStateFlow<Pair<String, Long>?>(null)
    val customPaymentPackage: StateFlow<Pair<String, Long>?> = _customPaymentPackage.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userEmail = MutableStateFlow("mopoleefans@gmail.com")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userName = MutableStateFlow("Mopolee Cinema Fan")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _isGoogleUser = MutableStateFlow(true)
    val isGoogleUser: StateFlow<Boolean> = _isGoogleUser.asStateFlow()

    // VIP Member state is tied to server-verified transactions in PaystackRepository
    private val _isVipMember = MutableStateFlow(paystackRepository.isVipUnlocked())
    val isVipMember: StateFlow<Boolean> = _isVipMember.asStateFlow()

    private val _paymentHistory = MutableStateFlow<List<PaystackTransaction>>(paystackRepository.getTransactions())
    val paymentHistory: StateFlow<List<PaystackTransaction>> = _paymentHistory.asStateFlow()

    private val _backendUrl = MutableStateFlow(paystackRepository.getBackendUrl())
    val backendUrl: StateFlow<String> = _backendUrl.asStateFlow()

    private val _publicKey = MutableStateFlow(paystackRepository.getPublicKey())
    val publicKey: StateFlow<String> = _publicKey.asStateFlow()

    private val _lastTransactionRef = MutableStateFlow<String?>(null)
    val lastTransactionRef: StateFlow<String?> = _lastTransactionRef.asStateFlow()

    private val _isInsideSettingsOpen = MutableStateFlow(false)
    val isInsideSettingsOpen: StateFlow<Boolean> = _isInsideSettingsOpen.asStateFlow()

    private val _currentAspectRatio = MutableStateFlow(MovieAspectRatio.RATIO_9_16)
    val currentAspectRatio: StateFlow<MovieAspectRatio> = _currentAspectRatio.asStateFlow()

    private val _isBigScreenMode = MutableStateFlow(true)
    val isBigScreenMode: StateFlow<Boolean> = _isBigScreenMode.asStateFlow()

    private val _currentColorTheme = MutableStateFlow(CinemaColorTheme.ROYAL_GOLD)
    val currentColorTheme: StateFlow<CinemaColorTheme> = _currentColorTheme.asStateFlow()

    private val _ambientGlowEnabled = MutableStateFlow(true)
    val ambientGlowEnabled: StateFlow<Boolean> = _ambientGlowEnabled.asStateFlow()

    private val _autoTalkAloud = MutableStateFlow(true)
    val autoTalkAloud: StateFlow<Boolean> = _autoTalkAloud.asStateFlow()

    private val _voiceSpeed = MutableStateFlow(1.0f)
    val voiceSpeed: StateFlow<Float> = _voiceSpeed.asStateFlow()

    private val _isCompactHomeCards = MutableStateFlow(false)
    val isCompactHomeCards: StateFlow<Boolean> = _isCompactHomeCards.asStateFlow()

    init {
        refreshPaymentHistory()
    }

    fun refreshPaymentHistory() {
        _paymentHistory.value = paystackRepository.getTransactions()
        _isVipMember.value = paystackRepository.isVipUnlocked()
    }

    /**
     * Determines if an episode requires VIP access.
     * Episodes 1 and 2 are free previews.
     * Episodes 3 to 32 require VIP pass.
     */
    fun isEpisodeLocked(film: Film): Boolean {
        if (_isVipMember.value) return false
        if (film.isUserUpload) return false
        val epNum = film.episodeNumber ?: return false
        return epNum > 2
    }

    /**
     * Checks if the user can play or download a film.
     * If locked, immediately prompts the Paystack Checkout Dialog!
     */
    fun canAccessFilm(film: Film): Boolean {
        if (isEpisodeLocked(film)) {
            startPaystackOrder(
                title = "VIP Cinema Pass (Unlock Episodes 1 to 32)",
                amountKobo = 250000 // ₦2,500
            )
            return false
        }
        return true
    }

    suspend fun initializePaymentOnServer(
        email: String,
        amountKobo: Long,
        packageTitle: String,
        reference: String
    ): Result<String> {
        val initialTx = PaystackTransaction(
            reference = reference,
            amountKobo = amountKobo,
            amountNgn = amountKobo / 100,
            currency = "NGN",
            customerEmail = email,
            packageTitle = packageTitle,
            status = PaystackPaymentStatus.PENDING,
            statusMessage = "Initialized, awaiting authorization",
            timestampMillis = System.currentTimeMillis()
        )
        paystackRepository.saveTransaction(initialTx)
        refreshPaymentHistory()

        return paystackRepository.initializePaymentOnServer(email, amountKobo, packageTitle, reference)
    }

    suspend fun verifyPaymentOnServer(
        reference: String,
        expectedAmountKobo: Long
    ): Result<PaystackTransaction> {
        val result = paystackRepository.verifyPaymentOnServer(reference, expectedAmountKobo)
        refreshPaymentHistory()
        return result
    }

    fun onPaymentCompleted(reference: String, amountKobo: Long) {
        _lastTransactionRef.value = reference
        _isVipMember.value = true
        paystackRepository.setVipUnlocked(true)
        _isPaystackDialogOpen.value = false
        refreshPaymentHistory()
    }

    fun updateBackendUrl(url: String) {
        paystackRepository.setBackendUrl(url)
        _backendUrl.value = paystackRepository.getBackendUrl()
    }

    fun login(email: String, name: String) {
        _userEmail.value = email
        _userName.value = name
        _isGoogleUser.value = false
        _isLoggedIn.value = true
    }

    fun loginWithGoogle(email: String = "mopoleefans@gmail.com", displayName: String = "Mopolee VIP Cinema User") {
        _userEmail.value = email
        _userName.value = displayName
        _isGoogleUser.value = true
        _isLoggedIn.value = true
    }

    fun register(name: String, email: String, password: String) {
        _userName.value = if (name.isNotBlank()) name else "Mopolee Member"
        _userEmail.value = email
        _isGoogleUser.value = false
        _isLoggedIn.value = true
    }

    fun toggleBigScreenMode() {
        val nextMode = !_isBigScreenMode.value
        _isBigScreenMode.value = nextMode
        _currentAspectRatio.value = if (nextMode) MovieAspectRatio.RATIO_9_16 else MovieAspectRatio.RATIO_16_9
    }

    fun setBigSmallScreen(isBig: Boolean) {
        _isBigScreenMode.value = isBig
        _currentAspectRatio.value = if (isBig) MovieAspectRatio.RATIO_9_16 else MovieAspectRatio.RATIO_16_9
    }

    fun toggleCompactHomeCards() {
        _isCompactHomeCards.value = !_isCompactHomeCards.value
    }

    fun logout() {
        _isLoggedIn.value = false
        _isGoogleUser.value = false
        _activeFilm.value = null
        _isExchangeDashboardOpen.value = false
        _isTwilioHubOpen.value = false
    }

    fun selectFilm(film: Film?) {
        _activeFilm.value = film
    }

    fun uploadFilm(film: Film) {
        _films.value = listOf(film) + _films.value
    }

    fun toggleFavorite(film: Film) {
        _films.value = _films.value.map {
            if (it.id == film.id) it.copy(isFavorite = !it.isFavorite) else it
        }
        if (_activeFilm.value?.id == film.id) {
            _activeFilm.value = _activeFilm.value?.copy(isFavorite = !(_activeFilm.value?.isFavorite ?: false))
        }
    }

    fun deleteFilm(film: Film) {
        _films.value = _films.value.filter { it.id != film.id }
        if (_activeFilm.value?.id == film.id) {
            _activeFilm.value = null
        }
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setUploadDialogOpen(open: Boolean) {
        _isUploadDialogOpen.value = open
    }

    fun setPaystackDialogOpen(open: Boolean) {
        _isPaystackDialogOpen.value = open
        if (open) refreshPaymentHistory()
    }

    fun setSharePromoDialogOpen(open: Boolean) {
        _isSharePromoDialogOpen.value = open
    }

    fun setExchangeDashboardOpen(open: Boolean) {
        _isExchangeDashboardOpen.value = open
    }

    fun setTwilioHubOpen(open: Boolean) {
        _isTwilioHubOpen.value = open
    }

    fun setInsideSettingsOpen(open: Boolean) {
        _isInsideSettingsOpen.value = open
    }

    fun setAspectRatio(ratio: MovieAspectRatio) {
        _currentAspectRatio.value = ratio
        _isBigScreenMode.value = ratio.isBigScreen
    }

    fun setBigScreenMode(enabled: Boolean) {
        _isBigScreenMode.value = enabled
        _currentAspectRatio.value = if (enabled) MovieAspectRatio.RATIO_9_16 else MovieAspectRatio.RATIO_16_9
    }

    fun setColorTheme(theme: CinemaColorTheme) {
        _currentColorTheme.value = theme
    }

    fun setAmbientGlow(enabled: Boolean) {
        _ambientGlowEnabled.value = enabled
    }

    fun setAutoTalkAloud(enabled: Boolean) {
        _autoTalkAloud.value = enabled
    }

    fun setVoiceSpeed(speed: Float) {
        _voiceSpeed.value = speed
    }

    fun startPaystackOrder(title: String, amountKobo: Long) {
        _customPaymentPackage.value = Pair(title, amountKobo)
        _isPaystackDialogOpen.value = true
        refreshPaymentHistory()
    }
}
