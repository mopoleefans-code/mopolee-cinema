package com.example.data

enum class MovieAspectRatio(
    val label: String,
    val shortLabel: String,
    val ratioWidth: Float,
    val ratioHeight: Float,
    val compactHeightDp: Int,
    val bigHeightDp: Int
) {
    RATIO_9_16(
        label = "9:16 Full Reel (Tall Big Screen)",
        shortLabel = "9:16 Reel",
        ratioWidth = 9f,
        ratioHeight = 16f,
        compactHeightDp = 420,
        bigHeightDp = 560
    ),
    RATIO_3_4(
        label = "3:4 Classic IMAX (Big Screen)",
        shortLabel = "3:4 Big",
        ratioWidth = 3f,
        ratioHeight = 4f,
        compactHeightDp = 340,
        bigHeightDp = 460
    ),
    RATIO_16_9(
        label = "16:9 Standard Cinema (Small Screen)",
        shortLabel = "16:9 Wide",
        ratioWidth = 16f,
        ratioHeight = 9f,
        compactHeightDp = 240,
        bigHeightDp = 320
    ),
    RATIO_4_3(
        label = "4:3 Vintage Retro TV (Small Screen)",
        shortLabel = "4:3 Retro",
        ratioWidth = 4f,
        ratioHeight = 3f,
        compactHeightDp = 270,
        bigHeightDp = 360
    ),
    RATIO_FILL(
        label = "Fill Screen (Maximum Cinema)",
        shortLabel = "Fill Screen",
        ratioWidth = 0f,
        ratioHeight = 0f,
        compactHeightDp = 380,
        bigHeightDp = 520
    );

    val isBigScreen: Boolean
        get() = this == RATIO_9_16 || this == RATIO_3_4 || this == RATIO_FILL

    val isSmallScreen: Boolean
        get() = this == RATIO_16_9 || this == RATIO_4_3
}
