package com.iluiis.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class WidgetPinnedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        // Simpan bahwa widget sudah dipasang
        val prefs = context.getSharedPreferences("ILU", Context.MODE_PRIVATE)

        prefs.edit()
            .putBoolean("widget_added", true)
            .apply()

        val chatIntent = Intent(context, ChatActivity::class.java)
        chatIntent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP

        context.startActivity(chatIntent)
    }
}
