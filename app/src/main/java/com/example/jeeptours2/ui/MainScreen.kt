package com.example.jeeptours2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jeeptours2.data.TourRepository
import com.example.jeeptours2.data.TourResponse
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(repository: TourRepository = TourRepository()) {
    val coroutineScope = rememberCoroutineScope()

    // Стан форми
    var durationHours by remember { mutableStateOf("4") }
    var difficulty by remember { mutableStateOf("MEDIUM") }
    var preferences by remember { mutableStateOf("Говерла") }
    var language by remember { mutableStateOf("UA") }

    // Стан запиту
    var isLoading by remember { mutableStateOf(false) }
    var tourResponse by remember { mutableStateOf<TourResponse?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🚘", fontSize = 22.sp, modifier = Modifier.padding(end = 8.dp))
                        Text(
                            text = if (language == "UA") "JeepTours — Гірські Пригоди" else "JeepTours — Mountain Adventures",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- СЕКЦІЯ НАЛАШТУВАНЬ МАРШРУТУ ---
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "⛰️", fontSize = 20.sp, modifier = Modifier.padding(end = 6.dp))
                            Text(
                                text = if (language == "UA") "Параметри туру" else "Tour Parameters",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Вибір мови
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(if (language == "UA") "Мова:" else "Language:")
                            FilterChip(
                                selected = language == "UA",
                                onClick = { language = "UA" },
                                label = { Text("UA") }
                            )
                            FilterChip(
                                selected = language == "EN",
                                onClick = { language = "EN" },
                                label = { Text("EN") }
                            )
                        }

                        // Тривалість
                        OutlinedTextField(
                            value = durationHours,
                            onValueChange = { durationHours = it },
                            label = { Text(if (language == "UA") "Тривалість (годин)" else "Duration (hours)") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Складність
                        Text(if (language == "UA") "Складність:" else "Difficulty:")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("EASY", "MEDIUM", "HARD").forEach { level ->
                                FilterChip(
                                    selected = difficulty == level,
                                    onClick = { difficulty = level },
                                    label = { Text(level) }
                                )
                            }
                        }

                        // Побажання
                        OutlinedTextField(
                            value = preferences,
                            onValueChange = { preferences = it },
                            label = { Text(if (language == "UA") "Локація / Побажання" else "Location / Preferences") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Кнопка
                        Button(
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                coroutineScope.launch {
                                    val hours = durationHours.toIntOrNull() ?: 4
                                    val result = repository.generateTour(
                                        durationHours = hours,
                                        difficulty = difficulty,
                                        preferences = preferences,
                                        language = language
                                    )
                                    result.onSuccess { response ->
                                        tourResponse = response
                                        isLoading = false
                                    }.onFailure { error ->
                                        errorMessage = error.localizedMessage ?: "Помилка з'єднання"
                                        isLoading = false
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(text = "🚘", fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
                                Text(if (language == "UA") "Згенерувати маршрут" else "Generate Route")
                            }
                        }
                    }
                }
            }

            // --- СЕКЦІЯ ПОМИЛКИ ---
            errorMessage?.let { error ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // --- РЕЗУЛЬТАТ ВІД БЕКЕНДУ ---
            tourResponse?.let { response ->
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = response.tourTitle,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🚘", fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
                                Text(
                                    text = response.recommendedVehicle,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.Top) {
                                Text(text = "⚠️", fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
                                Text(
                                    text = response.safetyTips,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // --- СПИСОК МАРШРУТІВ ---
                items(response.routes) { route ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = route.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = route.description,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            if (route.schedule.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "⏱️", fontSize = 16.sp, modifier = Modifier.padding(end = 6.dp))
                                    Text(
                                        text = if (language == "UA") "Розклад:" else "Schedule:",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                route.schedule.forEach { time ->
                                    Text(
                                        text = "• $time",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}