package com.example.jeeptours2.data

import com.example.jeeptours2.data.ai.FirebaseAiTourDataSource
import com.example.jeeptours2.data.ai.TourAiDataSource
import com.example.jeeptours2.data.ai.TourPromptBuilder
import com.example.jeeptours2.data.ai.TourResponseMapper
import com.example.jeeptours2.data.local.TourDao
import com.example.jeeptours2.data.local.TourEntity
import com.example.jeeptours2.domain.model.AppError
import com.example.jeeptours2.domain.model.GeneratedTourResult
import com.example.jeeptours2.domain.model.SavedTourItem
import com.example.jeeptours2.domain.model.TourRequestInput
import com.example.jeeptours2.domain.repository.ITourRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TourRepository(
    private val tourDao: TourDao,
    private val apiService: TourApiService = RetrofitInstance.api,
    private val promptBuilder: TourPromptBuilder = TourPromptBuilder(),
    private val aiDataSource: TourAiDataSource = FirebaseAiTourDataSource(),
    private val responseMapper: TourResponseMapper = TourResponseMapper()
) : ITourRepository {

    override suspend fun generateTour(input: TourRequestInput): Result<GeneratedTourResult> = withContext(Dispatchers.IO) {
        if (input.durationHours !in 1..24 || input.preferences.isBlank()) {
            return@withContext Result.failure(AppError.InvalidInputError)
        }

        val isEnglish = input.language.equals("EN", ignoreCase = true)

        // 1. Підготовка даних (без персональних даних користувача)
        val prompt = promptBuilder.buildPrompt(input)

        // 2. Виклик зовнішнього сервісу Gemini Developer API через Firebase AI Logic
        val rawJsonResult = aiDataSource.generateTourJson(prompt)
        val rawJson = rawJsonResult.getOrElse { throwable ->
            val appError = throwable as? AppError ?: AppError.Unknown
            return@withContext Result.failure(appError)
        }

        // 3. Перетворення та валідація структурованої відповіді JSON
        val mappedResult = responseMapper.map(rawJson, isEnglish)
        mappedResult.onSuccess { domainResult ->
            if (domainResult.isValidRequest) {
                val cleanPrefs = promptBuilder.sanitizePreferences(input.preferences)
                val entity = TourEntity(
                    tourTitle = domainResult.tourTitle,
                    description = domainResult.safetyTips,
                    routeType = "$cleanPrefs (${input.routeOptionType} •${input.seasonOrWeather})",
                    difficulty = input.difficulty,
                    durationHours = input.durationHours,
                    createdAt = System.currentTimeMillis()
                )
                tourDao.insertTour(entity)
            }
        }
        return@withContext mappedResult
    }

    override suspend fun bookTour(
        tourTitle: String,
        bookingDate: String,
        name: String,
        phone: String,
        lang: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val langCode = if (lang.lowercase().startsWith("en")) "en" else "uk"
            val request = TourBookingRequest(
                tourTitle = tourTitle,
                bookingDate = bookingDate,
                userName = name,
                userPhone = phone,
                language = langCode
            )
            val response = apiService.bookTour(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                Result.failure(AppError.ServerError)
            }
        } catch (e: Exception) {
            Result.failure(AppError.NetworkError)
        }
    }

    override suspend fun getSavedTours(): List<SavedTourItem> = withContext(Dispatchers.IO) {
        val entities = tourDao.getAllTours()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        entities.map { item ->
            val formattedDate = dateFormat.format(Date(item.createdAt))
            SavedTourItem(
                id = item.id,
                tourTitle = item.tourTitle,
                recommendedVehicle = "Jeep 4x4",
                durationHours = item.durationHours,
                difficulty = item.difficulty,
                preferences = item.routeType,
                createdAt = formattedDate
            )
        }
    }

    override suspend fun deleteTour(id: Long) = withContext(Dispatchers.IO) {
        tourDao.deleteTourById(id)
    }

    override suspend fun clearHistory() = withContext(Dispatchers.IO) {
        tourDao.clearAllToursCount()
    }
}