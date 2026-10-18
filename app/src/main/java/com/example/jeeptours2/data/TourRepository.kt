package com.example.jeeptours2.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TourRepository(baseUrl: String = "http://10.0.2.2:8000/") {
    private val api: TourApiService = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TourApiService::class.java)

    suspend fun generateTour(duration: Int, difficulty: String, preferences: String): Result<TourResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.generateTour(TourRequest(duration, difficulty, preferences))
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}