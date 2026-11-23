package com.example.jeeptours2.data

import com.google.gson.annotations.SerializedName

data class TourResponse(
    @SerializedName("tourTitle") val tourTitle: String,
    @SerializedName("recommendedVehicle") val recommendedVehicle: String,
    @SerializedName("safetyTips") val safetyTips: String,
    @SerializedName("schedule") val schedule: List<String>? = null
)

data class TourBookingRequest(
    @SerializedName("tourTitle") val tourTitle: String,
    @SerializedName("bookingDate") val bookingDate: String,
    @SerializedName("userName") val userName: String,
    @SerializedName("userPhone") val userPhone: String,
    @SerializedName("language") val language: String
)

data class BookingResponse(
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: String? = null
)