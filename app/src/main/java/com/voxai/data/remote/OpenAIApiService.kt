package com.voxai.data.remote

import com.voxai.data.remote.dto.OpenAIRequest
import com.voxai.data.remote.dto.OpenAIResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface OpenAIApiService {
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun chatCompletion(@Body request: OpenAIRequest): OpenAIResponse
}
