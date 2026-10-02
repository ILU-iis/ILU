package com.iluiis.app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ILUHomeWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, ILUHomeWidget::class.java)
            )
            for (id in ids) {
                updateWidget(context, manager, id)
            }
        }

        private fun updateWidget(
            context: Context,
            manager: AppWidgetManager,
            widgetId: Int
        ) {
            val prefs = context.getSharedPreferences("ILU", Context.MODE_PRIVATE)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val lastDate = prefs.getString("last_gratitude_date", "")

            val views = RemoteViews(context.packageName, R.layout.i_l_u_home_widget)

            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            val isFilled = (today == lastDate)

            if (isFilled) {
                views.setImageViewResource(R.id.ivWidgetMascot, R.drawable.mascot_proud)
                views.setTextViewText(R.id.tvWidgetTitle, "Makasih ya udah cerita ke aku! 🌻✨")
                views.setTextViewText(R.id.tvWidgetStatus, "Seneng deh denger ceritamu hari ini. Aku selalu di sini buat kamu 💛")
                views.setTextViewText(R.id.btnWidgetAction, "Ngobrol Lagi 💬")
            } else {
                when (hour) {
                    in 5..10 -> {
                        views.setImageViewResource(R.id.ivWidgetMascot, R.drawable.mascot_cheerful)
                        views.setTextViewText(R.id.tvWidgetTitle, "Selamat pagi! 🌅 Ada 3 hal baik apa hari ini?")
                        views.setTextViewText(R.id.tvWidgetStatus, "Yuk cerita dan awali harimu bareng ILU 🌻")
                        views.setTextViewText(R.id.btnWidgetAction, "Curhat Sama ILU 💬")
                    }
                    in 11..16 -> {
                        views.setImageViewResource(R.id.ivWidgetMascot, R.drawable.mascot_energetic)
                        views.setTextViewText(R.id.tvWidgetTitle, "Selamat siang! ☀️ Gimana harimu sejauh ini?")
                        views.setTextViewText(R.id.tvWidgetStatus, "Istirahat sejenak, yuk berbagi ceritamu! ✨")
                        views.setTextViewText(R.id.btnWidgetAction, "Curhat Sama ILU 💬")
                    }
                    else -> {
                        views.setImageViewResource(R.id.ivWidgetMascot, R.drawable.mascot_happy)
                        views.setTextViewText(R.id.tvWidgetTitle, "Selamat malam! 🌙 Gimana perasaanmu hari ini?")
                        views.setTextViewText(R.id.tvWidgetStatus, "Ceritain 3 hal baik sebelum istirahat 💛")
                        views.setTextViewText(R.id.btnWidgetAction, "Cerita Bareng ILU 💬")
                    }
                }
            }

            val intent = Intent(context, ChatActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent)
            views.setOnClickPendingIntent(R.id.btnWidgetAction, pendingIntent)

            manager.updateAppWidget(widgetId, views)
        }
    }
}
