package com.example.jeeptours2.ui

import com.example.jeeptours2.data.TourResponse

sealed interface TourUiState {
    object Idle : TourUiState
    object Loading : TourUiState
    data class Success(val tour: TourResponse) : TourUiState
    data class Error(val message: String) : TourUiState
}