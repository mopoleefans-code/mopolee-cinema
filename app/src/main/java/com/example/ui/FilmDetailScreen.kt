package com.example.ui

import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.CinemaColorTheme
import com.example.data.Film
import com.example.data.MovieAspectRatio
import com.example.ui.components.VideoPlayerView
import com.example.ui.theme.CinemaGold
import java.util.Locale

@Composable
fun FilmDetailScreen(
    film: Film,
    allFilms: List<Film>,
    onBack: () -> Unit,
    onToggleFavorite: (Film) -> Unit,
    onSelectFilm: (Film) -> Unit,
    onDeleteFilm: (Film) -> Unit,
    aspectRatio: MovieAspectRatio = MovieAspectRatio.RATIO_9_16,
    onAspectRatioChanged: (MovieAspectRatio) -> Unit = {},
    isBigScreenMode: Boolean = true,
    currentColorTheme: CinemaColorTheme = CinemaColorTheme.ROYAL_GOLD,
    ambientGlowEnabled: Boolean = true,
    autoTalkAloud: Boolean = true,
    voiceSpeed: Float = 1.0f,
    onOpenInsideSettings: () -> Unit = {},
    isVipMember: Boolean = false,
    onRequestPaystack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler { onBack() }

    var isFullscreen by remember { mutableStateOf(false) }
    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }
    var isSpeaking by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInstance?.language = Locale.US
                ttsInstance?.setSpeechRate(voiceSpeed)
            }
        }
        ttsInstance = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    fun speak(text: String) {
        val tts = ttsInstance ?: return
        if (isSpeaking) {
            tts.stop()
            isSpeaking = false
        } else {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "film_detail_speak")
            isSpeaking = true
            Toast.makeText(context, "Speaking: $text", Toast.LENGTH_SHORT).show()
        }
    }

    val episodes = remember(allFilms, film) {
        if (film.seriesTitle != null) {
            allFilms.filter { it.seriesTitle == film.seriesTitle }
                .sortedBy { it.episodeNumber ?: 99 }
        } else {
            allFilms.filter { it.id != film.id }
        }
    }

    if (isFullscreen) {
        // Fullscreen Player
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            VideoPlayerView(
                videoUrl = film.videoUrl,
                title = film.title,
                fallbackUrl = film.fallbackUrl,
                isFullscreen = true,
                aspectRatio = aspectRatio,
                onAspectRatioChanged = onAspectRatioChanged,
                ambientGlowEnabled = false,
                themeColor = currentColorTheme.primaryColor,
                autoTalkAloud = autoTalkAloud,
                voiceSpeed = voiceSpeed,
                onOpenSettings = onOpenInsideSettings,
                onToggleFullscreen = { isFullscreen = false },
                onClosePlayer = { isFullscreen = false },
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(currentColorTheme.backgroundColor)
        ) {
            // Video Player at top
            VideoPlayerView(
                videoUrl = film.videoUrl,
                title = film.title,
                fallbackUrl = film.fallbackUrl,
                isFullscreen = false,
                aspectRatio = aspectRatio,
                onAspectRatioChanged = onAspectRatioChanged,
                ambientGlowEnabled = ambientGlowEnabled,
                themeColor = currentColorTheme.primaryColor,
                autoTalkAloud = autoTalkAloud,
                voiceSpeed = voiceSpeed,
                onOpenSettings = onOpenInsideSettings,
                onToggleFullscreen = { isFullscreen = true },
                onClosePlayer = onBack,
                modifier = Modifier.fillMaxWidth()
            )

            val isEpisodeLocked = !isVipMember && !film.isUserUpload && (film.episodeNumber ?: 0) > 2
            if (isEpisodeLocked) {
                Surface(
                    color = Color(0xFFFFB300).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFFFFB300)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFFB300))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "🔒 VIP PASS REQUIRED",
                                    color = Color(0xFFFFB300),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Pay ₦2,500 with Paystack to unlock all 32 episodes",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Button(
                            onClick = onRequestPaystack,
                            colors = ButtonDefaults.buttonColors(containerColor = currentColorTheme.primaryColor),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Unlock", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Scrollable Details & Episode List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title and Favorite Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = film.title,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                            if (film.tagline.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = film.tagline,
                                    color = currentColorTheme.primaryColor,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onOpenInsideSettings) {
                                Icon(Icons.Default.Tune, contentDescription = "Settings", tint = currentColorTheme.primaryColor)
                            }
                            IconButton(onClick = { onToggleFavorite(film) }) {
                                Icon(
                                    if (film.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Favorite",
                                    tint = if (film.isFavorite) currentColorTheme.primaryColor else Color.White
                                )
                            }
                        }
                    }
                }

                // Tags: Big / Small Screen, Genre, Year, Rating
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Big / Small Sizing Chip
                        val isBig = aspectRatio == MovieAspectRatio.RATIO_9_16
                        Surface(
                            onClick = {
                                val next = if (isBig) MovieAspectRatio.RATIO_16_9 else MovieAspectRatio.RATIO_9_16
                                onAspectRatioChanged(next)
                                Toast.makeText(context, "Movie Screen: ${if (isBig) "SMALL (16:9)" else "BIG (9:16)"}", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = currentColorTheme.primaryColor,
                            border = BorderStroke(1.dp, currentColorTheme.primaryColor)
                        ) {
                            Text(
                                text = if (isBig) "📱 9:16 Reel (Big)" else "🎬 16:9 Wide (Small)",
                                color = Color.Black,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = currentColorTheme.surfaceColor,
                            border = BorderStroke(1.dp, currentColorTheme.cardBorderColor)
                        ) {
                            Text(
                                text = film.genre,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = currentColorTheme.surfaceColor,
                            border = BorderStroke(1.dp, currentColorTheme.cardBorderColor)
                        ) {
                            Text(
                                text = "${film.releaseYear}",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = currentColorTheme.primaryColor, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${film.rating}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Action Bar (Talk Out Loud, Theater Big/Small, Settings)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Talk Out Loud
                        Button(
                            onClick = { speak("${film.title}. ${film.synopsis}") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSpeaking) currentColorTheme.secondaryColor else Color(0xFF00E676)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSpeaking) "Stop Voice" else "Talk Out Loud",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        // Big / Small Sizing Button
                        Button(
                            onClick = {
                                val next = if (aspectRatio == MovieAspectRatio.RATIO_9_16) MovieAspectRatio.RATIO_16_9 else MovieAspectRatio.RATIO_9_16
                                onAspectRatioChanged(next)
                                Toast.makeText(context, "Movie Screen: ${if (next == MovieAspectRatio.RATIO_9_16) "BIG (9:16)" else "SMALL (16:9)"}", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = currentColorTheme.primaryColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Fullscreen,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (aspectRatio == MovieAspectRatio.RATIO_9_16) "Small (16:9)" else "Big (9:16)",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        // Inside Settings
                        OutlinedButton(
                            onClick = onOpenInsideSettings,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, currentColorTheme.primaryColor),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = currentColorTheme.primaryColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Settings", fontSize = 11.sp)
                        }
                    }
                }

                // Synopsis
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = currentColorTheme.surfaceColor),
                        border = BorderStroke(1.dp, currentColorTheme.cardBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "STORY SYNOPSIS",
                                color = currentColorTheme.primaryColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = film.synopsis,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }

                // All 32 Episodes Section
                item {
                    Text(
                        text = "💀 ALL 32 EPISODES • SELECT & WATCH",
                        color = currentColorTheme.primaryColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }

                items(episodes, key = { it.id }) { ep ->
                    val isCurrentPlaying = ep.id == film.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectFilm(ep)
                                Toast.makeText(context, "Playing ${ep.title}", Toast.LENGTH_SHORT).show()
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrentPlaying) currentColorTheme.primaryColor.copy(alpha = 0.15f) else currentColorTheme.surfaceColor
                        ),
                        border = BorderStroke(
                            if (isCurrentPlaying) 1.5.dp else 1.dp,
                            if (isCurrentPlaying) currentColorTheme.primaryColor else currentColorTheme.cardBorderColor
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isCurrentPlaying) currentColorTheme.primaryColor else Color(0xFF1E2D48)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = if (isCurrentPlaying) Color.Black else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ep.title,
                                    color = if (isCurrentPlaying) currentColorTheme.primaryColor else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = ep.synopsis,
                                    color = Color.White.copy(alpha = 0.65f),
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (isCurrentPlaying) {
                                Surface(
                                    color = currentColorTheme.primaryColor,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "PLAYING",
                                        color = Color.Black,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}
