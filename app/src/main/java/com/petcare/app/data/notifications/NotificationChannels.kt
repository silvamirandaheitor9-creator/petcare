package com.petcare.app.data.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val REMINDERS_CHANNEL_ID   = "petcare_reminders"
    const val REMINDERS_CHANNEL_NAME = "Lembretes de saúde"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REMINDERS_CHANNEL_ID,
                REMINDERS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Lembretes de vacinas, consultas e cuidados do pet"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 100, 250)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
