package com.example.ilu.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gratitude")
data class GratitudeEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val date: String,

    val good1: String,

    val good2: String,

    val good3: String
)