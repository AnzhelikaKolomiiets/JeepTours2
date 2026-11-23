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
        val lowerPrompt = prompt.lowercase()

        val locationMatch = Regex("""(?i)(?:локація|побажання|location|preferences|wishes)[^:\n\r]*[:\-]\s*["«]?([^"\n\r»]+)["»]?""")
            .find(prompt)
        val rawLocation = locationMatch?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotBlank() }
        val locationToCheck = rawLocation.orEmpty().lowercase()

        val invalidKeywords = listOf("марс", "mars", "місяць", "moon", "еверест", "everest", "океан", "ocean", "підвод")
        if (invalidKeywords.any { locationToCheck.contains(it) }) {
            return if (isEnglish) {
                """
                {
                  "isValidRequest": false,
                  "rejectionReason": "The requested destination is physically unreachable or unsafe for a 4x4 off-road vehicle in the Carpathians.",
                  "tourTitle": "",
                  "recommendedVehicle": "",
                  "weatherAdvice": "",
                  "safetyTips": "",
                  "schedule": []
                }
                """.trimIndent()
            } else {
                """
                {
                  "isValidRequest": false,
                  "rejectionReason": "Запитаний маршрут є фізично неможливим або небезпечним для позашляховика 4x4 в умовах Українських Карпат.",
                  "tourTitle": "",
                  "recommendedVehicle": "",
                  "weatherAdvice": "",
                  "safetyTips": "",
                  "schedule": []
                }
                """.trimIndent()
            }
        }

        val isHard = lowerPrompt.contains("hard")
        val isMedium = lowerPrompt.contains("medium")
        val isRainMud = lowerPrompt.contains("дощ") || lowerPrompt.contains("бруд") || lowerPrompt.contains("rain") || lowerPrompt.contains("mud")
        val isSnow = lowerPrompt.contains("зима") || lowerPrompt.contains("сніг") || lowerPrompt.contains("snow") || lowerPrompt.contains("winter")

        val displayLocationEn = rawLocation ?: "Synevyr Lake & Shypit Waterfall"
        val displayLocationUa = rawLocation?.replaceFirstChar { it.uppercase() } ?: "Озеро Синевир та водоспад Шипіт"

        return if (isEnglish) {
            val vehicleEn = when {
                isHard && isSnow -> "Arctic-prepped Toyota Hilux 4x4 on 35\" studded tires"
                isHard -> "Modified Nissan Patrol Y61 with portal axles and winch"
                isMedium && isRainMud -> "Mitsubishi Pajero Sport 4WD on Mud-Terrain tires"
                isMedium -> "Jeep Wrangler Rubicon (4x4 with locking differentials)"
                isSnow -> "Subaru Outback Wilderness / Land Cruiser Prado (Winter 4WD)"
                else -> "Toyota Land Cruiser Prado 150 (Comfortable 4x4)"
            }

            val weatherEn = when {
                isSnow -> "Sub-zero ridge winds expected: bring thermal flasks, insulated gloves, and snow goggles for viewpoints."
                isRainMud -> "Slippery clay ruts ahead: waterproof boots and a spare dry jacket are essential for photo stops."
                else -> "Clear mountain visibility, though UV radiation is high on open ridges — pack sunglasses and windbreakers."
            }

            val passengerTipEn = when {
                isHard -> "Hold onto the grab handles on steep rocky steps, keep cameras secured on straps, and don't lean out of windows during side tilts."
                isMedium -> "Expect bouncy forest trails — avoid hot open drinks inside the cabin and wait for the driver's signal before opening doors on slopes."
                else -> "Keep snacks and water easily reachable in the cabin, and watch out for low-hanging beech branches near open windows."
            }

            """
            {
              "isValidRequest": true,
              "rejectionReason": null,
              "tourTitle": "4x4 Off-Road Expedition: $displayLocationEn",
              "recommendedVehicle": "$vehicleEn",
              "weatherAdvice": "$weatherEn",
              "safetyTips": "$passengerTipEn",
              "schedule": [
                "09:00 - Crew briefing and tire pressure adjustment at base camp",
                "10:30 - Off-road climb along forest trails toward $displayLocationEn",
                "12:30 - Panoramic ridge stop and hot tea break",
                "14:00 - Exploring key viewpoints around $displayLocationEn",
                "16:00 - Scenic descent and return to base camp"
              ]
            }
            """.trimIndent()
        } else {
            val vehicleUa = when {
                isHard && isSnow -> "Підготовлений Toyota Hilux 4x4 на 35-х зимових шипованих колесах"
                isHard -> "Експедиційний Nissan Patrol Y61 із блокуванням диференціалів"
                isMedium && isRainMud -> "Mitsubishi Pajero Sport 4WD на грязьовій гумі"
                isMedium -> "Jeep Wrangler Rubicon (позашляховик із пониженим рядом)"
                isSnow -> "Land Cruiser Prado 150 (повнопривідний зимовий пакет)"
                else -> "Комфортний Toyota Land Cruiser Prado 150 (4x4)"
            }

            val weatherUa = when {
                isSnow -> "На хребті пронизливий вітер і мороз: обов'язково візьміть термос із гарячим чаєм, рукавиці та баф."
                isRainMud -> "Після опадів на підйомах глибока колія: взуйте трекінгові черевики з протектором і візьміть дощовик."
                else -> "Ясна погода на вершинах, але ввечері швидко холоднішає — захопіть вітрівку та сонцезахисні окуляри."
            }

            val passengerTipUa = when {
                isHard -> "На крутих кам'янистих схилах тримайтеся за поручні салону, зафіксуйте телефони та не висовуйте руки у вікна під час кренів."
                isMedium -> "На лісових вибоїнах добряче трясе — не тримайте відкриті гарячі напої в руках і виходьте з авто лише після повної зупинки."
                else -> "Візьміть у салон перекус і воду в пляшках із кришкою, а біля густих гілок не тримайте лікті на відкритому вікні."
            }

            """
            {
              "isValidRequest": true,
              "rejectionReason": null,
              "tourTitle": "Офроуд-експедиція: $displayLocationUa",
              "recommendedVehicle": "$vehicleUa",
              "weatherAdvice": "$weatherUa",
              "safetyTips": "$passengerTipUa",
              "schedule": [
                "09:00 - Знайомство екіпажу, налаштування тиску в шинах і виїзд із бази",
                "10:30 - Підйом гірськими серпантинами у напрямку: $displayLocationUa",
                "12:30 - Зупинка на панорамному хребті та привал із чаєм",
                "14:00 - Прогулянка біля локації «$displayLocationUa» та фотосесія",
                "16:00 - Спуск лісовою дорогою та повернення на базу"
              ]
            }
            """.trimIndent()
        }
    }
}