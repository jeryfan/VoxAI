package com.voxai.domain.model

data class ChatMessage(
    val id: String = "",
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val hasAudio: Boolean = false,
    val audioData: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChatMessage

        if (id != other.id) return false
        if (content != other.content) return false
        if (isUser != other.isUser) return false
        if (timestamp != other.timestamp) return false
        if (hasAudio != other.hasAudio) return false
        if (audioData != null) {
            if (other.audioData == null) return false
            if (!audioData.contentEquals(other.audioData)) return false
        } else if (other.audioData != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + isUser.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + hasAudio.hashCode()
        result = 31 * result + (audioData?.contentHashCode() ?: 0)
        return result
    }
}
