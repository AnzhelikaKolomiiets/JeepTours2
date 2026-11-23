package com.example.jeeptours2.domain.repository

import com.example.jeeptours2.domain.model.GeneratedTourResult
import com.example.jeeptours2.domain.model.SavedTourItem
import com.example.jeeptours2.domain.model.TourRequestInput

interface ITourRepository {
    suspend fun generateTour(input: TourRequestInput): Result<GeneratedTourResult>
    suspend fun bookTour(tourTitle: String, bookingDate: String, name: String, phone: String, lang: String): Result<String>
    suspend fun getSavedTours(): List<SavedTourItem>
    suspend fun deleteTour(id: Long)
    suspend fun clearHistory()
}