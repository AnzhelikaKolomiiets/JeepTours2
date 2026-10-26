package com.example.jeeptours2.data

import retrofit2.http.Body
import retrofit2.http.POST

interface TourApiService {
    @POST("generate_route")
    suspend fun generateTour(@Body request: TourRequest): TourResponse
}