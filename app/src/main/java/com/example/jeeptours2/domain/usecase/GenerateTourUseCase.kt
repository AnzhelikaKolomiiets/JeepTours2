package com.example.jeeptours2.domain.usecase

import com.example.jeeptours2.domain.model.AppError
import com.example.jeeptours2.domain.model.GeneratedTourResult
import com.example.jeeptours2.domain.model.TourRequestInput
import com.example.jeeptours2.domain.repository.ITourRepository

class GenerateTourUseCase(
    private val repository: ITourRepository
) {
    suspend operator fun invoke(input: TourRequestInput): Result<GeneratedTourResult> {
        if (input.durationHours <= 0 || input.preferences.isBlank()) {
            return Result.failure(AppError.InvalidInputError)
        }
        return repository.generateTour(input)
    }
}