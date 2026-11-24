package com.example.jeeptours2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jeeptours2.data.ai.TourPromptBuilder
import com.example.jeeptours2.domain.model.AppError
import com.example.jeeptours2.domain.model.TourPreset
import com.example.jeeptours2.domain.model.TourRequestInput
import com.example.jeeptours2.domain.repository.ITourRepository

// 🎨 Палітра
private val SoftSageGreen = Color(0xFF4A7C59)
private val DarkForestGreen = Color(0xFF1B3B2B)
private val DesertSand = Color(0xFFF4EAD4)
private val LightSage = Color(0xFFE8F5E9)

private val popularPresets = listOf(
    TourPreset(
        id = "hoverla",
        titleUa = "Говерла",
        titleEn = "Hoverla",
        subtitleUa = "Найвища точка України • Панорама Чорногори",
        subtitleEn = "Highest peak of Ukraine • Chornohora panorama",
        defaultHours = 4,
        defaultDifficulty = "MEDIUM",
        defaultTrackIndex = 0,
        priceUah = 2400,
        emoji = "🏔️"
    ),
    TourPreset(
        id = "shpytsi",
        titleUa = "Гора Шпиці",
        titleEn = "Mount Shpytsi",
        subtitleUa = "Скелясті вежі та високогірні хребти",
        subtitleEn = "Rocky spires and high-altitude ridges",
        defaultHours = 6,
        defaultDifficulty = "HARD",
        defaultTrackIndex = 2,
        priceUah = 3200,
        emoji = "🪨"
    ),
    TourPreset(
        id = "synevyr",
        titleUa = "Озеро Синевир та водоспад Шипіт",
        titleEn = "Synevyr Lake & Shypit Waterfall",
        subtitleUa = "Лісові броди, гірські озера та водоспади",
        subtitleEn = "Forest fords, alpine lakes and waterfalls",
        defaultHours = 5,
        defaultDifficulty = "EASY",
        defaultTrackIndex = 1,
        priceUah = 2800,
        emoji = "🌊"
    ),
    TourPreset(
        id = "dragobrat",
        titleUa = "Хребет Свидовець / Драгобрат",
        titleEn = "Svydovets Ridge / Dragobrat",
        subtitleUa = "Екстремальний підйом та альпійські луки",
        subtitleEn = "Extreme ascent and alpine meadows",
        defaultHours = 5,
        defaultDifficulty = "HARD",
        defaultTrackIndex = 2,
        priceUah = 3000,
        emoji = "🚙"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(repository: ITourRepository) {
    // ViewModel зберігає стан генерації, помилок, бронювання та історії при повороті екрана / фоні
    val tourViewModel: TourViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TourViewModel(repository) as T
            }
        }
    )

    val uiState by tourViewModel.uiState.collectAsState()
    val savedTours by tourViewModel.savedTours.collectAsState()
    val bookingMessage by tourViewModel.bookingResult.collectAsState()
    val isBookingLoading by tourViewModel.isBookingLoading.collectAsState()

    // rememberSaveable зберігає поля форми при зміні орієнтації екрана
    var isBuilderOpen by rememberSaveable { mutableStateOf(false) }
    var language by rememberSaveable { mutableStateOf("UA") }
    val isEnglish = language == "EN"

    var durationHours by rememberSaveable { mutableStateOf("4") }
    var difficulty by rememberSaveable { mutableStateOf("MEDIUM") }
    var preferences by rememberSaveable { mutableStateOf("Говерла") }
    var selectedRouteIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedWeatherIndex by rememberSaveable { mutableIntStateOf(0) }

    var userName by rememberSaveable { mutableStateOf("") }
    var userPhone by rememberSaveable { mutableStateOf("") }
    var bookingDate by rememberSaveable { mutableStateOf("2026-05-01") }

    val weatherOptions = if (isEnglish) {
        listOf("Dry / Clear", "Rain / Mud", "Winter / Snow")
    } else {
        listOf("Сухо", "Дощ/Бруд", "Зима/Сніг")
    }
    val seasonOrWeather = weatherOptions[selectedWeatherIndex]

    val routeOptions = if (isEnglish) {
        listOf(
            "Direct Climb / Fast Track",
            "Route via Water Bodies / Lakes / Waterfalls",
            "Panoramic Route via Ridge & Viewpoints"
        )
    } else {
        listOf(
            "Прямий підйом / Швидкий трек",
            "Маршрут через водойми / Озера / Водоспади",
            "Панорамний маршрут через хребет та оглядові місця"
        )
    }
    val routeOptionType = routeOptions[selectedRouteIndex]

    fun triggerGeneration() {
        val hours = durationHours.toIntOrNull() ?: 4
        val input = TourRequestInput(
            durationHours = hours,
            difficulty = difficulty,
            preferences = preferences,
            routeOptionType = routeOptionType,
            language = language,
            seasonOrWeather = seasonOrWeather
        )
        tourViewModel.generateTour(input)
    }

    fun openPresetInBuilder(preset: TourPreset) {
        preferences = if (isEnglish) preset.titleEn else preset.titleUa
        durationHours = preset.defaultHours.toString()
        difficulty = preset.defaultDifficulty
        selectedRouteIndex = preset.defaultTrackIndex
        selectedWeatherIndex = 0
        tourViewModel.resetState()
        isBuilderOpen = true
    }

    fun openCustomBuilder() {
        preferences = if (isEnglish) "Hoverla" else "Говерла"
        durationHours = "4"
        difficulty = "MEDIUM"
        selectedRouteIndex = 0
        selectedWeatherIndex = 0
        tourViewModel.resetState()
        isBuilderOpen = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isBuilderOpen) {
                            TextButton(
                                onClick = { isBuilderOpen = false },
                                contentPadding = PaddingValues(end = 8.dp)
                            ) {
                                Text(
                                    text = if (isEnglish) "⬅️ Back" else "⬅️ Назад",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        } else {
                            Text(text = "🚙💨 ", fontSize = 22.sp)
                        }
                        Text(
                            text = "JeepTours 4x4",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier.padding(end = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = language == "UA",
                            onClick = {
                                language = "UA"
                                if (preferences == "Hoverla") preferences = "Говерла"
                            },
                            label = { Text("UA", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DarkForestGreen,
                                selectedLabelColor = Color.White,
                                labelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = language == "EN",
                            onClick = {
                                language = "EN"
                                if (preferences == "Говерла") preferences = "Hoverla"
                            },
                            label = { Text("EN", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DarkForestGreen,
                                selectedLabelColor = Color.White,
                                labelColor = Color.White
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SoftSageGreen)
            )
        },
        bottomBar = {
            if (!isBuilderOpen) {
                Surface(
                    color = Color.White,
                    shadowElevation = 12.dp,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = { openCustomBuilder() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = if (isEnglish) "🧭 Where to? Build Custom Route" else "🧭 Куди їдемо? Створити власний маршрут",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DesertSand
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (!isBuilderOpen) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DesertSand)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
            ) {
                item {
                    Column {
                        Text(
                            text = if (isEnglish) "Choose Your Expedition" else "Оберіть вашу експедицію",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = DarkForestGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isEnglish) "Select a popular route below or customize your own off-road trip"
                            else "Оберіть готовий напрямок нижче або налаштуйте власний джип-тур",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkForestGreen.copy(alpha = 0.75f)
                        )
                    }
                }

                items(popularPresets) { preset ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { openPresetInBuilder(preset) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = LightSage,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.size(46.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(text = preset.emoji, fontSize = 24.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = if (isEnglish) preset.titleEn else preset.titleUa,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkForestGreen
                                        )
                                        Text(
                                            text = if (isEnglish) "⏱️ ${preset.defaultHours} hrs • ${preset.defaultDifficulty}"
                                            else "⏱️ ${preset.defaultHours} год • ${preset.defaultDifficulty}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftSageGreen,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Surface(
                                    color = LightSage,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "${preset.priceUah} ₴",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        fontWeight = FontWeight.Bold,
                                        color = DarkForestGreen,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (isEnglish) preset.subtitleEn else preset.subtitleUa,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { openPresetInBuilder(preset) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = SoftSageGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (isEnglish) "Select & Customize →" else "Обрати та налаштувати →",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        } else {
            val isLoading = uiState is TourUiState.Loading

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DesertSand)
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // PARAMETERS FORM
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = if (isEnglish) "⚙️ AI Route Parameters" else "⚙️ Параметри AI-маршруту",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkForestGreen
                            )

                            OutlinedTextField(
                                value = durationHours,
                                onValueChange = { durationHours = it },
                                label = { Text(if (isEnglish) "⏱️ Duration (1..24 hours)" else "⏱️ Тривалість (1..24 год)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Text(text = if (isEnglish) "🏔️ Difficulty:" else "🏔️ Складність:", fontWeight = FontWeight.Medium)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("EASY", "MEDIUM", "HARD").forEach { level ->
                                    FilterChip(
                                        selected = difficulty == level,
                                        onClick = { difficulty = level },
                                        label = { Text(level) }
                                    )
                                }
                            }

                            Text(text = if (isEnglish) "🌦️ Season / Weather:" else "🌦️ Сезон / Погода:", fontWeight = FontWeight.Medium)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                weatherOptions.forEachIndexed { index, weatherLabel ->
                                    FilterChip(
                                        selected = selectedWeatherIndex == index,
                                        onClick = { selectedWeatherIndex = index },
                                        label = { Text(weatherLabel, fontSize = 12.sp) }
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = preferences,
                                onValueChange = { preferences = it.take(TourPromptBuilder.MAX_PREFERENCES_LENGTH) },
                                label = {
                                    Text(
                                        if (isEnglish) "🗺️ Location & wishes (${preferences.length}/200)"
                                        else "🗺️ Локація та побажання (${preferences.length}/200)"
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Text(
                                text = if (isEnglish) "🥾 Route Track Variant:" else "🥾 Варіант треку за напрямком:",
                                fontWeight = FontWeight.Medium
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                routeOptions.forEachIndexed { index, option ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        RadioButton(
                                            selected = (selectedRouteIndex == index),
                                            onClick = { selectedRouteIndex = index }
                                        )
                                        Text(
                                            text = option,
                                            fontSize = 13.sp,
                                            color = DarkForestGreen,
                                            modifier = Modifier.padding(start = 2.dp)
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = { triggerGeneration() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = SoftSageGreen),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
                                } else {
                                    Text(
                                        text = if (isEnglish) "✨ Generate AI Route" else "✨ Згенерувати AI-Маршрут",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // VIEWMODEL UI STATES: ERROR / SUCCESS / NO SUITABLE RESULT
                when (val currentState = uiState) {
                    is TourUiState.Error -> {
                        val error = currentState.error
                        val isRetryableError = error !is AppError.ConfigError && error !is AppError.InvalidInputError
                        val errorText = when (error) {
                            is AppError.TimeoutError -> if (isEnglish) "AI took too long to respond (25s timeout). Please retry." else "AI не встиг відповісти за 25 секунд. Спробуйте ще раз."
                            is AppError.NetworkError -> if (isEnglish) "Network error. Check your internet connection." else "Немає з'єднання з мережею. Перевірте інтернет."
                            is AppError.ConfigError -> if (isEnglish) "Firebase / App Check configuration error. Check debug token." else "Помилка налаштування Firebase / App Check. Перевірте debug token."
                            is AppError.InvalidInputError -> if (isEnglish) "Check duration (1..24h) and location." else "Перевірте тривалість (1..24 год) та локацію."
                            is AppError.ServerError -> if (isEnglish) "AI service error. Try again later." else "Помилка AI-сервісу. Спробуйте пізніше."
                            else -> if (isEnglish) "Unexpected error occurred." else "Виникла непередбачена помилка."
                        }

                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "❌ $errorText",
                                        color = Color(0xFFC62828),
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (isRetryableError) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        OutlinedButton(
                                            onClick = { tourViewModel.retryLastTourGeneration() },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828))
                                        ) {
                                            Text(if (isEnglish) "🔄 Retry Request" else "🔄 Повторити запит")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is TourUiState.Success -> {
                        val response = currentState.result
                        if (!response.isValidRequest) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = if (isEnglish) "⚠️ No Suitable Route Found" else "⚠️ Немає придатного результату",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE65100)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = response.rejectionReason.orEmpty(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF4E342E)
                                        )
                                    }
                                }
                            }
                        } else {
                            val hoursCount = durationHours.toIntOrNull() ?: 4

                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = LightSage),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "🎯 ${response.tourTitle}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkForestGreen
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "🚜 ${response.recommendedVehicle}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (response.weatherAdvice.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "🌦️ ${response.weatherAdvice}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = DarkForestGreen
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "🛡️ ${response.safetyTips}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF33691E)
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))
                                        HorizontalDivider(color = SoftSageGreen.copy(alpha = 0.4f))
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = if (isEnglish) "⏱️ AI Schedule ($hoursCount hrs):" else "⏱️ AI-графік маршруту ($hoursCount год):",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkForestGreen
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))

                                        response.schedule.forEach { step ->
                                            Row(
                                                modifier = Modifier.padding(vertical = 3.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Text(text = "📌 ", fontSize = 14.sp)
                                                Text(text = step, style = MaterialTheme.typography.bodyMedium, color = DarkForestGreen)
                                            }
                                        }
                                    }
                                }
                            }

                            // BOOKING CARD (ЗЕЛЕНА ТЕМА)
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFDCEEE0)),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = if (isEnglish) "📅 Book This Tour" else "📅 Забронювати цей тур",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkForestGreen
                                        )

                                        OutlinedTextField(
                                            value = userName,
                                            onValueChange = { userName = it },
                                            label = { Text(if (isEnglish) "Your Name" else "Ваше ім'я") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            )
                                        )

                                        OutlinedTextField(
                                            value = userPhone,
                                            onValueChange = { userPhone = it },
                                            label = { Text(if (isEnglish) "Phone Number" else "Номер телефону") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            )
                                        )

                                        OutlinedTextField(
                                            value = bookingDate,
                                            onValueChange = { bookingDate = it },
                                            label = { Text(if (isEnglish) "Date (YYYY-MM-DD)" else "Дата (РРРР-ММ-ДД)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            )
                                        )

                                        Button(
                                            onClick = {
                                                tourViewModel.bookTour(
                                                    tourTitle = response.tourTitle,
                                                    bookingDate = bookingDate,
                                                    name = userName,
                                                    phone = userPhone,
                                                    lang = language
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = SoftSageGreen,
                                                disabledContainerColor = SoftSageGreen.copy(alpha = 0.55f)
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            enabled = !isBookingLoading && userName.isNotBlank() && userPhone.isNotBlank()
                                        ) {
                                            Text(
                                                text = if (isEnglish) "✅ Confirm Booking" else "✅ Підтвердити бронювання",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        bookingMessage?.let { msg ->
                                            Text(
                                                text = "✅ $msg",
                                                color = DarkForestGreen,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    else -> Unit
                }

                // SAVED TOURS HISTORY (ROOM DB)
                if (savedTours.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isEnglish) "📜 Saved Tours History (Room)" else "📜 Історія збережених турів (Room)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkForestGreen
                            )
                            TextButton(onClick = { tourViewModel.clearHistory() }) {
                                Text(
                                    text = if (isEnglish) "Clear" else "Очистити",
                                    color = Color(0xFFC62828),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    items(savedTours) { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.tourTitle,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkForestGreen,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "🚜 ${item.recommendedVehicle}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.DarkGray
                                    )
                                    Text(
                                        text = "⏱️ ${item.durationHours} год • ${item.difficulty}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftSageGreen,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                TextButton(onClick = { tourViewModel.deleteTour(item.id) }) {
                                    Text("🗑️", fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}