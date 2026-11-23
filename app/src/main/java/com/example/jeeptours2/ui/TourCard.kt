package com.example.jeeptours2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jeeptours2.data.TourResponse

private val DarkForestGreen = Color(0xFF1B3B2B)
private val LightSage = Color(0xFFE8F5E9)

@Composable
fun TourCard(
    response: TourResponse,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = LightSage),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🎯 ${response.tourTitle}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DarkForestGreen
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "🚜 ${response.recommendedVehicle}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "🛡️ ${response.safetyTips}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF33691E)
            )

            // 📍 ЗАМІСТЬ response.routes ВИКОРИСТОВУЄМО schedule:
            response.schedule?.let { steps ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "⏱️ Погодинний розклад:",
                    fontWeight = FontWeight.Bold,
                    color = DarkForestGreen,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                steps.forEach { step ->
                    Text(
                        text = "📌 $step",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkForestGreen,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}