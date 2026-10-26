# Architecture: JeepTours2

## Загальна схема
## Мобільна частина (Android Native)
- **Мова:** Kotlin
- **UI Framework:** Jetpack Compose + Material Design 3
- **State Management:** MVVM (`UiState` sealed class: `Idle`, `Loading`, `Success`, `Error`)
- **Мережа:** Retrofit2 + OkHttp + Gson Converter
- **Асинхронність:** Kotlin Coroutines + Flow
- **Network Base URL:** `http://10.0.2.2:8000/` (для Android-емулятора)

## Серверна частина (Backend API)
- **Стек:** Python 3.10+ / FastAPI / Uvicorn
- **Валідація:** Pydantic models
- **CORS / Host:** `0.0.0.0:8000`

## Потік даних (Data Flow)
1. Користувач заповнює форму в Compose UI та натискає «Згенерувати маршрут».
2. `MainViewModel` змінює стан на `UiState.Loading` і викликає `Repository`.
3. `Repository` надсилає POST-запит через Retrofit до FastAPI.
4. FastAPI приймає Pydantic-модель, аналізує складність і вподобання, формує відповідь `TourResponse`.
5. Retrofit парсить JSON у Kotlin-модель, `MainViewModel` оновлює `UiState.Success(data)`.
6. UI відображає згенеровану картку туру. При помилці передається `UiState.Error(msg)`.