package com.example.knightplayer

data class WatchHistory(
    val videoId: String, // 🔥 Upgraded to String for bulletproof duplicate tracking
    val title: String,
    val uri: String,
    val position: Long,
    val duration: Long,
    val timestamp: Long
)
