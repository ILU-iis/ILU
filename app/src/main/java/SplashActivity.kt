package com.iluiis.app

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val ivMascot = findViewById<ImageView>(R.id.ivMascot)
        ivMascot?.let { mascot ->
            // Mascot Bobbing Animation (Floating up & down smoothly)
            val bobbingAnimator = ObjectAnimator.ofFloat(mascot, "translationY", 0f, -16f, 0f).apply {
                duration = 2500
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.RESTART
            }
            bobbingAnimator.start()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, IntroActivity::class.java))
            finish()
        }, 3000)
    }
}
