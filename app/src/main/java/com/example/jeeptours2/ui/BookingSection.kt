package com.example.jeeptours2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BookingSection(
    tourTitle: String,
    currentLanguage: String,
    onBookClick: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("2026-10-30") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (currentLanguage == "EN") "Booking Details" else "Бронювання туру",
            style = MaterialTheme.typography.titleMedium
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(if (currentLanguage == "EN") "Your Name" else "Ваше ім'я") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(if (currentLanguage == "EN") "Phone Number" else "Номер телефону") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = selectedDate,
            onValueChange = { selectedDate = it },
            label = { Text(if (currentLanguage == "EN") "Date (YYYY-MM-DD)" else "Дата (РРРР-ММ-ДД)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { onBookClick(name, phone, selectedDate) },
            enabled = name.isNotBlank() && phone.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (currentLanguage == "EN") "Confirm Booking" else "Підтвердити бронювання")
        }
    }
}