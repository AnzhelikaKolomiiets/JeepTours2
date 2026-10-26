package com.example.jeeptours2.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jeeptours2.data.RetrofitInstance
import com.example.jeeptours2.data.TourRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TourViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<TourUiState>(TourUiState.Idle)
    val uiState: StateFlow<TourUiState> = _uiState

    fun generateTour(request: TourRequest) {
        viewModelScope.launch {
            _uiState.value = TourUiState.Loading
            try {
                val response = RetrofitInstance.api.generateTour(request)
                _uiState.value = TourUiState.Success(response)
            } catch (e: Exception) {
                _uiState.value = TourUiState.Error(e.message ?: "Помилка з'єднання з сервером")
            }
        }
    }
}