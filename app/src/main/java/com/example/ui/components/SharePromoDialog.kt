package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.DarkNavySurface

@Composable
fun SharePromoDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val promoText = "🎬 Watch Night of the Living Dead Episodes 1 to 32 Remastered in Big Screen Mode & Trade Gift Cards at top rates on MOPOLEE CINEMA & EXCHANGE! Download now: https://mopoleecinema.com/download"

    fun shareToSocial(platform: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "MOPOLEE Cinema & Exchange Demo")
            putExtra(Intent.EXTRA_TEXT, promoText)
        }
        try {
            context.startActivity(Intent.createChooser(shareIntent, "Share to $platform"))
        } catch (e: Exception) {
            Toast.makeText(context, "Sharing ready for $platform", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkNavySurface),
            border = BorderStroke(1.5.dp, CinemaGold)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = CinemaGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Promote on Social Media",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Text(
                    text = "Share MOPOLEE Cinema Demo directly to Facebook, Instagram, YouTube, X (Twitter), TikTok and WhatsApp!",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )

                // Platforms
                val platforms = listOf(
                    "WhatsApp" to Color(0xFF25D366),
                    "Facebook" to Color(0xFF1877F2),
                    "Instagram" to Color(0xFFE1306C),
                    "YouTube" to Color(0xFFFF0000),
                    "X (Twitter)" to Color(0xFF1DA1F2),
                    "TikTok" to Color(0xFF00F2FE)
                )

                platforms.forEach { (name, color) ->
                    Surface(
                        onClick = { shareToSocial(name) },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF19253B),
                        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Share to $name",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "▶ Post Demo",
                                color = color,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { shareToSocial("All Social Media") },
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Share Everywhere Now", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
