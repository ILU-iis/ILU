package com.iluiis.app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class GratitudeWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {

        for (appWidgetId in appWidgetIds) {

            val prefs =
                context.getSharedPreferences("ILU", Context.MODE_PRIVATE)

            val username =
                prefs.getString("username", "Teman")

            val views =
                RemoteViews(context.packageName, R.layout.widget_gratitude)

            views.setTextViewText(
                R.id.tvWidgetTitle,
                "Halo $username 🌸"
            )

            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            val mascotResId = when (hour) {
                in 5..11 -> R.drawable.mascot_cheerful
                in 12..17 -> R.drawable.mascot_excited
                else -> R.drawable.mascot_sad
            }
            views.setImageViewResource(R.id.ivWidgetMascot, mascotResId)

            val intent =
                Intent(context, ChatActivity::class.java)

            val pendingIntent =
                PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

            views.setOnClickPendingIntent(
                R.id.widgetRoot,
                pendingIntent
            )

            appWidgetManager.updateAppWidget(
                appWidgetId,
                views
            )
        }
    }
}
