package com.example.ilu.api

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    @SerializedName("model")
    val model: String,

    @SerializedName("messages")
    val messages: List<ChatMessage>
)

data class ChatMessage(
    @SerializedName("role")
    val role: String,

    @SerializedName("content")
    val content: String
)