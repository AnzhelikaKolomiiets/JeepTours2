package com.example.jeeptours2.data

import com.google.gson.annotations.SerializedName

data class TourRequest(
    @SerializedName("duration_hours") val durationHours: Int,
    @SerializedName("difficulty") val difficulty: String,
    @SerializedName("preferences") val preferences: String
)

data class RouteOption(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "Стандартний маршрут",
    @SerializedName("description") val description: String = "",
    @SerializedName("schedule") val schedule: List<String> = emptyList()
)

data class TourResponse(
    @SerializedName("tour_title") val tourTitle: String = "Джип-тур",
    @SerializedName("estimated_time") val estimatedTime: String = "",
    @SerializedName("recommended_vehicle") val recommendedVehicle: String = "Jeep 4x4",
    @SerializedName("routes") val routes: List<RouteOption>? = null,
    @SerializedName("itinerary") val itinerary: List<String>? = null,
    @SerializedName("safety_tips") val safetyTips: String = "Дотримуйтесь правил безпеки"
)