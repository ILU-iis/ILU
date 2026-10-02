package com.iluiis.app.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ChatMessageDao {

    @Insert
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("SELECT * FROM chat_messages WHERE date = :date ORDER BY timestamp ASC, id ASC")
    suspend fun getMessagesByDate(date: String): List<ChatMessageEntity>

    @Query("SELECT date FROM chat_messages ORDER BY id DESC LIMIT 1")
    suspend fun getLastMessageDate(): String?

    @Query("SELECT DISTINCT date FROM chat_messages ORDER BY date DESC")
    suspend fun getAllDates(): List<String>

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAll()
}
