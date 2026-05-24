package com.example.knightplayer.core.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ThumbnailCache {

    private val memoryCache = LruCache<String, Bitmap>(80)

    // 🔥 THE FIX: suspend + Dispatchers.IO moves this off the main thread so the app never lags!
    suspend fun getThumbnail(context: Context, uri: String): Bitmap? = withContext(Dispatchers.IO) {

        // 1️⃣ MEMORY CACHE (Instant)
        val cached = memoryCache.get(uri)
        if (cached != null) return@withContext cached

        // 2️⃣ DISK CACHE (Fast)
        val file = getPermanentCacheFile(context, uri)
        val diskBitmap = loadBitmap(file)
        if (diskBitmap != null) {
            memoryCache.put(uri, diskBitmap)
            return@withContext diskBitmap
        }

        // 3️⃣ GENERATE (Heavy - only happens once per video!)
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, Uri.parse(uri))

            val frame = getBrightFrame(retriever)

            retriever.release()

            if (frame != null) {
                memoryCache.put(uri, frame)
                saveBitmap(file, frame) // 🔥 SAVE PERMANENTLY AS JPG
            }

            return@withContext frame

        } catch (e: Exception) {
            return@withContext null
        }
    }

    ////////////////////////////////////////////////////////
    // 🎯 SMART FRAME (middle of video = best result)
    ////////////////////////////////////////////////////////

    private fun getBrightFrame(retriever: MediaMetadataRetriever): Bitmap? {
        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val durationMs = durationStr?.toLongOrNull() ?: 0L
        val middle = durationMs * 500 // middle of video in microseconds

        return retriever.getFrameAtTime(middle, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
    }

    ////////////////////////////////////////////////////////
    // 💾 PERMANENT DISK CACHE HELPERS
    ////////////////////////////////////////////////////////

    private fun getPermanentCacheFile(context: Context, uri: String): File {
        // 🔥 THE FIX: Use filesDir instead of cacheDir so the OS doesn't randomly delete them!
        val thumbDir = File(context.filesDir, "thumbnails")
        if (!thumbDir.exists()) {
            thumbDir.mkdirs()
        }
        val fileName = "${uri.hashCode()}.jpg"
        return File(thumbDir, fileName)
    }

    private fun saveBitmap(file: File, bitmap: Bitmap) {
        try {
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out) // 85 is a great quality/size ratio
            out.flush()
            out.close()
        } catch (_: Exception) {}
    }

    private fun loadBitmap(file: File): Bitmap? {
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else null
    }
}