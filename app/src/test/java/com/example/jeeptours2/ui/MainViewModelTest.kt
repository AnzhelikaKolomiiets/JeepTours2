package com.example.jeeptours2.ui

import com.example.jeeptours2.data.repository.FakeTourRepository
import com.example.jeeptours2.domain.model.AppError
import com.example.jeeptours2.domain.model.TourRequestInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeTourRepository
    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeTourRepository()
        viewModel = MainViewModel(fakeRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isIdleAndEmptyHistory() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.tourResult)
        assertNull(state.errorMessage)
        assertTrue(state.savedTours.isEmpty())
    }

    @Test
    fun generateTour_success_updatesUiStateAndHistory() = runTest(testDispatcher) {
        val input = TourRequestInput(
            durationHours = 4,
            difficulty = "MEDIUM",
            preferences = "Говерла",
            routeOptionType = "Прямий підйом",
            language = "UA"
        )

        viewModel.generateTour(input)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.tourResult)
        assertEquals("Тестовий Джип-Тур «Говерла»", state.tourResult?.tourTitle)
        assertEquals(1, state.savedTours.size)
    }

    @Test
    fun generateTour_networkError_setsErrorMessageAndDoesNotSave() = runTest(testDispatcher) {
        fakeRepository.shouldReturnError = true
        fakeRepository.errorToReturn = AppError.NetworkError

        val input = TourRequestInput(
            durationHours = 4,
            difficulty = "HARD",
            preferences = "Шпиці",
            routeOptionType = "Через озеро",
            language = "UA"
        )
        viewModel.generateTour(input)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.tourResult)
        assertEquals(AppError.NetworkError, state.errorMessage)
        assertTrue(state.savedTours.isEmpty())
    }

    @Test
    fun deleteTour_removesItemFromHistory() = runTest(testDispatcher) {
        val input = TourRequestInput(
            durationHours = 2,
            difficulty = "EASY",
            preferences = "Хом'як",
            routeOptionType = "Прямий",
            language = "UA"
        )
        viewModel.generateTour(input)
        testDispatcher.scheduler.advanceUntilIdle()

        val savedId = viewModel.uiState.value.savedTours.first().id
        viewModel.deleteTour(savedId)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.savedTours.isEmpty())
    }

    @Test
    fun clearResultAndReset_clearsCurrentResultKeepHistory() = runTest(testDispatcher) {
        val input = TourRequestInput(
            durationHours = 4,
            difficulty = "MEDIUM",
            preferences = "Говерла",
            routeOptionType = "Прямий",
            language = "UA"
        )
        viewModel.generateTour(input)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearResultAndReset()

        val state = viewModel.uiState.value
        assertNull(state.tourResult)
        assertEquals(1, state.savedTours.size)
    }
}