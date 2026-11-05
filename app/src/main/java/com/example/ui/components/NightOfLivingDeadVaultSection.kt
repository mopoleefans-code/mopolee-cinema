package com.example.ui.components

import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.CinemaColorTheme
import com.example.data.Film
import java.util.Locale

@Composable
fun NightOfLivingDeadVaultSection(
    films: List<Film>,
    onSelectFilm: (Film) -> Unit,
    onPlayFilm: (Film) -> Unit,
    currentColorTheme: CinemaColorTheme = CinemaColorTheme.ROYAL_GOLD,
    ambientGlow: Boolean = true,
    onOpenInsideSettings: () -> Unit = {},
    isVipMember: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val notldFilms = remember(films) {
        films.filter { it.seriesTitle == "Night of the Living Dead" || it.title.contains("Night of the Living Dead", ignoreCase = true) }
            .sortedBy { it.episodeNumber ?: 99 }
    }

    var selectedTab by remember { mutableStateOf("All 32 Episodes") }
    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }
    var isSpeaking by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInstance?.language = Locale.US
            }
        }
        ttsInstance = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    fun speak(text: String) {
        ttsInstance?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "notld_speak")
        isSpeaking = true
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "💀 NIGHT OF THE LIVING DEAD",
                        color = currentColorTheme.primaryColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = Color(0xFFFF334B),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "EPISODES 1 TO 32",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Classic 1968 Horror Remastered • Instant Local Play",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }

            IconButton(
                onClick = {
                    speak("Night of the Living Dead: complete series episodes 1 through 32 remastered in high definition.")
                },
                modifier = Modifier
                    .size(36.dp)
                    .background(currentColorTheme.surfaceColor, CircleShape)
            ) {
                Icon(
                    Icons.Default.VolumeUp,
                    contentDescription = "Speak Info",
                    tint = currentColorTheme.primaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Carousel of Episodes
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notldFilms, key = { it.id }) { ep ->
                NightOfLivingDeadEpisodeCard(
                    film = ep,
                    theme = currentColorTheme,
                    isVipMember = isVipMember,
                    onClick = { onSelectFilm(ep) },
                    onPlay = { onPlayFilm(ep) }
                )
            }
        }
    }
}

@Composable
fun NightOfLivingDeadEpisodeCard(
    film: Film,
    theme: CinemaColorTheme,
    isVipMember: Boolean = false,
    onClick: () -> Unit,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val epNum = film.episodeNumber ?: 1
    val isLocked = !isVipMember && epNum > 2

    Card(
        modifier = modifier
            .width(200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceColor),
        border = BorderStroke(1.dp, if (isLocked) Color(0xFFFFB300).copy(alpha = 0.5f) else theme.cardBorderColor)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(125.dp)
            ) {
                AsyncImage(
                    model = if (film.posterUrl.isNotBlank()) film.posterUrl else film.backdropUrl,
                    contentDescription = film.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark vignette
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                )

                // Episode Number Badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    color = Color.Black.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isLocked) Color(0xFFFFB300) else theme.primaryColor)
                ) {
                    Text(
                        text = if (isLocked) "🔒 EP $epNum • VIP" else if (epNum <= 2) "FREE • EP $epNum" else "EP $epNum OF 32",
                        color = if (isLocked) Color(0xFFFFB300) else theme.primaryColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Play / Lock Button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (isLocked) Color(0xFFFFB300) else theme.primaryColor)
                        .clickable { onPlay() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isLocked) Icons.Default.Lock else Icons.Default.PlayArrow,
                        contentDescription = if (isLocked) "Unlock VIP" else "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = film.title.replace("Night of the Living Dead: ", ""),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = film.synopsis,
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 10.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
