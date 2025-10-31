package com.voxai.util.audio

import com.voxai.domain.model.VoiceEffect
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

class VoiceEffectProcessor {

    fun applyEffect(audioData: ByteArray, effect: VoiceEffect): ByteArray {
        if (effect == VoiceEffect.NONE) {
            return audioData
        }

        val samples = bytesToShorts(audioData)
        val processed = when (effect) {
            VoiceEffect.ROBOT -> applyRobotEffect(samples)
            else -> {
                val pitchShifted = applyPitchShift(samples, effect.pitchShift)
                applySpeedChange(pitchShifted, effect.speedMultiplier)
            }
        }

        return shortsToBytes(processed)
    }

    private fun applyPitchShift(samples: ShortArray, pitchShift: Float): ShortArray {
        if (pitchShift == 1.0f) return samples

        val outputSize = (samples.size / pitchShift).roundToInt()
        val output = ShortArray(outputSize)

        for (i in output.indices) {
            val srcIndex = (i * pitchShift).toInt()
            if (srcIndex < samples.size - 1) {
                val fraction = (i * pitchShift) - srcIndex
                output[i] = ((1 - fraction) * samples[srcIndex] + 
                            fraction * samples[srcIndex + 1]).toInt().toShort()
            } else if (srcIndex < samples.size) {
                output[i] = samples[srcIndex]
            }
        }

        return output
    }

    private fun applySpeedChange(samples: ShortArray, speedMultiplier: Float): ShortArray {
        if (speedMultiplier == 1.0f) return samples

        val outputSize = (samples.size / speedMultiplier).roundToInt()
        val output = ShortArray(outputSize)

        for (i in output.indices) {
            val srcIndex = (i * speedMultiplier).toInt()
            if (srcIndex < samples.size - 1) {
                val fraction = (i * speedMultiplier) - srcIndex
                output[i] = ((1 - fraction) * samples[srcIndex] + 
                            fraction * samples[srcIndex + 1]).toInt().toShort()
            } else if (srcIndex < samples.size) {
                output[i] = samples[srcIndex]
            }
        }

        return output
    }

    private fun applyRobotEffect(samples: ShortArray): ShortArray {
        val output = ShortArray(samples.size)
        val modulationFreq = 30.0
        val sampleRate = 44100.0

        for (i in samples.indices) {
            val modulation = sin(2.0 * Math.PI * modulationFreq * i / sampleRate)
            val modulatedSample = samples[i] * (0.5 + 0.5 * modulation)
            output[i] = modulatedSample.roundToInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
        }

        return output
    }

    private fun bytesToShorts(bytes: ByteArray): ShortArray {
        val shorts = ShortArray(bytes.size / 2)
        for (i in shorts.indices) {
            val b1 = bytes[i * 2].toInt() and 0xFF
            val b2 = bytes[i * 2 + 1].toInt() and 0xFF
            shorts[i] = ((b2 shl 8) or b1).toShort()
        }
        return shorts
    }

    private fun shortsToBytes(shorts: ShortArray): ByteArray {
        val bytes = ByteArray(shorts.size * 2)
        for (i in shorts.indices) {
            bytes[i * 2] = (shorts[i].toInt() and 0xFF).toByte()
            bytes[i * 2 + 1] = ((shorts[i].toInt() shr 8) and 0xFF).toByte()
        }
        return bytes
    }

    fun calculateDuration(audioData: ByteArray, sampleRate: Int): Long {
        val samples = audioData.size / 2
        return (samples * 1000L) / sampleRate
    }
}
