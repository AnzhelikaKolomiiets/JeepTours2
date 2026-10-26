package com.example.jeeptours2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jeeptours2.data.TourRequest

@Composable
fun MainScreen(viewModel: TourViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedLanguage by remember { mutableStateOf("UA") }
    var durationText by remember { mutableStateOf("4") }
    var selectedDifficulty by remember { mutableStateOf("MEDIUM") }
    var preferencesText by remember { mutableStateOf("водоспади та оглядові майданчики") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "🚙 Джип-Тури",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = durationText,
                onValueChange = { durationText = it },
                label = { Text("Тривалість (годин)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text("Складність:", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("EASY", "MEDIUM", "HARD").forEach { diff ->
                    FilterChip(
                        selected = selectedDifficulty == diff,
                        onClick = { selectedDifficulty = diff },
                        label = { Text(diff) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = preferencesText,
                onValueChange = { preferencesText = it },
                label = { Text("Ваші вподобання") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Мова генерації маршруту:",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { selectedLanguage = "UA" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedLanguage == "UA") MaterialTheme.colorScheme.primary else Color.Gray
                    )
                ) {
                    Text("🇺🇦 UA")
                }

                Button(
                    onClick = { selectedLanguage = "EN" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedLanguage == "EN") MaterialTheme.colorScheme.primary else Color.Gray
                    )
                ) {
                    Text("🇬🇧 EN")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val duration = durationText.toIntOrNull() ?: 1
                    val request = TourRequest(
                        durationHours = duration,
                        difficulty = selectedDifficulty,
                        preferences = preferencesText,
                        language = selectedLanguage
                    )
                    viewModel.generateTour(request)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Згенерувати маршрут")
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (val state = uiState) {
                is TourUiState.Idle -> {
                    Text(
                        text = "Заповніть форму та натисніть кнопку.",
                        color = Color.Gray
                    )
                }
                is TourUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is TourUiState.Success -> {
                    val tour = state.tour
                    var selectedRouteIndex by remember { mutableIntStateOf(0) }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = tour.tourTitle,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Рекомендоване авто: ${tour.recommendedVehicle}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (!tour.routes.isNullOrEmpty()) {
                                Text(
                                    text = "Оберіть варіант маршруту:",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    tour.routes.forEachIndexed { index, _ ->
                                        FilterChip(
                                            selected = selectedRouteIndex == index,
                                            onClick = { selectedRouteIndex = index },
                                            label = { Text("Варіант ${index + 1}") }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                val activeRoute = tour.routes.getOrNull(selectedRouteIndex) ?: tour.routes.first()
                                Text(
                                    text = activeRoute.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (activeRoute.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = activeRoute.description,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                if (activeRoute.schedule.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Погодинний графік:",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                                    activeRoute.schedule.forEach { timeSlot ->
                                        Text(
                                            text = "⏱ $timeSlot",
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                }
                            } else if (!tour.itinerary.isNullOrEmpty()) {
                                Text(
                                    text = "План маршруту:",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                tour.itinerary.forEach { step ->
                                    Text(
                                        text = "• $step",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Порада з безпеки: ${tour.safetyTips}",
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                is TourUiState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Помилка: ${state.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            val duration = durationText.toIntOrNull() ?: 1
                            val request = TourRequest(
                                durationHours = duration,
                                difficulty = selectedDifficulty,
                                preferences = preferencesText,
                                language = selectedLanguage
                            )
                            viewModel.generateTour(request)
                        }) {
                            Text("Повторити спробу")
                        }
                    }
                }
            }
        }
    }
}