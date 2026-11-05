package com.example.data

data class Film(
    val id: String,
    val title: String,
    val tagline: String = "",
    val synopsis: String,
    val videoUrl: String,
    val fallbackUrl: String = "",
    val posterUrl: String = "",
    val backdropUrl: String = "",
    val durationMinutes: Int = 10,
    val releaseYear: Int = 2024,
    val genre: String = "Cinema",
    val director: String = "George A. Romero",
    val rating: Float = 8.5f,
    val isUserUpload: Boolean = false,
    val isFavorite: Boolean = false,
    val fileSizeFormatted: String = "",
    val uploadedAt: Long = System.currentTimeMillis(),
    val seriesTitle: String? = null,
    val episodeNumber: Int? = null
)
