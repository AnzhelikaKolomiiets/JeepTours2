package com.example.jeeptours2.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tour_table")
data class TourEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tourTitle: String,
    val description: String,
    val routeType: String,
    val difficulty: String,
    val durationHours: Int,
    val createdAt: Long = System.currentTimeMillis()
)