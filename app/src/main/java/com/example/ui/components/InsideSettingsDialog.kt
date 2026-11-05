package com.example.ui.components

import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CinemaColorTheme
import com.example.data.MovieAspectRatio

@Composable
fun InsideSettingsDialog(
    currentAspectRatio: MovieAspectRatio,
    onAspectRatioSelected: (MovieAspectRatio) -> Unit,
    isBigScreenMode: Boolean,
    onToggleBigScreenMode: (Boolean) -> Unit,
    currentColorTheme: CinemaColorTheme,
    onColorThemeSelected: (CinemaColorTheme) -> Unit,
    ambientGlowEnabled: Boolean,
    onToggleAmbientGlow: (Boolean) -> Unit,
    autoTalkAloud: Boolean,
    onToggleAutoTalkAloud: (Boolean) -> Unit,
    voiceSpeed: Float,
    onVoiceSpeedChanged: (Float) -> Unit,
    onDismiss: () -> Unit,
    onRequestTwilioHub: () -> Unit = {},
    onRequestExchangeDashboard: () -> Unit = {},
    isVipMember: Boolean = false,
    onRequestPaystack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = currentColorTheme.surfaceColor),
            border = BorderStroke(1.5.dp, currentColorTheme.primaryColor)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "🎬 CINEMA SETTINGS",
                            color = currentColorTheme.primaryColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Big Screen, Small Screen & Interior Colors",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // 1. BIG / SMALL MOVIE SCREEN SIZING
                Text(
                    text = "MOVIE SCREEN SIZING (BIG / SMALL):",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                // Quick Big vs Small Toggle Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isBigActive = currentAspectRatio == MovieAspectRatio.RATIO_9_16 || isBigScreenMode
                    Surface(
                        onClick = {
                            onAspectRatioSelected(MovieAspectRatio.RATIO_9_16)
                            onToggleBigScreenMode(true)
                            Toast.makeText(context, "Movie Screen: BIG (9:16 Reel)", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isBigActive) currentColorTheme.primaryColor else currentColorTheme.backgroundColor,
                        border = BorderStroke(1.dp, currentColorTheme.cardBorderColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📱 BIG SCREEN",
                                color = if (isBigActive) Color.Black else Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "9:16 Tall Reel",
                                color = if (isBigActive) Color.Black.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    val isSmallActive = currentAspectRatio == MovieAspectRatio.RATIO_16_9 || (!isBigScreenMode && currentAspectRatio != MovieAspectRatio.RATIO_9_16)
                    Surface(
                        onClick = {
                            onAspectRatioSelected(MovieAspectRatio.RATIO_16_9)
                            onToggleBigScreenMode(false)
                            Toast.makeText(context, "Movie Screen: SMALL (16:9 Cinema)", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSmallActive) currentColorTheme.primaryColor else currentColorTheme.backgroundColor,
                        border = BorderStroke(1.dp, currentColorTheme.cardBorderColor),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🎬 SMALL SCREEN",
                                color = if (isSmallActive) Color.Black else Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "16:9 Standard",
                                color = if (isSmallActive) Color.Black.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // All Aspect Ratio Choices
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    MovieAspectRatio.entries.forEach { ratio ->
                        val isSelected = ratio == currentAspectRatio
                        Surface(
                            onClick = {
                                onAspectRatioSelected(ratio)
                                onToggleBigScreenMode(ratio.isBigScreen)
                                Toast.makeText(context, "Set to ${ratio.label}", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) currentColorTheme.primaryColor.copy(alpha = 0.2f) else Color.Transparent,
                            border = BorderStroke(1.dp, if (isSelected) currentColorTheme.primaryColor else currentColorTheme.cardBorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = ratio.label,
                                    color = if (isSelected) currentColorTheme.primaryColor else Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                                if (isSelected) {
                                    Text(
                                        text = "ACTIVE",
                                        color = currentColorTheme.primaryColor,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. INSIDE INTERIOR THEMES
                Text(
                    text = "INSIDE COLOR THEMES:",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CinemaColorTheme.entries.take(3).forEach { theme ->
                        val isSelected = theme == currentColorTheme
                        Surface(
                            onClick = {
                                onColorThemeSelected(theme)
                                Toast.makeText(context, "Theme: ${theme.displayName}", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = theme.backgroundColor,
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) theme.primaryColor else Color.Transparent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(theme.primaryColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = theme.displayName.split(" ").first(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CinemaColorTheme.entries.drop(3).forEach { theme ->
                        val isSelected = theme == currentColorTheme
                        Surface(
                            onClick = {
                                onColorThemeSelected(theme)
                                Toast.makeText(context, "Theme: ${theme.displayName}", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = theme.backgroundColor,
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) theme.primaryColor else Color.Transparent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(theme.primaryColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = theme.displayName.split(" ").first(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 3. TALK OUT LOUD / WORDS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Talk Out Loud (Spoken Words)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Narrate episode names & status aloud",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = autoTalkAloud,
                        onCheckedChange = onToggleAutoTalkAloud,
                        colors = SwitchDefaults.colors(checkedThumbColor = currentColorTheme.primaryColor)
                    )
                }

                // 4. AMBIENT GLOW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Inside Neon Ambient Glow",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Soft neon theater backlighting",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = ambientGlowEnabled,
                        onCheckedChange = onToggleAmbientGlow,
                        colors = SwitchDefaults.colors(checkedThumbColor = currentColorTheme.primaryColor)
                    )
                }

                // 5. PAYSTACK VIP MEMBERSHIP & PAYMENT HISTORY
                Surface(
                    onClick = {
                        onRequestPaystack()
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = currentColorTheme.primaryColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, currentColorTheme.primaryColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isVipMember) "👑 VIP MEMBERSHIP: ACTIVE" else "⚡ VIP MEMBERSHIP: FREE TIER",
                                color = if (isVipMember) Color(0xFF00E676) else currentColorTheme.primaryColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Paystack Checkout & Payment History (₦)",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = "OPEN >",
                            color = currentColorTheme.primaryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                // 6. QUICK SHORTCUTS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRequestTwilioHub,
                        colors = ButtonDefaults.buttonColors(containerColor = currentColorTheme.backgroundColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("SMS Hub", fontSize = 11.sp, color = currentColorTheme.primaryColor)
                    }
                    Button(
                        onClick = onRequestExchangeDashboard,
                        colors = ButtonDefaults.buttonColors(containerColor = currentColorTheme.backgroundColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Exchange", fontSize = 11.sp, color = currentColorTheme.primaryColor)
                    }
                }
            }
        }
    }
}
