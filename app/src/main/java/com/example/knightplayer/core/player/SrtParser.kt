package com.example.knightplayer.core.player

import android.content.Context
import android.net.Uri
import java.io.InputStreamReader

data class SubtitleCue(val startTime: Long, val endTime: Long, val text: String)

object SrtParser {
    fun parse(context: Context, uri: Uri): List<SubtitleCue> {
        val cues = mutableListOf<SubtitleCue>()
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = InputStreamReader(inputStream, "UTF-8").buffered()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val index = line?.trim()
                    if (index.isNullOrEmpty()) continue

                    val timeString = reader.readLine() ?: break
                    val times = timeString.split(" --> ")
                    if (times.size == 2) {
                        val startTime = parseTime(times[0])
                        val endTime = parseTime(times[1])

                        val textBuilder = StringBuilder()
                        while (reader.readLine().also { line = it } != null && line!!.isNotBlank()) {
                            textBuilder.append(line).append("\n")
                        }
                        cues.add(SubtitleCue(startTime, endTime, textBuilder.toString().trim()))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cues
    }

    private fun parseTime(timeStr: String): Long {
        try {
            val parts = timeStr.replace(",", ".").split(":")
            val h = parts[0].toLong()
            val m = parts[1].toLong()
            val sParts = parts[2].split(".")
            val s = sParts[0].toLong()
            val ms = if (sParts.size > 1) sParts[1].toLong() else 0L
            return (h * 3600000) + (m * 60000) + (s * 1000) + ms
        } catch (e: Exception) {
            return 0L
        }
    }
}
