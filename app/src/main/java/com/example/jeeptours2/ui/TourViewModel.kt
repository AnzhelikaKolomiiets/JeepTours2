package com.example.jeeptours2.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jeeptours2.data.TourRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TourViewModel(
    private val repository: TourRepository = TourRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<TourUiState>(TourUiState.Idle)
    val uiState: StateFlow<TourUiState> = _uiState.asStateFlow()

    fun generateTour(durationStr: String, difficulty: String, preferences: String) {
        val duration = durationStr.toIntOrNull()
        if (duration == null || duration <= 0) {
            _uiState.value = TourUiState.Error("Введіть коректну тривалість (від 1 год)")
            return
        }
        if (preferences.isBlank()) {
            _uiState.value = TourUiState.Error("Вкажіть ваші вподобання для туру")
            return
        }

        viewModelScope.launch {
            _uiState.value = TourUiState.Loading
            repository.generateTour(duration, difficulty, preferences)
                .onSuccess { tour ->
                    _uiState.value = TourUiState.Success(tour)
                }
                .onFailure { error ->
                    _uiState.value = TourUiState.Error(
                        error.localizedMessage ?: "Не вдалося з'єднатися з сервером"
                    )
                }
        }
    }
}