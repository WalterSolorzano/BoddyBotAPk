package com.aistudio.unibuddy.qywvsp.ui

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.*
import java.util.Random

object AmbientAudioEngine {
    private var audioTrack: AudioTrack? = null
    private var playJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    private var isPlaying = false

    fun playSoundscape(type: String) {
        stop()
        isPlaying = true

        val sampleRate = 44100
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
            AudioTrack.MODE_STREAM
        )

        audioTrack?.play()

        playJob = scope.launch {
            val buffer = ShortArray(bufferSize)
            val random = Random()
            var lastOut = 0.0
            
            while (isActive && isPlaying) {
                for (i in buffer.indices) {
                    when (type) {
                        "rain" -> {
                            // Simple pink/brown noise for rain
                            val white = random.nextGaussian()
                            lastOut = (lastOut + (0.02 * white)) / 1.02
                            buffer[i] = (lastOut * 4000).toInt().toShort()
                        }
                        "space" -> {
                            // Deep brown noise
                            val white = random.nextGaussian()
                            lastOut = (lastOut + (0.01 * white)) / 1.01
                            buffer[i] = (lastOut * 8000).toInt().toShort()
                        }
                        else -> {
                            // White noise for library/cafe
                            val white = random.nextGaussian()
                            lastOut = (lastOut + (0.05 * white)) / 1.05
                            buffer[i] = (lastOut * 3000).toInt().toShort()
                        }
                    }
                }
                audioTrack?.write(buffer, 0, buffer.size)
            }
        }
    }

    fun stop() {
        isPlaying = false
        playJob?.cancel()
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {}
        audioTrack = null
    }
}
