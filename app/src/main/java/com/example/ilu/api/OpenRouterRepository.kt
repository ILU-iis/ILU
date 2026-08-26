package com.example.ilu.api

import android.util.Log
import kotlinx.coroutines.delay

object OpenRouterRepository {

    private const val TAG = "OpenRouterRepo"

    private fun isValidResponse(text: String): Boolean {
        // Cukup periksa apakah teks tidak kosong atau hanya berisi whitespace
        return text.isNotBlank() && text.trim().length >= 2
    }

    suspend fun sendMessage(
        userMessage: String,
        history: List<ChatMessage> = emptyList()
    ): String {

        var retry = 0

        while (retry < 3) {

            try {

                val messages = mutableListOf<ChatMessage>()

                // System Prompt
                messages.add(
                    ChatMessage(
                        role = "system",
                        content = Constants.SYSTEM_PROMPT
                    )
                )

                // History (Maksimal 10 pesan terakhir agar context tidak overflow)
                val limitedHistory = if (history.size > 10) history.takeLast(10) else history
                messages.addAll(limitedHistory)

                // Pesan user terbaru
                messages.add(
                    ChatMessage(
                        role = "user",
                        content = userMessage
                    )
                )

                val request = ChatRequest(
                    model = Constants.MODEL,
                    messages = messages
                )

                Log.d(TAG, "Mengirim pesan ke model: ${Constants.MODEL}")
                val response = RetrofitClient.api.sendMessage(
                    authorization = "Bearer ${Constants.API_KEY}",
                    request = request
                )

                if (response.isSuccessful) {
                    val content = response.body()
                        ?.choices
                        ?.firstOrNull()
                        ?.message
                        ?.content
                        ?.trim()

                    Log.d(TAG, "Respons berhasil diterima: $content")

                    if (content != null && isValidResponse(content)) {
                        return content
                    } else {
                        Log.w(TAG, "Respons tidak valid (kosong atau terlalu pendek): $content")
                        retry++
                        if (retry >= 3) {
                            return "Maaf ya, aku lagi sedikit bingung nih. Bisa tolong ceritain lagi? 😊"
                        }
                        delay(1000)
                    }

                } else {
                    val errorCode = response.code()
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Respons API Gagal (HTTP $errorCode): $errorBody")

                    retry++

                    if (retry >= 3) {
                        return "Aduh, sepertinya koneksiku lagi agak lambat nih. Boleh tolong ulangi lagi pesanmu? 🌸"
                    }

                    delay(1000)

                }

            } catch (e: Exception) {
                Log.e(TAG, "Terjadi exception saat memanggil API (Percobaan ${retry + 1})", e)
                retry++

                if (retry >= 3) {
                    return "Aduh, sepertinya jaringan kita lagi sedikit terganggu nih. Coba kirim pesan lagi ya, aku di sini kok! 🌸"
                }

                delay(1000)

            }

        }

        return "Aduh, sepertinya jaringan kita lagi sedikit terganggu nih. Coba kirim pesan lagi ya, aku di sini kok! 🌸"

    }

}