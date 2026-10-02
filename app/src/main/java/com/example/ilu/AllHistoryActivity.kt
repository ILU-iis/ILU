package com.iluiis.app

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iluiis.app.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AllHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AllHistoryAdapter
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_history)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.recyclerAllHistory)
        recyclerView.layoutManager = LinearLayoutManager(this)

        database = AppDatabase.getDatabase(this)

        lifecycleScope.launch {
            val history = withContext(Dispatchers.IO) {
                database.gratitudeDao().getAll()
            }

            // Reverse to show latest first
            val reversedHistory = history.reversed()

            adapter = AllHistoryAdapter(reversedHistory)
            recyclerView.adapter = adapter
        }
    }
}
