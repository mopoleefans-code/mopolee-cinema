package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CinemaColorTheme

enum class AuthStep {
    WELCOME,
    LOGIN,
    VERIFY_EMAIL
}

// Exact purple neon theme colors matching user reference image
val DeepObsidianBackground = Color(0xFF090514)
val PurpleGlowStart = Color(0xFF8B2FC9)
val PurpleGlowEnd = Color(0xFF5A189A)
val InputContainerDark = Color(0xFF140F24)
val InputBorderDark = Color(0xFF281C40)
val PurpleAccentPrimary = Color(0xFF7B2CBF)
val PurpleAccentSecondary = Color(0xFF6200EA)
val PurpleAccentBright = Color(0xFF9D4EDD)

@Composable
fun MopoleeLoginScreen(
    onLogin: (email: String, name: String) -> Unit,
    onGoogleLogin: () -> Unit,
    currentColorTheme: CinemaColorTheme = CinemaColorTheme.ROYAL_GOLD,
    onColorThemeSelected: (CinemaColorTheme) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(AuthStep.WELCOME) }
    var email by remember { mutableStateOf("mopoleefans@gmail.com") }
    var password by remember { mutableStateOf("••••••••") }
    var rememberMe by remember { mutableStateOf(true) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isSignUpMode by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("Mopolee Cinema Fan") }

    val otpDigits = remember { mutableStateListOf("8", "4", "9", "2", "1") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidianBackground)
    ) {
        // Top ambient purple light flare (matches reference design exactly)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PurpleGlowStart.copy(alpha = 0.6f),
                            PurpleGlowEnd.copy(alpha = 0.35f),
                            Color.Transparent
                        ),
                        center = Offset(300f, 100f),
                        radius = 500f
                    )
                )
        )

        AnimatedContent(
            targetState = currentStep,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "auth_screen_transition"
        ) { step ->
            when (step) {
                AuthStep.WELCOME -> {
                    // SCREEN 1: WELCOME ONBOARDING
                    WelcomeOnboardingScreen(
                        onNext = { currentStep = AuthStep.LOGIN },
                        onSkip = {
                            onLogin(email, userName)
                        }
                    )
                }
                AuthStep.LOGIN -> {
                    // SCREEN 2: LOGIN / SIGN UP
                    LoginMainScreen(
                        email = email,
                        onEmailChange = { email = it },
                        password = password,
                        onPasswordChange = { password = it },
                        rememberMe = rememberMe,
                        onRememberMeChange = { rememberMe = it },
                        isPasswordVisible = isPasswordVisible,
                        onTogglePasswordVisibility = { isPasswordVisible = !isPasswordVisible },
                        isSignUpMode = isSignUpMode,
                        userName = userName,
                        onUserNameChange = { userName = it },
                        onToggleSignUpMode = { isSignUpMode = !isSignUpMode },
                        onLoginClick = {
                            if (email.isBlank()) {
                                Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                            } else {
                                // Proceed to verify email screen as shown in reference design
                                currentStep = AuthStep.VERIFY_EMAIL
                            }
                        },
                        onGoogleLogin = onGoogleLogin,
                        onFacebookLogin = {
                            Toast.makeText(context, "Connecting via Facebook...", Toast.LENGTH_SHORT).show()
                            onLogin("facebook_fan@mopolee.com", "Mopolee Fan")
                        },
                        onBackToWelcome = { currentStep = AuthStep.WELCOME }
                    )
                }
                AuthStep.VERIFY_EMAIL -> {
                    // SCREEN 3: VERIFY YOUR EMAIL (OTP)
                    VerifyEmailScreen(
                        email = email,
                        otpDigits = otpDigits,
                        onVerifySuccess = {
                            Toast.makeText(context, "Email Verified Successfully!", Toast.LENGTH_SHORT).show()
                            onLogin(email, userName)
                        },
                        onResend = {
                            Toast.makeText(context, "Verification code resent to $email", Toast.LENGTH_SHORT).show()
                        },
                        onBack = { currentStep = AuthStep.LOGIN }
                    )
                }
            }
        }
    }
}

/**
 * Screen 1: Welcome Onboarding
 */
@Composable
private fun WelcomeOnboardingScreen(
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Status bar area spacer
        Spacer(modifier = Modifier.height(20.dp))

        // Space
        Spacer(modifier = Modifier.weight(1f))

        // Content Area at Bottom
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text(
                    text = "Welcome To",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Mopolee Cinema",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }

            Text(
                text = "Your smart companion ready to stream 30 episodes, watch in Big & Small screen, and explore VIP vaults 24/7.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                lineHeight = 20.sp
            )

            // Page Indicator (Purple pill + 2 dots)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 24.dp, height = 6.dp)
                        .background(PurpleAccentBright, RoundedCornerShape(3.dp))
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color.White.copy(alpha = 0.4f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color.White.copy(alpha = 0.4f), CircleShape)
                )
            }

            // Next Button
            Button(
                onClick = onNext,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurpleAccentPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Next",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            // Skip Button
            Text(
                text = "Skip",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSkip() }
                    .padding(vertical = 8.dp)
            )
        }
    }
}

/**
 * Screen 2: Login / Sign Up Screen
 */
@Composable
private fun LoginMainScreen(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    rememberMe: Boolean,
    onRememberMeChange: (Boolean) -> Unit,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    isSignUpMode: Boolean,
    userName: String,
    onUserNameChange: (String) -> Unit,
    onToggleSignUpMode: () -> Unit,
    onLoginClick: () -> Unit,
    onGoogleLogin: () -> Unit,
    onFacebookLogin: () -> Unit,
    onBackToWelcome: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (isSignUpMode) "Create Account" else "Welcome Back !",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = if (isSignUpMode) "Sign up to start streaming 30 episodes" else "Log in to continue using Mopolee Cinema",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // If Sign Up Mode, show Name Field
        if (isSignUpMode) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Name",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                OutlinedTextField(
                    value = userName,
                    onValueChange = onUserNameChange,
                    placeholder = { Text("Enter your name", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp) },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = InputContainerDark,
                        unfocusedContainerColor = InputContainerDark,
                        focusedBorderColor = PurpleAccentBright,
                        unfocusedBorderColor = InputBorderDark,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Email Field
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Email",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                placeholder = { Text("Enter your email", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = "Email",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = InputContainerDark,
                    unfocusedContainerColor = InputContainerDark,
                    focusedBorderColor = PurpleAccentBright,
                    unfocusedBorderColor = InputBorderDark,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Password Field
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Password",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                placeholder = { Text("Enter your Password", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Password",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = onTogglePasswordVisibility) {
                        Icon(
                            if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Password",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = InputContainerDark,
                    unfocusedContainerColor = InputContainerDark,
                    focusedBorderColor = PurpleAccentBright,
                    unfocusedBorderColor = InputBorderDark,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Remember Me & Forgot Password Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onRememberMeChange(!rememberMe) }
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(if (rememberMe) PurpleAccentBright else Color.Transparent, RoundedCornerShape(4.dp))
                        .border(1.dp, if (rememberMe) PurpleAccentBright else Color.White.copy(alpha = 0.4f), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (rememberMe) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Remeber Me.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }

            Text(
                text = "Forget Password ?",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { /* action */ }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Purple Gradient Login Button
        Button(
            onClick = onLoginClick,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PurpleAccentPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = if (isSignUpMode) "Sign Up" else "Login",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        // 'or' Divider
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.15f))
            Text(
                text = "or",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.15f))
        }

        // Social Login Buttons (Facebook and Google)
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Facebook circular button
            Surface(
                onClick = onFacebookLogin,
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "f",
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
            }

            // Google circular button
            Surface(
                onClick = onGoogleLogin,
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "G",
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Bottom Toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isSignUpMode) "Already have an account? " else "Don't have an account? ",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
            Text(
                text = if (isSignUpMode) "Login" else "Sign Up",
                color = PurpleAccentBright,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.clickable { onToggleSignUpMode() }
            )
        }

        // Small Welcome back button
        Text(
            text = "◀ Return to Intro",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 11.sp,
            modifier = Modifier.clickable { onBackToWelcome() }
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

/**
 * Screen 3: Verify Your Email (OTP Verification)
 */
@Composable
private fun VerifyEmailScreen(
    email: String,
    otpDigits: List<String>,
    onVerifySuccess: () -> Unit,
    onResend: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back button row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3D Neon Security Lock Hologram Container (matches image exactly)
        Box(
            modifier = Modifier
                .size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            // Neon glow back
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(PurpleAccentBright.copy(alpha = 0.6f), Color.Transparent),
                            radius = 160f
                        )
                    )
            )

            // Angled digital circuit cards representation
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E1438).copy(alpha = 0.75f),
                border = BorderStroke(1.dp, PurpleAccentBright.copy(alpha = 0.4f)),
                modifier = Modifier
                    .size(width = 130.dp, height = 75.dp)
            ) {}

            // Centered glowing padlock
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF1B0B36),
                border = BorderStroke(2.dp, PurpleAccentBright),
                modifier = Modifier
                    .size(76.dp)
                    .shadow(16.dp, RoundedCornerShape(18.dp))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Secured",
                        tint = PurpleAccentBright,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Title and description
        Text(
            text = "Verify Your Email",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp
        )

        Text(
            text = "We've sent a verification code to your email address\n$email",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 5 OTP Input Boxes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
        ) {
            otpDigits.forEachIndexed { index, digit ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = InputContainerDark,
                    border = BorderStroke(1.5.dp, if (digit.isNotBlank()) PurpleAccentBright else InputBorderDark),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = digit,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Purple Gradient Verify Code Button
        Button(
            onClick = onVerifySuccess,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PurpleAccentPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Verify Code",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        // Resend Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Didn't receive the code? ",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
            Text(
                text = "Resend",
                color = PurpleAccentBright,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.clickable { onResend() }
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
