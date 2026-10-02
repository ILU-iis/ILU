package com.iluiis.app

object CrisisDetector {

    private val CRISIS_KEYWORDS = listOf(
        "bunuh diri",
        "ingin mati",
        "mau mati",
        "pengen mati",
        "pengen mati aja",
        "gantung diri",
        "potong urat",
        "minum racun",
        "self harm",
        "selfharm",
        "menyakiti diri",
        "melukai diri",
        "nggak mau hidup",
        "ga mau hidup",
        "gak mau hidup",
        "tidak mau hidup",
        "capek hidup",
        "lelah hidup",
        "pengen hilang",
        "mau hilang aja",
        "akhiri hidup",
        "mengakhiri hidup",
        "putus asa hidup"
    )

    fun isCrisis(message: String): Boolean {
        val lowerMessage = message.lowercase().trim()
        return CRISIS_KEYWORDS.any { lowerMessage.contains(it) }
    }

    fun getTriggerKeyword(message: String): String? {
        val lowerMessage = message.lowercase().trim()
        return CRISIS_KEYWORDS.firstOrNull { lowerMessage.contains(it) }
    }

    const val CRISIS_RESPONSE =
        "Aku sayang sama kamu dan aku peduli banget. Tapi untuk hal ini, kamu perlu ngobrol sama orang yang lebih ahli ya. Coba hubungi psikiater di nomor +62 877 0324 4632. Mereka bisa bantu kamu lebih dari aku. 💜"
}
