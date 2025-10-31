package com.voxai.data.repository

import com.voxai.data.local.ChatMessageDao
import com.voxai.data.local.entity.ChatMessageEntity
import com.voxai.data.remote.OpenAIApiService
import com.voxai.data.remote.dto.Message
import com.voxai.data.remote.dto.OpenAIRequest
import com.voxai.domain.model.ChatMessage
import com.voxai.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val apiService: OpenAIApiService,
    private val chatMessageDao: ChatMessageDao
) : ChatRepository {

    override fun getAllMessages(): Flow<List<ChatMessage>> {
        return chatMessageDao.getAllMessages().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun sendMessage(message: String): Result<ChatMessage> {
        return try {
            val userMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = message,
                isUser = true,
                timestamp = System.currentTimeMillis()
            )
            
            chatMessageDao.insertMessage(userMessage.toEntity())
            
            val messages = chatMessageDao.getAllMessages()
            val conversationHistory = mutableListOf<Message>()
            
            messages.map { entities ->
                entities.takeLast(10).forEach { entity ->
                    conversationHistory.add(
                        Message(
                            role = if (entity.isUser) "user" else "assistant",
                            content = entity.content
                        )
                    )
                }
            }
            
            val request = OpenAIRequest(
                messages = conversationHistory.ifEmpty {
                    listOf(Message(role = "user", content = message))
                }
            )
            
            val response = apiService.chatCompletion(request)
            
            val aiMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                content = response.choices.firstOrNull()?.message?.content ?: "抱歉，我现在无法回复。",
                isUser = false,
                timestamp = System.currentTimeMillis()
            )
            
            chatMessageDao.insertMessage(aiMessage.toEntity())
            
            Result.success(aiMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearAllMessages() {
        chatMessageDao.clearAllMessages()
    }

    override suspend fun deleteMessage(messageId: String) {
        chatMessageDao.deleteMessage(messageId)
    }

    override suspend fun saveMessage(message: ChatMessage) {
        chatMessageDao.insertMessage(message.toEntity())
    }

    private fun ChatMessageEntity.toDomainModel() = ChatMessage(
        id = id,
        content = content,
        isUser = isUser,
        timestamp = timestamp,
        hasAudio = hasAudio,
        audioData = audioData
    )

    private fun ChatMessage.toEntity() = ChatMessageEntity(
        id = id,
        content = content,
        isUser = isUser,
        timestamp = timestamp,
        hasAudio = hasAudio,
        audioData = audioData
    )
}
