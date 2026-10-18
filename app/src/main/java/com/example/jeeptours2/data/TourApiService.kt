package com.example.jeeptours2.data

import retrofit2.http.Body
import retrofit2.http.POST

interface TourApiService {
    @POST("api/v1/tours/generate")
    suspend fun generateTour(@Body request: TourRequest): TourResponse
}