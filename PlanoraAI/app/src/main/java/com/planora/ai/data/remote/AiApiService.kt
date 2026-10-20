package com.planora.ai.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Streaming

/**
 * Backend API interface for AI planning.
 * The Android app never holds the real LLM API key.
 * All requests go through your secure backend which proxies to the chosen LLM.
 *
 * Configure BASE_URL in BuildConfig or a remote config.
 */
interface AiApiService {

    @POST("v1/chat")
    suspend fun chat(
        @Header("Authorization") token: String? = null,
        @Body request: ChatRequest
    ): ChatResponse

    @Streaming
    @POST("v1/chat/stream")
    suspend fun chatStream(
        @Header("Authorization") token: String? = null,
        @Body request: ChatRequest
    ): okhttp3.ResponseBody
}

@JsonClass(generateAdapter = true)
data class ChatRequest(
    val messages: List<ChatMessageDto>,
    val conversationId: String? = null,
    val language: String = "fa",
    val userPreferences: UserPrefsDto? = null,
    val existingTasksSummary: String? = null
)

@JsonClass(generateAdapter = true)
data class ChatMessageDto(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class UserPrefsDto(
    val preferredStartHour: Int = 8,
    val preferredEndHour: Int = 22,
    val dailyWorkloadLimitHours: Float = 8f,
    val peakEnergyTime: String = "morning",
    val breakDurationMinutes: Int = 15
)

@JsonClass(generateAdapter = true)
data class ChatResponse(
    val message: String,
    val conversationId: String,
    val structuredPlan: StructuredPlanDto? = null,
    val needsClarification: Boolean = false,
    val clarifyingQuestions: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class StructuredPlanDto(
    val title: String,
    val explanation: String,
    val tasks: List<PlanTaskDto>,
    val warnings: List<String> = emptyList(),
    val totalEstimatedMinutes: Int = 0
)

@JsonClass(generateAdapter = true)
data class PlanTaskDto(
    val title: String,
    val description: String = "",
    val priority: Int = 1,
    val estimatedMinutes: Int = 30,
    val suggestedDate: String? = null, // yyyy-MM-dd
    val suggestedStartTime: String? = null, // HH:mm
    val tags: List<String> = emptyList()
)
