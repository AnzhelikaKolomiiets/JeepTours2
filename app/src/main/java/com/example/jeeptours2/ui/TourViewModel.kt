package com.example.jeeptours2.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jeeptours2.domain.model.AppError
import com.example.jeeptours2.domain.model.SavedTourItem
import com.example.jeeptours2.domain.model.TourRequestInput
import com.example.jeeptours2.domain.repository.ITourRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TourViewModel(
    private val repository: ITourRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TourUiState>(TourUiState.Idle)
    val uiState: StateFlow<TourUiState> = _uiState.asStateFlow()

    private val _savedTours = MutableStateFlow<List<SavedTourItem>>(emptyList())
    val savedTours: StateFlow<List<SavedTourItem>> = _savedTours.asStateFlow()

    private val _bookingResult = MutableStateFlow<String?>(null)
    val bookingResult: StateFlow<String?> = _bookingResult.asStateFlow()

    private val _isBookingLoading = MutableStateFlow(false)
    val isBookingLoading: StateFlow<Boolean> = _isBookingLoading.asStateFlow()

    private var lastInput: TourRequestInput? = null

    init {
        loadSavedTours()
    }

    fun generateTour(input: TourRequestInput) {
        lastInput = input
        _bookingResult.value = null
        viewModelScope.launch {
            _uiState.value = TourUiState.Loading
            val result = repository.generateTour(input)
            result.fold(
                onSuccess = { data ->
                    _uiState.value = TourUiState.Success(data)
                    if (data.isValidRequest) {
                        loadSavedTours()
                    }
                },
                onFailure = { throwable ->
                    val appError = throwable as? AppError ?: AppError.Unknown
                    _uiState.value = TourUiState.Error(appError)
                }
            )
        }
    }

    fun retryLastTourGeneration() {
        lastInput?.let { generateTour(it) }
    }

    fun resetState() {
        _uiState.value = TourUiState.Idle
        _bookingResult.value = null
    }

    fun bookTour(tourTitle: String, bookingDate: String, name: String, phone: String, lang: String) {
        viewModelScope.launch {
            _isBookingLoading.value = true
            _bookingResult.value = null
            val result = repository.bookTour(tourTitle, bookingDate, name, phone, lang)
            result.fold(
                onSuccess = { msg -> _bookingResult.value = msg },
                onFailure = {
                    _bookingResult.value = if (lang == "EN") "Booking error" else "Помилка бронювання"
                }
            )
            _isBookingLoading.value = false
        }
    }

    fun loadSavedTours() {
        viewModelScope.launch {
            _savedTours.value = repository.getSavedTours()
        }
    }

    fun deleteTour(id: Long) {
        viewModelScope.launch {
            repository.deleteTour(id)
            loadSavedTours()
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            loadSavedTours()
        }
    }
}