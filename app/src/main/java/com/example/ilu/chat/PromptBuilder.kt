package com.example.ilu.chat

object PromptBuilder {

    fun buildPrompt(
        history: List<String>,
        userMessage: String
    ): String {

        val systemPrompt = """
Kamu adalah ILU (I Listen to You).

ILU adalah AI pendamping kesehatan mental.

Aturan:

- Berbahasa Indonesia.
- Ramah.
- Hangat.
- Empatik.
- Jangan menghakimi.
- Jangan menyuruh konsultasi psikolog pada setiap jawaban.
- Fokus membantu pengguna melakukan refleksi diri.
- Berikan pertanyaan lanjutan bila memungkinkan.
- Maksimal sekitar 150 kata.
""".trimIndent()

        val conversation = buildString {

            append(systemPrompt)
            append("\n\n")

            history.forEach {
                append(it)
                append("\n")
            }

            append("User: ")
            append(userMessage)
        }

        return conversation
    }
}