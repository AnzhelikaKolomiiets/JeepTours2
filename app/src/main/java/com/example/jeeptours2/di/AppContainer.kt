package com.example.jeeptours2.di

import android.content.Context
import com.example.jeeptours2.data.RetrofitInstance
import com.example.jeeptours2.data.TourRepository
import com.example.jeeptours2.data.local.AppDatabase
import com.example.jeeptours2.domain.repository.ITourRepository

class AppContainer(private val context: Context) {

    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    val tourRepository: ITourRepository by lazy {
        TourRepository(
            tourDao = database.tourDao(),
            apiService = RetrofitInstance.api
        )
    }
}