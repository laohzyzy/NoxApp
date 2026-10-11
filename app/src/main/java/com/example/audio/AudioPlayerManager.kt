package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.ToneGenerator
import android.media.AudioManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AudioPlayerManager {

    /**
     * Plays a pleasant WhatsApp-style audio note sound through the speaker
     */
    suspend fun playSynthesizedAudioNote(durationSec: Int, onProgress: (Float) -> Unit) = withContext(Dispatchers.Default) {
        val sampleRate = 44100
        val totalSamples = sampleRate * durationSec
        val buffer = ShortArray(sampleRate / 10) // 100ms chunks

        val audioTrack = try {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } catch (e: Exception) {
            null
        }

        audioTrack?.play()

        var currentSample = 0
        var phase = 0.0
        val baseFreq = 440.0 // Hz

        while (currentSample < totalSamples) {
            val progressFraction = currentSample.toFloat() / totalSamples
            onProgress(progressFraction)

            // Generate modulated voice note waveform
            for (i in buffer.indices) {
                val freq = baseFreq + 80.0 * Math.sin(2.0 * Math.PI * (currentSample + i) / (sampleRate * 0.4))
                phase += 2.0 * Math.PI * freq / sampleRate
                val amp = 0.3 * Math.sin(phase) * (0.8 + 0.2 * Math.sin(2.0 * Math.PI * (currentSample + i) / (sampleRate * 0.1)))
                buffer[i] = (amp * Short.MAX_VALUE).toInt().toShort()
            }

            audioTrack?.write(buffer, 0, buffer.size)
            currentSample += buffer.size
        }

        onProgress(1f)
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // ignored
        }
    }

    /**
     * Plays WhatsApp outgoing message sent tick sound
     */
    fun playSentBeep() {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 60)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 80)
        } catch (e: Exception) {
            // ignored
        }
    }

    /**
     * Plays SMS incoming notification chime
     */
    fun playSmsNotificationChime() {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
            toneGen.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 180)
        } catch (e: Exception) {
            // ignored
        }
    }
}
