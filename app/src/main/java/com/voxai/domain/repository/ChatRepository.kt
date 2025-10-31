package com.voxai.domain.repository

import com.voxai.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getAllMessages(): Flow<List<ChatMessage>>
    suspend fun sendMessage(message: String): Result<ChatMessage>
    suspend fun clearAllMessages()
    suspend fun deleteMessage(messageId: String)
    suspend fun saveMessage(message: ChatMessage)
}
