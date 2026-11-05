package com.example.ui.components

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.FrameLayout
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.EpisodeVideoRegistry
import com.example.data.MovieAspectRatio
import kotlinx.coroutines.delay
import java.util.Locale

fun extractEpisodeNumber(title: String, url: String): Int? {
    val epPattern = Regex("(?:Ep\\.?|Episode|notld_ep_)\\s*(\\d+)", RegexOption.IGNORE_CASE)
    val match = epPattern.find(title) ?: epPattern.find(url)
    return match?.groupValues?.getOrNull(1)?.toIntOrNull()
}

/**
 * Resolves the playback URI strictly for the requested video.
 * CRITICAL: Never falls back to Episode 1 (night_living_dead_clip) when another episode is selected.
 */
fun resolveVideoUri(context: Context, url: String, title: String = ""): Uri? {
    if (url.isBlank()) return null

    val epNum = extractEpisodeNumber(title, url)

    // Check episode registry if an episode number is identified
    if (epNum != null) {
        return EpisodeVideoRegistry.getEpisodeVideoUri(context, epNum)
    }

    if (url.startsWith("episode://notld_ep_") || url.startsWith("notld_ep_")) {
        val num = url.removePrefix("episode://notld_ep_").removePrefix("notld_ep_").toIntOrNull() ?: 1
        return EpisodeVideoRegistry.getEpisodeVideoUri(context, num)
    }

    // Specific local resources
    if (url.contains("mopolee_feature_film")) {
        return Uri.parse("android.resource://${context.packageName}/${R.raw.mopolee_feature_film}")
    }
    if (url.contains("night_living_dead_clip")) {
        return Uri.parse("android.resource://${context.packageName}/${R.raw.night_living_dead_clip}")
    }

    // Direct Web, Content, or File URIs
    if (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("content://") || url.startsWith("file://")) {
        return Uri.parse(url)
    }

    return null
}

@Composable
fun VideoPlayerView(
    videoUrl: String,
    title: String,
    modifier: Modifier = Modifier,
    fallbackUrl: String = "",
    isFullscreen: Boolean = false,
    aspectRatio: MovieAspectRatio = MovieAspectRatio.RATIO_9_16,
    onAspectRatioChanged: (MovieAspectRatio) -> Unit = {},
    ambientGlowEnabled: Boolean = true,
    themeColor: Color = Color(0xFFFFB80B),
    autoTalkAloud: Boolean = true,
    voiceSpeed: Float = 1.0f,
    onOpenSettings: () -> Unit = {},
    onToggleFullscreen: () -> Unit = {},
    onClosePlayer: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentEpNum = remember(title, videoUrl) { extractEpisodeNumber(title, videoUrl) }

    var activeUrl by remember(videoUrl) { mutableStateOf(videoUrl) }

    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableIntStateOf(0) }
    var duration by remember { mutableIntStateOf(0) }
    var areControlsVisible by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var showUrlInputDialog by remember { mutableStateOf(false) }
    var customUrlInput by remember { mutableStateOf("") }

    var videoViewRef by remember { mutableStateOf<VideoView?>(null) }
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }

    // Launcher for attaching local video files to this specific episode
    val videoFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val epNumber = currentEpNum ?: 1
            EpisodeVideoRegistry.setEpisodeVideoUri(context, epNumber, uri.toString())
            activeUrl = uri.toString()
            hasError = false
            Toast.makeText(context, "Attached video file to Episode $epNumber", Toast.LENGTH_SHORT).show()
        }
    }

    // When videoUrl or activeUrl changes, resolve and play strictly that episode
    LaunchedEffect(activeUrl, videoUrl) {
        val targetUri = resolveVideoUri(context, activeUrl, title)
        if (targetUri == null) {
            hasError = true
            isPlaying = false
            videoViewRef?.stopPlayback()
        } else {
            hasError = false
            videoViewRef?.let { vv ->
                try {
                    vv.stopPlayback()
                    vv.setVideoURI(targetUri)
                    vv.start()
                    isPlaying = true
                } catch (e: Exception) {
                    hasError = true
                    isPlaying = false
                }
            }
        }
    }

    // Text to Speech
    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }
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

    // Speak episode title aloud when autoTalkAloud is on
    LaunchedEffect(title) {
        if (autoTalkAloud && title.isNotBlank()) {
            delay(400)
            val cleanTitle = title.replace("Night of the Living Dead: ", "")
            ttsInstance?.speak(cleanTitle, TextToSpeech.QUEUE_FLUSH, null, "episode_announce")
        }
    }

    // Playback progress ticker
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            videoViewRef?.let { vv ->
                if (vv.isPlaying) {
                    currentPosition = vv.currentPosition
                    duration = vv.duration
                }
            }
            delay(500)
        }
    }

    // Auto-hide controls
    LaunchedEffect(areControlsVisible) {
        if (areControlsVisible && isPlaying && !hasError) {
            delay(4000)
            areControlsVisible = false
        }
    }

    // Container with neon ambient glow
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isFullscreen) 600.dp else 240.dp)
            .background(Color.Black)
            .clickable { areControlsVisible = !areControlsVisible }
    ) {
        // Neon ambient glow backdrop
        if (ambientGlowEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                themeColor.copy(alpha = 0.25f),
                                Color.Transparent
                            ),
                            radius = 600f
                        )
                    )
            )
        }

        // Native Android VideoView
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    setOnPreparedListener { mp ->
                        mediaPlayerRef = mp
                        duration = mp.duration
                        isBuffering = false
                        hasError = false
                        mp.isLooping = true
                        start()
                        isPlaying = true
                    }
                    setOnErrorListener { _, _, _ ->
                        // STRICT FIX: Never auto-recover to Episode 1!
                        post {
                            hasError = true
                            isBuffering = false
                            isPlaying = false
                        }
                        true
                    }
                    setOnInfoListener { _, what, _ ->
                        when (what) {
                            MediaPlayer.MEDIA_INFO_BUFFERING_START -> isBuffering = true
                            MediaPlayer.MEDIA_INFO_BUFFERING_END -> isBuffering = false
                        }
                        false
                    }

                    val initialUri = resolveVideoUri(ctx, activeUrl, title)
                    if (initialUri != null) {
                        setVideoURI(initialUri)
                    } else {
                        hasError = true
                    }
                    videoViewRef = this
                }
            },
            update = { vv ->
                videoViewRef = vv
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering indicator
        if (isBuffering && !hasError) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = themeColor, strokeWidth = 3.dp)
            }
        }

        // Distinct Missing Video UI (STRICT FIX: Shown when video is missing, NEVER plays Episode 1)
        if (hasError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(20.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Missing Video",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(44.dp)
                    )

                    Text(
                        text = if (currentEpNum != null) "Episode $currentEpNum Video Missing" else "Video Not Found",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "No dedicated video file is attached to $title.\nEpisode 1 will NOT be played as a substitute.",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { videoFilePicker.launch("video/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.FileOpen, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Attach Video File", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { showUrlInputDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, themeColor)
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = themeColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Stream URL", color = themeColor, fontSize = 11.sp)
                        }
                    }

                    if (isFullscreen) {
                        OutlinedButton(
                            onClick = onToggleFullscreen,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Exit Fullscreen", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Overlay Controls
        AnimatedVisibility(
            visible = areControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.8f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = onClosePlayer) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                            currentEpNum?.let { ep ->
                                Text(
                                    text = "Episode $ep of 32 • Independent Video Stream",
                                    color = themeColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { videoFilePicker.launch("video/*") }) {
                            Icon(Icons.Default.FileOpen, contentDescription = "Attach File", tint = themeColor)
                        }
                        IconButton(onClick = onOpenSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = themeColor)
                        }
                        IconButton(onClick = onToggleFullscreen) {
                            Icon(
                                if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = "Fullscreen",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Center Play/Pause & Skip Buttons
                if (!hasError) {
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Replay 10s
                        IconButton(
                            onClick = {
                                videoViewRef?.let { vv ->
                                    val nextPos = (vv.currentPosition - 10000).coerceAtLeast(0)
                                    vv.seekTo(nextPos)
                                }
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Replay10, contentDescription = "Replay 10s", tint = Color.White)
                        }

                        // Play/Pause Center Button
                        Surface(
                            onClick = {
                                videoViewRef?.let { vv ->
                                    if (vv.isPlaying) {
                                        vv.pause()
                                        isPlaying = false
                                    } else {
                                        vv.start()
                                        isPlaying = true
                                    }
                                }
                            },
                            shape = CircleShape,
                            color = themeColor,
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.Black,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        // Forward 10s
                        IconButton(
                            onClick = {
                                videoViewRef?.let { vv ->
                                    val nextPos = (vv.currentPosition + 10000).coerceAtMost(vv.duration)
                                    vv.seekTo(nextPos)
                                }
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Forward10, contentDescription = "Forward 10s", tint = Color.White)
                        }
                    }
                }

                // Bottom Seekbar & Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Slider(
                        value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                        onValueChange = { fraction ->
                            videoViewRef?.let { vv ->
                                val targetMs = (fraction * duration).toInt()
                                vv.seekTo(targetMs)
                                currentPosition = targetMs
                            }
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = themeColor,
                            activeTrackColor = themeColor,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${formatDuration(currentPosition)} / ${formatDuration(duration)}",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    isMuted = !isMuted
                                    mediaPlayerRef?.let { mp ->
                                        if (isMuted) mp.setVolume(0f, 0f) else mp.setVolume(1f, 1f)
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                                    contentDescription = "Mute",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog for entering a custom stream URL for this episode
    if (showUrlInputDialog) {
        Dialog(onDismissRequest = { showUrlInputDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF140F24)),
                border = BorderStroke(1.dp, themeColor),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Custom Stream URL for ${if (currentEpNum != null) "Episode $currentEpNum" else title}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = customUrlInput,
                        onValueChange = { customUrlInput = it },
                        placeholder = { Text("https://example.com/video.mp4") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColor,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (customUrlInput.isNotBlank()) {
                                    val epNumber = currentEpNum ?: 1
                                    EpisodeVideoRegistry.setEpisodeVideoUri(context, epNumber, customUrlInput.trim())
                                    activeUrl = customUrlInput.trim()
                                    hasError = false
                                    showUrlInputDialog = false
                                    Toast.makeText(context, "Saved stream for Episode $epNumber", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save & Play", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { showUrlInputDialog = false },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

private fun formatDuration(millis: Int): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
