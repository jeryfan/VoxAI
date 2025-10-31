package com.voxai.domain.model

data class AudioData(
    val id: String = "",
    val data: ByteArray,
    val sampleRate: Int,
    val durationMs: Long = 0,
    val effect: VoiceEffect = VoiceEffect.NONE,
    val timestamp: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AudioData

        if (id != other.id) return false
        if (!data.contentEquals(other.data)) return false
        if (sampleRate != other.sampleRate) return false
        if (durationMs != other.durationMs) return false
        if (effect != other.effect) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + sampleRate
        result = 31 * result + durationMs.hashCode()
        result = 31 * result + effect.hashCode()
        return result
    }
}
