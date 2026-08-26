package com.example.ilu

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CrisisLogger {

    private const val TAG = "CrisisLogger"
    private const val COLLECTION_NAME = "crisis_logs"

    fun logCrisisEvent(
        detectedBy: String,
        triggerKeyword: String? = null,
        username: String? = null,
        periodId: String? = null
    ) {
        val dateString = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.getDefault()
        ).format(Date())

        val logData = hashMapOf(
            "timestamp" to System.currentTimeMillis(),
            "date" to dateString,
            "username" to (username ?: "Teman"),
            "detectedBy" to detectedBy,
            "triggerKeyword" to (triggerKeyword ?: "N/A"),
            "periodId" to (periodId ?: "periode_1"),
            "appVersion" to "1.0",
            "reviewed" to false
        )

        try {
            val db = FirebaseFirestore.getInstance()
            db.collection(COLLECTION_NAME)
                .add(logData)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "Log krisis berhasil tersimpan di Firestore ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Gagal menyimpan log krisis ke Firestore", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception saat menghubungi Firestore Database", e)
        }
    }
}
