package com.voxai.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OpenAIRequest(
    @SerializedName("model")
    val model: String = "gpt-3.5-turbo",
    @SerializedName("messages")
    val messages: List<Message>,
    @SerializedName("temperature")
    val temperature: Float = 0.7f,
    @SerializedName("max_tokens")
    val maxTokens: Int = 1000
)

data class Message(
    @SerializedName("role")
    val role: String,
    @SerializedName("content")
    val content: String
)

data class OpenAIResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("choices")
    val choices: List<Choice>,
    @SerializedName("created")
    val created: Long
)

data class Choice(
    @SerializedName("message")
    val message: Message,
    @SerializedName("finish_reason")
    val finishReason: String,
    @SerializedName("index")
    val index: Int
)
