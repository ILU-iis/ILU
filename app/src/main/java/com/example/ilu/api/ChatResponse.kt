package com.iluiis.app.api

data class ChatResponse(

    val id: String?,

    val choices: List<Choice>

)

data class Choice(

    val message: AssistantMessage

)

data class AssistantMessage(

    val role: String,

    val content: String

)
