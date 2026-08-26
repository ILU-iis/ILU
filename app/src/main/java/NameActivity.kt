package com.example.ilu

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class NameActivity : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_name)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Mascot Bobbing Animation (Floating up & down smoothly)
        findViewById<ImageView>(R.id.ivMascotName)?.let { mascot ->
            val bobbingAnimator = ObjectAnimator.ofFloat(mascot, "translationY", 0f, -14f, 0f).apply {
                duration = 2500
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.RESTART
            }
            bobbingAnimator.start()
        }

        sharedPreferences = getSharedPreferences("ILU", MODE_PRIVATE)

        val etName = findViewById<EditText>(R.id.etName)
        val btnSave = findViewById<Button>(R.id.btnSave)

        // Interactive Button Press Scale Animation (Mengecil saat ditekan, membal saat dilepas)
        btnSave.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(80).start()
                }
            }
            false
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Masukkan nama terlebih dahulu", Toast.LENGTH_SHORT).show()
            } else {
                sharedPreferences.edit().putString("username", name).apply()
                startActivity(Intent(this, WidgetIntroActivity::class.java))
                finish()
            }
        }
    }
}