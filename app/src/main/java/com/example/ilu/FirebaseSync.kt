package com.iluiis.app

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseSync {

    private const val TAG = "FirebaseSync"
    private const val COLLECTION_GRATITUDE = "gratitudes"
    private const val COLLECTION_CHATS = "chats"

    fun syncGratitude(
        date: String,
        good1: String,
        good2: String,
        good3: String,
        username: String?,
        periodId: String? = null
    ) {
        val timestamp = System.currentTimeMillis()
        val data = hashMapOf(
            "date" to date,
            "username" to (username ?: "Teman"),
            "good1" to good1,
            "good2" to good2,
            "good3" to good3,
            "periodId" to (periodId ?: "periode_1"),
            "timestamp" to timestamp
        )

        try {
            val db = FirebaseFirestore.getInstance()
            db.collection(COLLECTION_GRATITUDE)
                .add(data)
                .addOnSuccessListener { ref ->
                    Log.d(TAG, "Berhasil sync Gratitude ke Firebase Firestore ID: ${ref.id}")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Gagal sync Gratitude ke Firebase Firestore", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception saat sync Gratitude ke Firestore", e)
        }
    }

    fun syncChatMessage(
        date: String,
        text: String,
        isUser: Boolean,
        username: String?,
        periodId: String? = null
    ) {
        val timestamp = System.currentTimeMillis()
        val data = hashMapOf(
            "date" to date,
            "username" to (username ?: "Teman"),
            "text" to text,
            "isUser" to isUser,
            "sender" to (if (isUser) "user" else "ILU"),
            "periodId" to (periodId ?: "periode_1"),
            "timestamp" to timestamp
        )

        try {
            val db = FirebaseFirestore.getInstance()
            db.collection(COLLECTION_CHATS)
                .add(data)
                .addOnSuccessListener { ref ->
                    Log.d(TAG, "Berhasil sync ChatMessage ke Firebase Firestore ID: ${ref.id}")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Gagal sync ChatMessage ke Firebase Firestore", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception saat sync ChatMessage ke Firestore", e)
        }
    }
}
