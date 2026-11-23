# JeepTours 4x4 — AI Off-Road Route Planner

Мобільний Android-застосунок (Kotlin + Jetpack Compose) для планування позашляхових експедицій 4x4 в Українських Карпатах із використанням **Firebase AI Logic (Gemini Developer API)**.

## 1. Обраний варіант розвитку (Лабораторна робота №6)
* **Варіант А — Розширення наявної AI-функції:** перехід від статичного шаблонного генератора з Лаб. №2 до генеративної моделі **Gemini 3.8 Flash** через Firebase AI Logic із контрактом JSON-відповіді, перевіркою фізичної реалістичності запиту, обробкою всіх станів UI та таймаутами.
* **Місце виконання AI:** Зовнішній сервіс Google (Gemini Developer API через клієнтський SDK com.google.firebase:firebase-ai без власного backend).

## 2. Архітектура AI-компонента та потік даних
1. **Підготовка даних (data/ai/TourPromptBuilder.kt):** санітизація тексту (до 200 символів), нормалізація тривалості (1..24 год), формування інструкцій під мову (UA/EN), складність і погоду.
2. **Виклик моделі (data/ai/FirebaseAiTourDataSource.kt):** irebase-bom:35.1.0, com.google.firebase:firebase-ai, GenerativeBackend.googleAI(), gemini-3.8-flash (та резервна gemini-3.8-flash-lite), esponseMimeType = "application/json", 	emperature = 0.4f, таймаут 25_000 мс, мапінг помилок у AppError.
3. **Перетворення результату (data/ai/TourResponseMapper.kt, domain/usecase/GenerateTourUseCase.kt):** парсинг JSON, перевірка isValidRequest та ejectionReason.
4. **Стани UI (ui/TourViewModel.kt, ui/TourUiState.kt, ui/MainScreen.kt):** Idle, Loading, Success, NoSuitableResult, Error із кнопкою повтору.

**Потік даних:**
MainScreen (UI) -> TourViewModel -> GenerateTourUseCase -> TourPromptBuilder -> FirebaseAiTourDataSource (Firebase AI + App Check) -> TourResponseMapper -> TourUiState

## 3. Налаштування та запуск
1. Відкрийте проєкт у **Android Studio**.
2. У **Firebase Console** створіть проєкт, додайте Android-додаток com.example.jeeptours2, завантажте google-services.json у папку pp/ (файл додано до .gitignore і не комітиться в репозиторій).
3. Увімкніть **AI Logic -> Gemini Developer API** (тариф Spark).
4. Увімкніть **App Check**: у debug-збірці використовується DebugAppCheckProviderFactory; скопіюйте Firebase App Check debug token з Logcat і зареєструйте його в консолі Firebase (*Manage debug tokens*).
5. Запустіть застосунок (Run 'app') або тести (./gradlew testDebugUnitTest).

## 4. Безпека даних
* У сервіс Gemini передаються лише знеособлені параметри поїздки (тривалість, складність, погода, мова та локація). Персональні дані користувача не передаються.
* google-services.json, API-ключі та debug-токени виключені з Git.
