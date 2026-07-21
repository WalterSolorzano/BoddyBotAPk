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

    fun playCelebrationSound(context: android.content.Context) {
        scope.launch {
            try {
                val db = com.aistudio.unibuddy.qywvsp.data.AppDatabase.getDatabase(context)
                val repo = com.aistudio.unibuddy.qywvsp.data.UniBuddyRepository(db)
                val soundEnabledStr = repo.getSetting("sound_effects_enabled") ?: "true"
                if (soundEnabledStr != "true") return@launch

                val sampleRate = 44100
                // Chime: Two beautiful ascending notes (e.g. C5 followed by G5)
                val note1Freq = 523.25 // C5
                val note2Freq = 783.99 // G5
                val durationMs = 300
                
                val numSamples = sampleRate * durationMs / 1000
                val bufferSize = numSamples * 2
                
                val audioTrackTemp = AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                    AudioTrack.MODE_STATIC
                )
                
                val buffer = ShortArray(numSamples)
                // Note 1
                for (i in 0 until numSamples / 2) {
                    val t = i.toDouble() / sampleRate
                    // Sine wave with fade out
                    val envelope = 1.0 - (i.toDouble() / (numSamples / 2))
                    buffer[i] = (Math.sin(2 * Math.PI * note1Freq * t) * 8000 * envelope).toInt().toShort()
                }
                // Note 2
                for (i in numSamples / 2 until numSamples) {
                    val t = (i - numSamples / 2).toDouble() / sampleRate
                    val envelope = 1.0 - ((i - numSamples / 2).toDouble() / (numSamples / 2))
                    buffer[i] = (Math.sin(2 * Math.PI * note2Freq * t) * 10000 * envelope).toInt().toShort()
                }
                
                audioTrackTemp.write(buffer, 0, buffer.size)
                audioTrackTemp.play()
                delay(durationMs.toLong() + 50)
                audioTrackTemp.release()
            } catch (e: Exception) {}
        }
    }
}
