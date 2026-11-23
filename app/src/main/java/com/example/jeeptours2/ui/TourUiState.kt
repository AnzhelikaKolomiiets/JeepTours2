package com.example.jeeptours2.ui

import com.example.jeeptours2.domain.model.AppError
import com.example.jeeptours2.domain.model.GeneratedTourResult

sealed class TourUiState {
    object Idle : TourUiState()
    object Loading : TourUiState()
    data class Success(val result: GeneratedTourResult) : TourUiState()
    data class Error(val error: AppError) : TourUiState()
}