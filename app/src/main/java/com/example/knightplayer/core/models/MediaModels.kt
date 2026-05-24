package com.example.knightplayer.core.models

import android.net.Uri

/**
 * The source type for media content.
 * LOCAL: Files stored on the device.
 * NETWORK: Online streams (mp4, m3u8).
 */
enum class MediaSourceType {
    LOCAL,
    NETWORK
}

/**
 * The master model for all playable content.
 */
data class MediaEntry(
    val id: String,
    val title: String,
    val uri: Uri,
    val duration: Long = 0L,
    val posterUri: Uri? = null,
    val sourceType: MediaSourceType = MediaSourceType.LOCAL,
    val folderName: String = "Unknown"
)

/**
 * Model for the "Continue Watching" row.
 */
data class WatchHistory(
    val videoId: String,
    val title: String,
    val uri: String,
    val duration: Long,
    val position: Long,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Unified Folder/Collection model.
 */
data class MediaCollection(
    val name: String,
    val entries: List<MediaEntry>,
    val posterUri: Uri? = entries.firstOrNull()?.uri
)
