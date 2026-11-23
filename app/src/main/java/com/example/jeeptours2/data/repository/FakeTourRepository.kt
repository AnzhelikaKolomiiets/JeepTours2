package com.example.jeeptours2.data.repository

import com.example.jeeptours2.domain.model.*
import com.example.jeeptours2.domain.repository.ITourRepository

class FakeTourRepository : ITourRepository {

    var shouldReturnError = false
    var errorToReturn: AppError = AppError.NetworkError

    private val inMemoryTours = mutableListOf<SavedTourItem>()

    override suspend fun generateTour(input: TourRequestInput): Result<GeneratedTourResult> {
        if (shouldReturnError) {
            return Result.failure(errorToReturn)
        }
        val result = GeneratedTourResult(
            tourTitle = "Тестовий Джип-Тур «${input.preferences}»",
            recommendedVehicle = "Suzuki Jimny 4x4",
            safetyTips = "Тестові інструкції з безпеки",
            schedule = listOf("09:00 - Старт", "13:00 - Фініш")
        )
        inMemoryTours.add(
            SavedTourItem(
                id = (inMemoryTours.size + 1).toLong(),
                tourTitle = result.tourTitle,
                recommendedVehicle = result.recommendedVehicle,
                durationHours = input.durationHours,
                difficulty = input.difficulty,
                preferences = input.preferences,
                createdAt = "2026-11-11 12:00"
            )
        )
        return Result.success(result)
    }

    override suspend fun bookTour(tourTitle: String, bookingDate: String, name: String, phone: String, lang: String): Result<String> {
        if (shouldReturnError) return Result.failure(errorToReturn)
        return Result.success("Заброньовано для $name")
    }

    override suspend fun getSavedTours(): List<SavedTourItem> = inMemoryTours.reversed()

    override suspend fun deleteTour(id: Long) {
        inMemoryTours.removeAll { it.id == id }
    }

    override suspend fun clearHistory() {
        inMemoryTours.clear()
    }
}