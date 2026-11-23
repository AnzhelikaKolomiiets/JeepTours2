package com.example.jeeptours2.data.ai

import com.example.jeeptours2.domain.model.AppError
import com.example.jeeptours2.domain.model.GeneratedTourResult
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class AiTourJsonDto(
    @SerializedName("isValidRequest") val isValidRequest: Boolean = true,
    @SerializedName("rejectionReason") val rejectionReason: String? = null,
    @SerializedName("tourTitle") val tourTitle: String? = null,
    @SerializedName("recommendedVehicle") val recommendedVehicle: String? = null,
    @SerializedName("weatherAdvice") val weatherAdvice: String? = null,
    @SerializedName("safetyTips") val safetyTips: String? = null,
    @SerializedName("schedule") val schedule: List<String>? = null
)

class TourResponseMapper(
    private val gson: Gson = Gson()
) {
    fun map(rawJson: String, isEnglish: Boolean): Result<GeneratedTourResult> {
        return try {
            val dto = gson.fromJson(rawJson, AiTourJsonDto::class.java)
                ?: return Result.failure(AppError.ServerError)

            if (!dto.isValidRequest) {
                val defaultReason = if (isEnglish) {
                    "This route cannot be organized safely due to unrealistic location or weather conditions."
                } else {
                    "Неможливо безпечно побудувати джип-тур за вказаними параметрами (нереалістична локація або небезпечні умови)."
                }
                return Result.success(
                    GeneratedTourResult(
                        tourTitle = "",
                        recommendedVehicle = "",
                        safetyTips = "",
                        schedule = emptyList(),
                        weatherAdvice = "",
                        isValidRequest = false,
                        rejectionReason = dto.rejectionReason?.takeIf { it.isNotBlank() } ?: defaultReason
                    )
                )
            }

            val title = dto.tourTitle?.takeIf { it.isNotBlank() }
                ?: return Result.failure(AppError.ServerError)

            val scheduleList = dto.schedule?.filter { it.isNotBlank() }.orEmpty()
            if (scheduleList.isEmpty()) {
                return Result.failure(AppError.ServerError)
            }

            Result.success(
                GeneratedTourResult(
                    tourTitle = title,
                    recommendedVehicle = dto.recommendedVehicle?.takeIf { it.isNotBlank() } ?: "Jeep Wrangler 4x4",
                    safetyTips = dto.safetyTips?.takeIf { it.isNotBlank() } ?: "",
                    schedule = scheduleList,
                    weatherAdvice = dto.weatherAdvice?.takeIf { it.isNotBlank() } ?: "",
                    isValidRequest = true,
                    rejectionReason = null
                )
            )
        } catch (e: Exception) {
            Result.failure(AppError.ServerError)
        }
    }
}