package com.example.jeeptours2.data

class TourRepository {
    suspend fun generateTour(
        durationHours: Int,
        difficulty: String,
        preferences: String,
        language: String = "UA"
    ): Result<TourResponse> {
        return try {
            val request = TourRequest(
                durationHours = durationHours,
                difficulty = difficulty,
                preferences = preferences,
                language = language
            )
            val response = RetrofitInstance.api.generateTour(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}