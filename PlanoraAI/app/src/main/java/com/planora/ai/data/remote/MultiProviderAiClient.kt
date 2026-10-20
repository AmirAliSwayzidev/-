package com.planora.ai.data.remote

import com.planora.ai.data.local.entity.UserPreferencesEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Direct multi-provider client (OpenAI-compatible + Gemini).
 * Providers: openai | grok | gemini | deepseek | groq | custom
 */
object MultiProviderAiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    private val JSON = "application/json; charset=utf-8".toMediaType()

    data class Preset(val baseUrl: String, val model: String, val style: String)

    val presets = mapOf(
        "openai" to Preset("https://api.openai.com/v1", "gpt-4o-mini", "openai"),
        "grok" to Preset("https://api.x.ai/v1", "grok-2-latest", "openai"),
        "gemini" to Preset("https://generativelanguage.googleapis.com/v1beta", "gemini-2.0-flash", "gemini"),
        "deepseek" to Preset("https://api.deepseek.com", "deepseek-chat", "openai"),
        "groq" to Preset("https://api.groq.com/openai/v1", "llama-3.3-70b-versatile", "openai"),
        "custom" to Preset("https://api.openai.com/v1", "gpt-4o-mini", "openai")
    )

    fun sanitizeKey(key: String?): String {
        if (key.isNullOrBlank()) return ""
        return key
            .replace(Regex("[\\u200c\\u200b\\ufeff]"), "")
            .replace(Regex("[\\r\\n\\t]"), "")
            .filter { it.code in 0..127 }
            .trim()
    }

    suspend fun chat(
        prefs: UserPreferencesEntity,
        systemPrompt: String,
        history: List<Pair<String, String>>,
        userMessage: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val provider = prefs.apiProvider.ifBlank { "openai" }
        val preset = presets[provider] ?: presets["openai"]!!
        val baseUrl = prefs.apiBaseUrl.ifBlank { preset.baseUrl }.trimEnd('/')
        val model = prefs.apiModel.ifBlank { preset.model }
        val apiKey = sanitizeKey(prefs.apiKey)
        if (apiKey.length < 8) {
            return@withContext Result.failure(
                Exception("API Key تنظیم نشده. در پروفایل کلید را ذخیره کنید.")
            )
        }

        try {
            val content = if (provider == "gemini" || preset.style == "gemini") {
                callGemini(baseUrl, model, apiKey, systemPrompt, history, userMessage)
            } else {
                callOpenAiCompat(baseUrl, model, apiKey, systemPrompt, history, userMessage)
            }
            Result.success(content)
        } catch (e: Exception) {
            Result.failure(Exception(friendlyError(e)))
        }
    }

    private fun callOpenAiCompat(
        baseUrl: String,
        model: String,
        apiKey: String,
        system: String,
        history: List<Pair<String, String>>,
        userMessage: String
    ): String {
        val messages = JSONArray()
        messages.put(JSONObject().put("role", "system").put("content", system))
        history.takeLast(12).forEach { (role, content) ->
            messages.put(
                JSONObject()
                    .put("role", if (role == "user") "user" else "assistant")
                    .put("content", content)
            )
        }
        messages.put(JSONObject().put("role", "user").put("content", userMessage))

        val body = JSONObject()
            .put("model", model)
            .put("messages", messages)
            .put("temperature", 0.7)

        val req = Request.Builder()
            .url("$baseUrl/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(body.toString().toRequestBody(JSON))
            .build()

        client.newCall(req).execute().use { res ->
            val raw = res.body?.string().orEmpty()
            if (!res.isSuccessful) throw Exception(parseHttpError(res.code, raw))
            val json = JSONObject(raw)
            return json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        }
    }

    private fun callGemini(
        baseUrl: String,
        model: String,
        apiKey: String,
        system: String,
        history: List<Pair<String, String>>,
        userMessage: String
    ): String {
        val contents = JSONArray()
        history.takeLast(12).forEach { (role, content) ->
            contents.put(
                JSONObject()
                    .put("role", if (role == "user") "user" else "model")
                    .put("parts", JSONArray().put(JSONObject().put("text", content)))
            )
        }
        contents.put(
            JSONObject()
                .put("role", "user")
                .put("parts", JSONArray().put(JSONObject().put("text", userMessage)))
        )

        val body = JSONObject()
            .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", system))))
            .put("contents", contents)
            .put("generationConfig", JSONObject().put("temperature", 0.7))

        val url = "$baseUrl/models/$model:generateContent?key=$apiKey"
        val req = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .post(body.toString().toRequestBody(JSON))
            .build()

        client.newCall(req).execute().use { res ->
            val raw = res.body?.string().orEmpty()
            if (!res.isSuccessful) throw Exception(parseHttpError(res.code, raw))
            val json = JSONObject(raw)
            val parts = json.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
            val sb = StringBuilder()
            for (i in 0 until parts.length()) {
                sb.append(parts.getJSONObject(i).optString("text"))
            }
            return sb.toString()
        }
    }

    private fun parseHttpError(code: Int, raw: String): String {
        val msg = try {
            JSONObject(raw).optJSONObject("error")?.optString("message")
                ?: JSONObject(raw).optString("message")
                ?: raw.take(200)
        } catch (_: Exception) {
            raw.take(200)
        }
        return when (code) {
            401, 400 -> "کلید API رد شد ($code). کلید و Base URL را با همان سرویس یکی کنید.\n$msg"
            402 -> "موجودی اکانت تمام شده (402). از Groq یا Gemini رایگان تست کنید یا شارژ کنید."
            403 -> "دسترسی رد شد (403). اعتبار/لایسنس اکانت را چک کنید.\n$msg"
            429 -> "محدودیت نرخ. کمی بعد تلاش کنید."
            else -> "خطا در ارتباط با AI ($code): $msg"
        }
    }

    private fun friendlyError(e: Exception): String = e.message ?: "خطای شبکه"
}
