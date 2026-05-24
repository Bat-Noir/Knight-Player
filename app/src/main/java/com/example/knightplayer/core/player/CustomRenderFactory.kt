package com.example.knightplayer.core.player

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import androidx.media3.exoplayer.mediacodec.MediaCodecUtil

@UnstableApi
class CustomRenderersFactory(
    context: Context,
    private val audioSyncProcessor: AudioSyncProcessor,
    private val forceSoftwareDecoder: Boolean,
    private val audioPassthrough: Boolean
) : DefaultRenderersFactory(context) {

    init {
        setMediaCodecSelector { mimeType, requiresSecureDecoder, requiresTunnelingProvider ->
            val decoders = MediaCodecUtil.getDecoderInfos(mimeType, requiresSecureDecoder, requiresTunnelingProvider)
            if (forceSoftwareDecoder) {
                decoders.sortedBy { it.hardwareAccelerated }.toMutableList()
            } else {
                decoders
            }
        }

        // 🔥 REMOVE OR COMMENT OUT THIS LINE
        // setEnableAudioOffload(audioPassthrough)
    }

    override fun buildAudioSink(
        context: Context,
        enableFloatOutput: Boolean,
        enableAudioTrackPlaybackParams: Boolean
    ): AudioSink {
        // 🔥 BYPASS TEST: We are removing the audioSyncProcessor from the chain
        return DefaultAudioSink.Builder(context)
            .setAudioProcessors(emptyArray()) // Empty processor chain
            .setEnableFloatOutput(false)
            .build()
    }
}

