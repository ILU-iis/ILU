package com.example.ilu

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object PeriodManager {

    private const val TAG = "PeriodManager"
    private const val PREFS_NAME = "ilu_period_prefs"
    private const val KEY_START_DATE = "period_start_date"
    private const val KEY_PERIOD_COUNTER = "period_counter"
    private const val COLLECTION_PERIODS = "periods"
    private const val PERIOD_DAYS_LIMIT = 15

    fun getCurrentPeriodId(context: Context, username: String?): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())

        var startDateStr = prefs.getString(KEY_START_DATE, null)
        var counter = prefs.getInt(KEY_PERIOD_COUNTER, 1)

        if (startDateStr == null) {
            // Periode pertama baru dimulai
            startDateStr = todayStr
            counter = 1
            prefs.edit()
                .putString(KEY_START_DATE, startDateStr)
                .putInt(KEY_PERIOD_COUNTER, counter)
                .apply()

            val periodId = "periode_$counter"
            syncPeriodToFirestore(username, periodId, startDateStr)
            return periodId
        }

        // Cek selisih hari dari period_start_date
        try {
            val startDate = dateFormat.parse(startDateStr)
            val today = dateFormat.parse(todayStr)

            if (startDate != null && today != null) {
                val diffInMillis = today.time - startDate.time
                val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

                if (diffInDays >= PERIOD_DAYS_LIMIT) {
                    // Waktunya ganti periode baru (setiap 15 hari)
                    counter += 1
                    startDateStr = todayStr

                    prefs.edit()
                        .putString(KEY_START_DATE, startDateStr)
                        .putInt(KEY_PERIOD_COUNTER, counter)
                        .apply()

                    val periodId = "periode_$counter"
                    syncPeriodToFirestore(username, periodId, startDateStr)
                    Log.d(TAG, "Siklus 15 hari telah berlalu. Memulai $periodId sejak $startDateStr")
                    return periodId
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal menghitung selisih hari periode", e)
        }

        return "periode_$counter"
    }

    private fun syncPeriodToFirestore(username: String?, periodId: String, startDate: String) {
        val data = hashMapOf(
            "username" to (username ?: "Teman"),
            "periodId" to periodId,
            "startDate" to startDate,
            "timestamp" to System.currentTimeMillis()
        )

        try {
            val db = FirebaseFirestore.getInstance()
            db.collection(COLLECTION_PERIODS)
                .add(data)
                .addOnSuccessListener { ref ->
                    Log.d(TAG, "Berhasil sync metadata $periodId ke Firestore ID: ${ref.id}")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Gagal sync metadata periode ke Firestore", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception saat sync metadata periode ke Firestore", e)
        }
    }
}
