package com.example.jeeptours2.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TourDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTour(tour: TourEntity): Long

    @Query("SELECT * FROM tour_table ORDER BY createdAt DESC")
    fun getAllTours(): List<TourEntity>

    @Query("DELETE FROM tour_table WHERE id = :id")
    fun deleteTourById(id: Long)

    @Query("DELETE FROM tour_table")
    fun clearAllToursCount()
}