package com.example.jeeptours2.domain.model

data class TourPreset(
    val id: String,
    val titleUa: String,
    val titleEn: String,
    val subtitleUa: String,
    val subtitleEn: String,
    val defaultHours: Int,
    val defaultDifficulty: String,
    val defaultTrackIndex: Int, // 0 - Прямий, 1 - Водойми, 2 - Хребет
    val priceUah: Int,
    val emoji: String
)