package com.iluiis.app.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val date: String,

    val text: String,

    @ColumnInfo(name = "is_user")
    val isUser: Boolean,

    val timestamp: Long = System.currentTimeMillis()
)
