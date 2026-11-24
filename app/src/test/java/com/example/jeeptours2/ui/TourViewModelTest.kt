package com.example.jeeptours2.ui

import com.example.jeeptours2.domain.model.AppError
import com.example.jeeptours2.domain.model.GeneratedTourResult
import com.example.jeeptours2.domain.model.SavedTourItem
import com.example.jeeptours2.domain.model.TourRequestInput
import com.example.jeeptours2.domain.repository.ITourRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TourViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeTourRepository : ITourRepository {
        var nextResult: Result<GeneratedTourResult> = Result.success(
            GeneratedTourResult(
                isValidRequest = true,
                rejectionReason = null,
                tourTitle = "Офроуд-експедиція: Говерла",
                recommendedVehicle = "Jeep Wrangler Rubicon",
                weatherAdvice = "Ясна погода",
                safetyTips = "Тримайтеся за поручні",
                schedule = listOf("09:00 - Виїзд", "12:00 - Вершина")
            )
        )
        private val storage = mutableListOf<SavedTourItem>()

        override suspend fun generateTour(input: TourRequestInput): Result<GeneratedTourResult> {
            val res = nextResult
            res.onSuccess { data ->
                if (data.isValidRequest) {
                    storage.add(
                        SavedTourItem(
                            id = (storage.size + 1).toLong(),
                            tourTitle = data.tourTitle,
                            recommendedVehicle = data.recommendedVehicle,
                            durationHours = input.durationHours,
                            difficulty = input.difficulty,
                            preferences = input.preferences,
                            createdAt = "2026-11-24 19:25"
                        )
                    )
                }
            }
            return res
        }

        override suspend fun getSavedTours(): List<SavedTourItem> = storage.toList()

        override suspend fun deleteTour(id: Long) {
            storage.removeAll { it.id == id }
        }

        override suspend fun clearHistory() {
            storage.clear()
        }

        override suspend fun bookTour(
            tourTitle: String,
            bookingDate: String,
            name: String,
            phone: String,
            lang: String
        ): Result<String> = Result.success("Бронювання підтверджено")
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val sampleInput = TourRequestInput(
        durationHours = 4,
        difficulty = "MEDIUM",
        preferences = "Говерла",
        routeOptionType = "Прямий підйом",
        language = "UA",
        seasonOrWeather = "Сухо"
    )

    @Test
    fun generateTourUpdatesStateToSuccessAndSavesToHistory() = runTest {
        val repo = FakeTourRepository()
        val viewModel = TourViewModel(repo)

        viewModel.generateTour(sampleInput)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is TourUiState.Success)
        assertEquals("Офроуд-експедиція: Говерла", (state as TourUiState.Success).result.tourTitle)
        assertEquals(1, viewModel.savedTours.value.size)
    }

    @Test
    fun networkFailureSetsErrorStateAndRetryRecovers() = runTest {
        val repo = FakeTourRepository()
        repo.nextResult = Result.failure(AppError.NetworkError)
        val viewModel = TourViewModel(repo)

        viewModel.generateTour(sampleInput)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is TourUiState.Error)
        assertEquals(AppError.NetworkError, (viewModel.uiState.value as TourUiState.Error).error)

        repo.nextResult = Result.success(
            GeneratedTourResult(
                isValidRequest = true,
                rejectionReason = null,
                tourTitle = "Офроуд-експедиція: Говерла",
                recommendedVehicle = "Jeep Wrangler Rubicon",
                weatherAdvice = "Ясно",
                safetyTips = "Безпека",
                schedule = listOf("09:00 - Старт")
            )
        )
        viewModel.retryLastTourGeneration()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is TourUiState.Success)
    }

    @Test
    fun invalidRequestDoesNotSaveToHistory() = runTest {
        val repo = FakeTourRepository()
        repo.nextResult = Result.success(
            GeneratedTourResult(
                isValidRequest = false,
                rejectionReason = "Неможливо дістатися на Марс на 4x4",
                tourTitle = "",
                recommendedVehicle = "",
                weatherAdvice = "",
                safetyTips = "",
                schedule = emptyList()
            )
        )
        val viewModel = TourViewModel(repo)

        viewModel.generateTour(sampleInput.copy(preferences = "Марс"))
        advanceUntilIdle()

        val state = viewModel.uiState.value as TourUiState.Success
        assertEquals(false, state.result.isValidRequest)
        assertEquals(0, viewModel.savedTours.value.size)
    }

    @Test
    fun resetStateReturnsUiStateToIdle() = runTest {
        val repo = FakeTourRepository()
        val viewModel = TourViewModel(repo)

        viewModel.generateTour(sampleInput)
        advanceUntilIdle()
        viewModel.resetState()

        assertTrue(viewModel.uiState.value is TourUiState.Idle)
    }
}
