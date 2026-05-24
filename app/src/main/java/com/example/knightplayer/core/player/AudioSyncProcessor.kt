package com.example.knightplayer.core.player

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.AudioProcessor.AudioFormat
import androidx.media3.common.util.UnstableApi
import java.nio.ByteBuffer
import java.nio.ByteOrder

@UnstableApi
class AudioSyncProcessor : AudioProcessor {

    private var pendingOffsetMs: Long = 0L
    private var currentOffsetMs: Long = 0L
    private var inputFormat = AudioFormat.NOT_SET

    private var buffer: ByteBuffer = AudioProcessor.EMPTY_BUFFER
    private var bytesToSkip = 0
    private var bytesToInsert = 0
    private var inputEnded = false

    fun setSyncOffset(offsetMs: Long) {
        pendingOffsetMs = offsetMs
    }

    override fun configure(inputAudioFormat: AudioFormat): AudioFormat {
        // 🔥 THE FIX: Do not throw exception.
        // If it's not 16-bit PCM, return NOT_SET to bypass this processor safely.
        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
            this.inputFormat = AudioFormat.NOT_SET
            return AudioFormat.NOT_SET
        }

        this.inputFormat = inputAudioFormat
        return inputAudioFormat
    }

    override fun isActive(): Boolean = inputFormat != AudioFormat.NOT_SET

    override fun queueInput(inputBuffer: ByteBuffer) {
        if (inputFormat == AudioFormat.NOT_SET) {
            // Should not happen if isActive is checked, but safe guard
            return
        }

        if (currentOffsetMs != pendingOffsetMs) {
            currentOffsetMs = pendingOffsetMs
            val bytesPerFrame = inputFormat.channelCount * 2
            val bytesPerMs = (inputFormat.sampleRate * bytesPerFrame) / 1000

            bytesToInsert = (currentOffsetMs * bytesPerMs).toInt().coerceAtLeast(0)
            bytesToSkip = (kotlin.math.abs(currentOffsetMs) * bytesPerMs).toInt().coerceAtLeast(0)
        }

        val remaining = inputBuffer.remaining()

        // Handle Skip
        if (bytesToSkip > 0) {
            val skipAmount = kotlin.math.min(bytesToSkip, remaining)
            inputBuffer.position(inputBuffer.position() + skipAmount)
            bytesToSkip -= skipAmount
            if (inputBuffer.remaining() == 0) return
        }

        // Handle Insert (Silence)
        val frameSize = inputFormat.channelCount * 2
        val spaceNeeded = inputBuffer.remaining() + bytesToInsert

        if (buffer.capacity() < spaceNeeded) {
            buffer = ByteBuffer.allocateDirect(spaceNeeded).order(ByteOrder.nativeOrder())
        } else {
            buffer.clear()
        }

        if (bytesToInsert > 0) {
            val silence = ByteArray(bytesToInsert) // Initialized to 0
            buffer.put(silence)
            bytesToInsert = 0
        }

        buffer.put(inputBuffer)
        buffer.flip()
    }

    override fun getOutput(): ByteBuffer {
        val output = buffer
        buffer = AudioProcessor.EMPTY_BUFFER
        return output
    }

    override fun queueEndOfStream() { inputEnded = true }
    override fun isEnded(): Boolean = inputEnded && !buffer.hasRemaining()
    override fun flush() { buffer = AudioProcessor.EMPTY_BUFFER; bytesToSkip = 0; bytesToInsert = 0 }
    override fun reset() { flush(); inputFormat = AudioFormat.NOT_SET }
}
