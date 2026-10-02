package com.iluiis.app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

import android.widget.ImageButton

class WidgetIntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_intro)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Mascot Bobbing Animation (Floating up & down smoothly)
        findViewById<android.widget.ImageView>(R.id.ivMascotWidgetIntro)?.let { mascot ->
            val bobbingAnimator = android.animation.ObjectAnimator.ofFloat(mascot, "translationY", 0f, -14f, 0f).apply {
                duration = 2500
                repeatCount = android.animation.ValueAnimator.INFINITE
                repeatMode = android.animation.ValueAnimator.RESTART
            }
            bobbingAnimator.start()
        }

        val btnAddWidget = findViewById<Button>(R.id.btnAddWidget)
        val btnLater = findViewById<Button>(R.id.btnLater)

        btnAddWidget.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                val appWidgetManager =
                    getSystemService(AppWidgetManager::class.java)

                val provider =
                    ComponentName(this, ILUHomeWidget::class.java)

                if (appWidgetManager.isRequestPinAppWidgetSupported) {

                    val successIntent = Intent(this, WidgetPinnedReceiver::class.java)

                    val successCallback = PendingIntent.getBroadcast(
                        this,
                        0,
                        successIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val success = appWidgetManager.requestPinAppWidget(
                        provider,
                        null,
                        successCallback
                    )

                    if (!success) {
                        startActivity(Intent(this, WidgetGuideActivity::class.java))
                        finish()
                    }

                } else {

                    startActivity(Intent(this, WidgetGuideActivity::class.java))
                    finish()
                }
            }
        }

        btnLater.setOnClickListener {

            startActivity(Intent(this, ChatActivity::class.java))
            finish()
        }
    }
}
