package com.example.jeeptours2.domain.model

// Вхідні дані від користувача для генерації
data class TourRequestInput(
    val durationHours: Int,
    val difficulty: String,
    val preferences: String,
    val routeOptionType: String,
    val language: String,
    val seasonOrWeather: String = "Сухо"
)

// Чистий результат згенерованого туру для UI та UseCase
data class GeneratedTourResult(
    val tourTitle: String,
    val recommendedVehicle: String,
    val safetyTips: String,
    val schedule: List<String>,
    val weatherAdvice: String = "",
    val isValidRequest: Boolean = true,
    val rejectionReason: String? = null
)

// Чистий елемент історії
data class SavedTourItem(
    val id: Long,
    val tourTitle: String,
    val recommendedVehicle: String,
    val durationHours: Int,
    val difficulty: String,
    val preferences: String,
    val createdAt: String
)