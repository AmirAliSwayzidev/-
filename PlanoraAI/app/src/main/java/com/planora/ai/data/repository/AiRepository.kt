package com.planora.ai.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.planora.ai.data.local.dao.ChatDao
import com.planora.ai.data.local.entity.ChatMessageEntity
import com.planora.ai.data.local.entity.ConversationEntity
import com.planora.ai.data.local.entity.UserPreferencesEntity
import com.planora.ai.data.remote.*
import com.planora.ai.data.remote.MultiProviderAiClient
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.util.UUID

/**
 * Online AI only when:
 * 1) Internet is available
 * 2) User has set apiKey in preferences
 *
 * Offline: NEVER invents plans. Clear message only.
 *
 * ═══════════════════════════════════════════════
 * WHERE TO ADD API KEY:
 * 1. In-app: Settings / Profile → API Key fields
 *    (saved into UserPreferencesEntity.apiKey)
 * 2. Or temporarily for debug in PreferencesDao insert.
 * NEVER hardcode the key in this file or BuildConfig for production.
 * ═══════════════════════════════════════════════
 */
class AiRepository(
    private val api: AiApiService?,
    private val chatDao: ChatDao,
    private val context: Context
) {
    fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun getMessages(conversationId: String): Flow<List<ChatMessageEntity>> =
        chatDao.getMessages(conversationId)

    fun getConversations(): Flow<List<ConversationEntity>> = chatDao.getAllConversations()

    suspend fun sendMessage(
        conversationId: String?,
        userMessage: String,
        history: List<ChatMessageEntity>,
        preferences: UserPreferencesEntity,
        existingTasksSummary: String? = null
    ): Result<ChatResponse> {
        val convId = conversationId ?: UUID.randomUUID().toString()

        chatDao.insertMessage(
            ChatMessageEntity(conversationId = convId, role = "user", content = userMessage)
        )

        // ── OFFLINE: no AI plans at all ──
        if (!isOnline()) {
            val msg = "شما آفلاین هستید.\n\n" +
                    "در حالت آفلاین دستیار هوش مصنوعی غیرفعال است و هیچ برنامه‌ای تولید نمی‌شود.\n\n" +
                    "می‌توانید کار، یادداشت، تگ و زمان دلخواه را خودتان اضافه کنید. " +
                    "برای گفتگو با AI به اینترنت وصل شوید و API Key را در تنظیمات وارد کنید."
            chatDao.insertMessage(
                ChatMessageEntity(conversationId = convId, role = "assistant", content = msg)
            )
            ensureConversation(convId, userMessage)
            return Result.success(
                ChatResponse(message = msg, conversationId = convId, structuredPlan = null)
            )
        }

        // ── No API key ──
        if (preferences.apiKey.isBlank() || preferences.apiKey.length < 10) {
            val msg = "API Key تنظیم نشده است.\n\n" +
                    "مسیر: پروفایل / تنظیمات → فیلد API Key\n\n" +
                    "کلید را از پنل ارائه‌دهنده (OpenAI، xAI و …) بگیرید و اینجا ذخیره کنید. " +
                    "کلید فقط روی دستگاه شما می‌ماند."
            chatDao.insertMessage(
                ChatMessageEntity(conversationId = convId, role = "assistant", content = msg)
            )
            ensureConversation(convId, userMessage)
            return Result.success(
                ChatResponse(message = msg, conversationId = convId, structuredPlan = null)
            )
        }

        if (api == null) {
            val msg = "سرویس شبکه پیکربندی نشده. Base URL و Retrofit را در لایه remote تنظیم کنید.\n" +
                    "API Key شما در تنظیمات ذخیره شده است."
            chatDao.insertMessage(
                ChatMessageEntity(conversationId = convId, role = "assistant", content = msg, isError = true)
            )
            return Result.failure(IllegalStateException("AiApiService is null"))
        }

        // Direct multi-provider (GPT / Grok / Gemini / DeepSeek / Groq)
        val system = """تو دستیار برنامه‌ریزی پلانورا هستی. فارسی پاسخ بده.
ساعات ترجیحی: ${preferences.preferredStartHour}–${preferences.preferredEndHour}
سقف روزانه: ${preferences.dailyWorkloadLimitHours} ساعت.
${existingTasksSummary?.let { "کارهای فعلی: $it" } ?: ""}"""

        val hist = history.map { it.role to it.content }
        val result = MultiProviderAiClient.chat(preferences, system, hist, userMessage)
        return result.fold(
            onSuccess = { content ->
                chatDao.insertMessage(
                    ChatMessageEntity(
                        conversationId = convId,
                        role = "assistant",
                        content = content
                    )
                )
                ensureConversation(convId, userMessage)
                Result.success(
                    ChatResponse(
                        message = content,
                        conversationId = convId,
                        structuredPlan = null
                    )
                )
            },
            onFailure = { e ->
                val errorMsg = e.message ?: "خطا در ارتباط با AI"
                chatDao.insertMessage(
                    ChatMessageEntity(
                        conversationId = convId,
                        role = "assistant",
                        content = errorMsg,
                        isError = true
                    )
                )
                Result.failure(e)
            }
        )
    }

    private suspend fun ensureConversation(id: String, firstMessage: String) {
        val existing = chatDao.getConversation(id)
        if (existing == null) {
            chatDao.insertConversation(
                ConversationEntity(id = id, title = firstMessage.take(40), messageCount = 2)
            )
        } else {
            chatDao.updateConversation(
                existing.copy(updatedAt = LocalDateTime.now(), messageCount = existing.messageCount + 2)
            )
        }
    }
}
