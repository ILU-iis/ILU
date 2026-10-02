package com.iluiis.app

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        // Mascot Bobbing Animation
        findViewById<ImageView>(R.id.ivMascotIntro)?.let { mascot ->
            val bobbingAnimator = ObjectAnimator.ofFloat(mascot, "translationY", 0f, -14f, 0f).apply {
                duration = 2500
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.RESTART
            }
            bobbingAnimator.start()
        }

        val btnNext = findViewById<Button>(R.id.btnNext)

        // Interactive Button Press Scale Animation (Mengecil saat ditekan, membal saat dilepas)
        btnNext.setOnTouchListener { view, event ->
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

        val prefs = getSharedPreferences("ILU", MODE_PRIVATE)
        val username = prefs.getString("username", null)

        btnNext.setOnClickListener {
            if (username == null) {
                startActivity(Intent(this, NameActivity::class.java))
            } else {
                startActivity(Intent(this, ChatActivity::class.java))
            }
            finish()
        }
    }
}
