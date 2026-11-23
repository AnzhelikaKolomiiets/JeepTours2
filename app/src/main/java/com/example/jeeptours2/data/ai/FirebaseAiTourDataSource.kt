package com.example.jeeptours2.data.ai

import android.util.Log
import com.example.jeeptours2.domain.model.AppError
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.io.IOException

interface TourAiDataSource {
    suspend fun generateTourJson(prompt: String): Result<String>
}

class FirebaseAiTourDataSource(
    private val timeoutMs: Long = 25_000L
) : TourAiDataSource {

    private val modelsToTry = listOf(
        "gemini-3.8-flash-lite",
        "gemini-3.8-flash"
    )

    private fun getModel(modelName: String) =
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = modelName,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                temperature = 0.4f
            }
        )

    override suspend fun generateTourJson(prompt: String): Result<String> {
        return try {
            val rawJson = withTimeout(timeoutMs) {
                var lastException: Exception? = null
                var responseText: String? = null

                for (modelName in modelsToTry) {
                    try {
                        Log.d("AI_DEBUG", "Запит до моделі: $modelName")
                        val response = getModel(modelName).generateContent(prompt)
                        responseText = response.text
                        if (!responseText.isNullOrBlank()) break
                    } catch (e: Exception) {
                        lastException = e
                        Log.w("AI_DEBUG", "Модель $modelName повернула помилку: ${e.message}")
                    }
                }

                if (!responseText.isNullOrBlank()) {
                    responseText
                } else {
                    val errMsg = lastException?.message.orEmpty().lowercase()
                    if (errMsg.contains("quota") || errMsg.contains("429")) {
                        Log.w("AI_DEBUG", "Денна квота Free Tier вичерпана — використовуємо локальний резервний JSON")
                        buildFallbackJson(prompt)
                    } else {
                        throw (lastException ?: IllegalStateException("Empty AI response body"))
                    }
                }
            }

            val cleanJson = rawJson
                .trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            Result.success(cleanJson)
        } catch (e: TimeoutCancellationException) {
            Result.failure(AppError.TimeoutError)
        } catch (e: IOException) {
            Result.failure(AppError.NetworkError)
        } catch (e: Exception) {
            Log.e("AI_DEBUG", "Кінцева помилка AI: ${e.message}", e)
            val msg = e.message.orEmpty().lowercase()
            val mappedError = when {
                msg.contains("app check") ||
                        msg.contains("appcheck") ||
                        msg.contains("permission_denied") ||
                        msg.contains("api key") ||
                        msg.contains("403") -> AppError.ConfigError

                msg.contains("unable to resolve host") ||
                        msg.contains("network") ||
                        msg.contains("timeout") ||
                        msg.contains("failed to connect") -> AppError.NetworkError

                else -> AppError.ServerError
            }
            Result.failure(mappedError)
        }
    }

    private fun buildFallbackJson(prompt: String): String {
        val isEnglish = prompt.contains("Respond strictly in English", ignoreCase = true)
        return if (isEnglish) {
            """
            {
              "isValidRequest": true,
              "rejectionReason": null,
              "tourTitle": "Synevyr Lake & Shypit Waterfall 4x4 Expedition",
              "recommendedVehicle": "Toyota Land Cruiser 80 / Nissan Patrol Y61 (33\" Mud-Terrain tires, winch, snorkel)",
              "weatherAdvice": "Mountain weather changes rapidly. Pack waterproof jackets, extra layers, and check tire pressure (1.6 bar) before gravel climbs.",
              "safetyTips": "Engage 4L on steep rocky descents, keep a 30m distance between vehicles, and stay strictly on marked forest trails.",
              "schedule": [
                "09:00 - Briefing and vehicle check at the base camp",
                "10:30 - Off-road ascent through beech forest trails toward Pylypets ridge",
                "12:30 - Photo stop and panoramic viewpoint overlooking Borzhava valley",
                "14:00 - Crossing mountain streams and arrival at Shypit Waterfall",
                "16:00 - Scenic drive to Synevyr Lake and return to base"
              ]
            }
            """.trimIndent()
        } else {
            """
            {
              "isValidRequest": true,
              "rejectionReason": null,
              "tourTitle": "Офроуд-експедиція: Озеро Синевир та водоспад Шипіт",
              "recommendedVehicle": "Toyota Land Cruiser 80 / Nissan Patrol Y61 (грязьова гума 33\", лебідка, шноркель)",
              "weatherAdvice": "У високогір'ї погода мінлива: візьміть вітрозахисні куртки та знизьте тиск у шинах до 1.6 атм перед кам'янистими підйомами.",
              "safetyTips": "На крутих спусках вмикайте понижену передачу (4L), тримайте дистанцію 30 м між авто та не з'їжджайте з маркованих лісових колій.",
              "schedule": [
                "09:00 - Інструктаж екіпажів, перевірка тиску в шинах та старт з бази",
                "10:30 - Підйом лісовими серпантинами та подолання кам'янистих ділянок",
                "12:30 - Оглядовий майданчик на хребті Боржава та привал",
                "14:00 - Форсування гірських потічків і зупинка біля водоспаду Шипіт",
                "16:00 - Переїзд до озера Синевир та повернення на базу"
              ]
            }
            """.trimIndent()
        }
    }
}