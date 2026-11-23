package com.example.jeeptours2.data.ai

import com.example.jeeptours2.domain.model.TourRequestInput

class TourPromptBuilder {

    companion object {
        const val MAX_PREFERENCES_LENGTH = 200
    }

    fun sanitizePreferences(raw: String): String {
        return raw.trim().take(MAX_PREFERENCES_LENGTH)
    }

    fun buildPrompt(input: TourRequestInput): String {
        val cleanPreferences = sanitizePreferences(input.preferences)
        val clampedHours = input.durationHours.coerceIn(1, 24)
        val isEnglish = input.language.equals("EN", ignoreCase = true)

        val targetLangInstruction = if (isEnglish) {
            "Respond strictly in English."
        } else {
            "Відповідай виключно українською мовою."
        }

        return """
            You are an expert off-road 4x4 tour planner in the Ukrainian Carpathians.
            $targetLangInstruction

            User trip parameters:
            - Duration: $clampedHours hours (valid range 1..24)
            - Difficulty level: ${input.difficulty}
            - Route style: ${input.routeOptionType}
            - Season / Weather conditions: ${input.seasonOrWeather}
            - Location / Preferences: "$cleanPreferences"

            Validation & Rules:
            1. Evaluate if this off-road jeep tour request is physically realistic, safe, and geographically plausible for a 4x4 expedition (reject Mars, Everest, underwater driving, extreme life-threatening disasters, or gibberish).
            2. If NOT realistic or safe: set "isValidRequest": false, explain why in "rejectionReason", and leave other fields empty.
            3. If valid and safe: set "isValidRequest": true, "rejectionReason": null, and fill all fields with realistic Carpathian 4x4 details and a $clampedHours-hour schedule.

            Return ONLY a valid JSON object matching this exact structure (no markdown, no extra text):
            {
              "isValidRequest": true,
              "rejectionReason": null,
              "tourTitle": "string",
              "recommendedVehicle": "string",
              "weatherAdvice": "string",
              "safetyTips": "string",
              "schedule": ["09:00 - ...", "10:00 - ..."]
            }
        """.trimIndent()
    }
}