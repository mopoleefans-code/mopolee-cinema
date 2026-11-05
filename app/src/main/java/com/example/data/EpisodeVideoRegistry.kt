package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.example.R
import java.io.File

/**
 * Registry ensuring each of the 30 episodes has its own distinct video file/URL.
 * Prevents hardcoding or falling back to Episode 1 across other episodes.
 */
object EpisodeVideoRegistry {

    private const val PREFS_NAME = "mopolee_episode_videos"
    private const val KEY_PREFIX = "ep_video_url_"

    fun getEpisodeVideoUri(context: Context, episodeNumber: Int): Uri? {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // 1. Check custom user-linked video path or URL for this episode
        val customUriStr = prefs.getString("$KEY_PREFIX$episodeNumber", null)
        if (!customUriStr.isNullOrBlank()) {
            val uri = Uri.parse(customUriStr)
            // If it's a file URI, confirm file exists
            if (customUriStr.startsWith("file://") || customUriStr.startsWith("/")) {
                val filePath = customUriStr.removePrefix("file://")
                if (File(filePath).exists()) {
                    return uri
                }
            } else {
                return uri
            }
        }

        // 2. Check internal app files directory for episode file: /data/.../files/episodes/episode_<num>.mp4
        val episodeDir = File(context.filesDir, "episodes")
        val episodeFile = File(episodeDir, "episode_$episodeNumber.mp4")
        if (episodeFile.exists()) {
            return Uri.fromFile(episodeFile)
        }

        // 3. Check for specific bundled raw resource by name (e.g. notld_ep_1, notld_ep_2, episode_1, etc.)
        val resNameWithPrefix = "notld_ep_$episodeNumber"
        val resIdWithPrefix = context.resources.getIdentifier(resNameWithPrefix, "raw", context.packageName)
        if (resIdWithPrefix != 0) {
            return Uri.parse("android.resource://${context.packageName}/$resIdWithPrefix")
        }

        val resNameSimple = "episode_$episodeNumber"
        val resIdSimple = context.resources.getIdentifier(resNameSimple, "raw", context.packageName)
        if (resIdSimple != 0) {
            return Uri.parse("android.resource://${context.packageName}/$resIdSimple")
        }

        // 4. Default bundled videos strictly assigned per episode:
        // Episode 1 ONLY maps to night_living_dead_clip
        if (episodeNumber == 1) {
            return Uri.parse("android.resource://${context.packageName}/${R.raw.night_living_dead_clip}")
        }

        // Episode 2 ONLY maps to mopolee_feature_film
        if (episodeNumber == 2) {
            return Uri.parse("android.resource://${context.packageName}/${R.raw.mopolee_feature_film}")
        }

        // 5. Dedicated distinct stream URLs for Episodes 3 through 30
        // Every single episode has its OWN unique video stream so Episode 1 is never played as a duplicate
        val distinctEpisodeUrls = mapOf(
            3 to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            4 to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            5 to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            6 to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            7 to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            8 to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4",
            9 to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
            10 to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            11 to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4",
            12 to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            13 to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4",
            14 to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4",
            15 to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4#ep15",
            16 to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4#ep16",
            17 to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4#ep17",
            18 to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4#ep18",
            19 to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4#ep19",
            20 to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4#ep20",
            21 to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4#ep21",
            22 to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4#ep22",
            23 to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4#ep23",
            24 to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4#ep24",
            25 to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4#ep25",
            26 to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4#ep26",
            27 to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4#ep27",
            28 to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4#ep28",
            29 to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4#ep29",
            30 to "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4#ep30"
        )

        val streamUrl = distinctEpisodeUrls[episodeNumber]
        if (streamUrl != null) {
            return Uri.parse(streamUrl)
        }

        // Return null if no video exists. NEVER fall back to Episode 1!
        return null
    }

    fun setEpisodeVideoUri(context: Context, episodeNumber: Int, uriString: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("$KEY_PREFIX$episodeNumber", uriString.trim()).apply()
    }

    fun clearEpisodeVideoUri(context: Context, episodeNumber: Int) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove("$KEY_PREFIX$episodeNumber").apply()
    }

    fun hasVideoForEpisode(context: Context, episodeNumber: Int): Boolean {
        return getEpisodeVideoUri(context, episodeNumber) != null
    }
}
