package com.iluiis.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iluiis.app.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecapActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecapAdapter
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recap)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Mascot Bobbing Animation (Floating up & down smoothly)
        findViewById<android.widget.ImageView>(R.id.ivMascotRecap)?.let { mascot ->
            val bobbingAnimator = android.animation.ObjectAnimator.ofFloat(mascot, "translationY", 0f, -14f, 0f).apply {
                duration = 2500
                repeatCount = android.animation.ValueAnimator.INFINITE
                repeatMode = android.animation.ValueAnimator.RESTART
            }
            bobbingAnimator.start()
        }

        sharedPreferences = getSharedPreferences("ILU", MODE_PRIVATE)
        database = AppDatabase.getDatabase(this)

        val tvDate = findViewById<TextView>(R.id.tvDate)
        val tvTotal = findViewById<TextView>(R.id.tvTotal)
        val tvSeeAll = findViewById<TextView>(R.id.tvSeeAll)
        recyclerView = findViewById(R.id.recyclerGratitude)

        val todayFormatted = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID")).format(Date())
        tvDate.text = todayFormatted

        tvSeeAll.setOnClickListener {
            startActivity(Intent(this, AllHistoryActivity::class.java))
        }

        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            val data = withContext(Dispatchers.IO) {
                database.gratitudeDao().getAll()
            }

            tvTotal.text = "Total Gratitude Tersimpan: ${data.size} 🌻"

            val latestEntry = data.lastOrNull()
            val itemList = ArrayList<String>()
            if (latestEntry != null) {
                itemList.add(latestEntry.good1)
                itemList.add(latestEntry.good2)
                itemList.add(latestEntry.good3)
            } else {
                itemList.add("Belum ada gratitude tersimpan hari ini")
                itemList.add("Buka chat untuk bercerita bareng ILU")
                itemList.add("Semoga harimu menyenangkan! 🌻")
            }

            adapter = RecapAdapter(itemList)
            recyclerView.adapter = adapter
        }
    }
}
