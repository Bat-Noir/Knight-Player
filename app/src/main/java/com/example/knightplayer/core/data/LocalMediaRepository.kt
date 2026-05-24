package com.example.knightplayer.core.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.example.knightplayer.core.models.MediaCollection
import com.example.knightplayer.core.models.MediaEntry
import com.example.knightplayer.core.models.MediaSourceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository responsible for scanning the device for local video files.
 * Optimized for background execution.
 */
object LocalMediaRepository {

    suspend fun getAllLocalMedia(context: Context): List<MediaCollection> = withContext(Dispatchers.IO) {
        val folderMap = mutableMapOf<String, MutableList<MediaEntry>>()

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATA // Used to get the absolute file path
        )

        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val cursor = context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            "${MediaStore.Video.Media.DATE_ADDED} DESC"
        )

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val folderCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
            val durCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val dataCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)

            while (it.moveToNext()) {
                val id = it.getLong(idCol)
                val name = it.getString(nameCol)
                val folder = it.getString(folderCol) ?: "Internal Storage"
                val duration = it.getLong(durCol)
                val path = it.getString(dataCol)
                val contentUri = ContentUris.withAppendedId(uri, id)

                val entry = MediaEntry(
                    // 🔥 THE FIX: Removed 'extension' parameter
                    id = path ?: contentUri.toString(),
                    title = name,
                    uri = contentUri,
                    duration = duration,
                    folderName = folder,
                    sourceType = MediaSourceType.LOCAL
                )

                if (!folderMap.containsKey(folder)) {
                    folderMap[folder] = mutableListOf()
                }
                folderMap[folder]?.add(entry)
            }
        }

        folderMap.map { (name, entries) ->
            MediaCollection(name = name, entries = entries)
        }.sortedBy { it.name }
    }
}
