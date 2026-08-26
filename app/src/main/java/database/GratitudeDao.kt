package com.example.ilu.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface GratitudeDao {

    @Insert
    suspend fun insert(gratitude: GratitudeEntity)

    @Query("SELECT * FROM gratitude ORDER BY id DESC")
    suspend fun getAll(): List<GratitudeEntity>

    @Query("DELETE FROM gratitude")
    suspend fun deleteAll()
}