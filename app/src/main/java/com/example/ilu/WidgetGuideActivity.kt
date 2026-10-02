package com.iluiis.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

import android.widget.ImageButton

class WidgetGuideActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_guide)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Mascot Bobbing Animation (Floating up & down smoothly)
        findViewById<android.widget.ImageView>(R.id.ivMascotGuide)?.let { mascot ->
            val bobbingAnimator = android.animation.ObjectAnimator.ofFloat(mascot, "translationY", 0f, -12f, 0f).apply {
                duration = 2500
                repeatCount = android.animation.ValueAnimator.INFINITE
                repeatMode = android.animation.ValueAnimator.RESTART
            }
            bobbingAnimator.start()
        }

        findViewById<Button>(R.id.btnDone).setOnClickListener {

            startActivity(Intent(this, GuideActivity::class.java))
            finish()

        }

    }
}
