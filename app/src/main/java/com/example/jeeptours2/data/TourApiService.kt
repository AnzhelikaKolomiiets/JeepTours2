package com.example.jeeptours2.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TourApiService {

    @GET("api/v1/tours/generate")
    suspend fun generateTour(
        @Query("duration_hours") durationHours: Int,
        @Query("difficulty") difficulty: String,
        @Query("preferences") preferences: String,
        @Query("language") language: String
    ): Response<TourResponse>

    @POST("api/v1/tours/book")
    suspend fun bookTour(
        @Body request: TourBookingRequest
    ): Response<BookingResponse>
}