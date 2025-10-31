package com.voxai.presentation.aichat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voxai.domain.model.ChatMessage
import com.voxai.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIChatUiState())
    val uiState: StateFlow<AIChatUiState> = _uiState.asStateFlow()

    init {
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            chatRepository.getAllMessages()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        error = "加载消息失败: ${e.message}"
                    )
                }
                .collect { messages ->
                    _uiState.value = _uiState.value.copy(
                        messages = messages,
                        isLoading = false
                    )
                }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        if (_uiState.value.isSending) return

        _uiState.value = _uiState.value.copy(
            isSending = true,
            error = null
        )

        viewModelScope.launch {
            chatRepository.sendMessage(content)
                .onSuccess { aiMessage ->
                    _uiState.value = _uiState.value.copy(
                        isSending = false
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        error = "发送失败: ${e.message}"
                    )
                }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            chatRepository.clearAllMessages()
            _uiState.value = _uiState.value.copy(
                messages = emptyList()
            )
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            chatRepository.deleteMessage(messageId)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class AIChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val error: String? = null
)
