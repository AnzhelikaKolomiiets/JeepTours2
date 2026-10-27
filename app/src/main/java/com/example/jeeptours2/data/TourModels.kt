package com.example.jeeptours2.data

import com.google.gson.annotations.SerializedName

data class TourRequest(
    @SerializedName("durationHours")
    val durationHours: Int,

    @SerializedName("difficulty")
    val difficulty: String,

    @SerializedName("preferences")
    val preferences: String,

    @SerializedName("language")
    val language: String = "UA"
)

data class RouteOption(
    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("schedule")
    val schedule: List<String>
)

data class TourResponse(
    @SerializedName("tourTitle")
    val tourTitle: String,

    @SerializedName("recommendedVehicle")
    val recommendedVehicle: String,

    @SerializedName("routes")
    val routes: List<RouteOption> = emptyList(),

    @SerializedName("safetyTips")
    val safetyTips: String
)