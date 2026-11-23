package com.example.jeeptours2

import com.example.jeeptours2.data.TourRepository
import com.example.jeeptours2.data.ai.TourAiDataSource
import com.example.jeeptours2.data.ai.TourPromptBuilder
import com.example.jeeptours2.data.ai.TourResponseMapper
import com.example.jeeptours2.data.local.TourDao
import com.example.jeeptours2.data.local.TourEntity
import com.example.jeeptours2.domain.model.AppError
import com.example.jeeptours2.domain.model.TourRequestInput
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TourAiGeneratorTest {

    private class InMemoryTourDao : TourDao {
        val items = mutableListOf<TourEntity>()
        override suspend fun insertTour(tour: TourEntity) { items.add(tour) }
        override suspend fun getAllTours(): List<TourEntity> = items.toList()
        override suspend fun deleteTourById(id: Long) { items.removeAll { it.id == id } }
        override suspend fun clearAllToursCount() { items.clear() }
    }

    private class FakeAiDataSource(
        var nextResult: Result<String>
    ) : TourAiDataSource {
        var lastPrompt: String = ""
        override suspend fun generateTourJson(prompt: String): Result<String> {
            lastPrompt = prompt
            return nextResult
        }
    }

    @Test
    fun promptBuilder_truncatesPreferencesTo200Chars_andIncludesWeatherWithoutPersonalData() {
        val builder = TourPromptBuilder()
        val longText = "А".repeat(250)
        val input = TourRequestInput(
            durationHours = 5,
            difficulty = "HARD",
            preferences = longText,
            routeOptionType = "Хребет",
            language = "UA",
            seasonOrWeather = "Зима/Сніг"
        )

        val prompt = builder.buildPrompt(input)
        assertTrue(prompt.contains("Зима/Сніг"))
        assertFalse(prompt.contains("А".repeat(201)))
        assertTrue(prompt.contains("А".repeat(200)))
    }

    @Test
    fun repository_parsesValidStructuredJson_andSavesToDao() = runTest {
        val validJson = """
            {
              "isValidRequest": true,
              "rejectionReason": null,
              "tourTitle": "Зимовий штурм Свидовця",
              "recommendedVehicle": "Toyota Land Cruiser 80 (35 MT tires)",
              "weatherAdvice": "Використовуйте ланцюги протиковзання та знижений тиск у шинах.",
              "safetyTips": "Тримати дистанцію 30 метрів на спусках.",
              "schedule": ["09:00 - Виїзд", "11:30 - Хребет", "14:00 - Повернення"]
            }
        """.trimIndent()

        val dao = InMemoryTourDao()
        val fakeAi = FakeAiDataSource(Result.success(validJson))
        val repo = TourRepository(
            tourDao = dao,
            promptBuilder = TourPromptBuilder(),
            aiDataSource = fakeAi,
            responseMapper = TourResponseMapper()
        )

        val result = repo.generateTour(
            TourRequestInput(5, "HARD", "Драгобрат", "Хребет", "UA", "Зима/Сніг")
        )

        assertTrue(result.isSuccess)
        val tour = result.getOrThrow()
        assertTrue(tour.isValidRequest)
        assertEquals("Зимовий штурм Свидовця", tour.tourTitle)
        assertEquals(3, tour.schedule.size)
        assertEquals(1, dao.items.size)
    }

    @Test
    fun repository_handlesUnrealisticRequestAsNoSuitableResult_withoutSavingToDao() = runTest {
        val rejectionJson = """
            {
              "isValidRequest": false,
              "rejectionReason": "Підйом на Еверест за 1 годину на джипі під час урагану неможливий.",
              "tourTitle": "",
              "recommendedVehicle": "",
              "weatherAdvice": "",
              "safetyTips": "",
              "schedule": []
            }
        """.trimIndent()

        val dao = InMemoryTourDao()
        val fakeAi = FakeAiDataSource(Result.success(rejectionJson))
        val repo = TourRepository(
            tourDao = dao,
            promptBuilder = TourPromptBuilder(),
            aiDataSource = fakeAi,
            responseMapper = TourResponseMapper()
        )

        val result = repo.generateTour(
            TourRequestInput(1, "HARD", "Еверест в ураган", "Прямий підйом", "UA", "Зима/Сніг")
        )

        assertTrue(result.isSuccess)
        val tour = result.getOrThrow()
        assertFalse(tour.isValidRequest)
        assertTrue(tour.rejectionReason!!.contains("Еверест"))
        assertEquals(0, dao.items.size)
    }

    @Test
    fun repository_propagatesTimeoutAndConfigErrors() = runTest {
        val dao = InMemoryTourDao()
        val fakeAi = FakeAiDataSource(Result.failure(AppError.TimeoutError))
        val repo = TourRepository(tourDao = dao, aiDataSource = fakeAi)

        val timeoutRes = repo.generateTour(
            TourRequestInput(4, "MEDIUM", "Говерла", "Прямий", "UA", "Сухо")
        )
        assertEquals(AppError.TimeoutError, timeoutRes.exceptionOrNull())

        fakeAi.nextResult = Result.failure(AppError.ConfigError)
        val configRes = repo.generateTour(
            TourRequestInput(4, "MEDIUM", "Говерла", "Прямий", "UA", "Сухо")
        )
        assertEquals(AppError.ConfigError, configRes.exceptionOrNull())
    }
}