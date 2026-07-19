package com.petcare.app.data.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.petcare.app.data.db.entity.Reminder
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Agenda um AlarmManager exato para o lembrete.
     * [petName] e [petPhotoPath] são passados no Intent para o BroadcastReceiver
     * montar a notificação rica sem acesso ao BD.
     */
    fun schedule(reminder: Reminder, petName: String = "", petPhotoPath: String = "") {
        // Usa o ID do lembrete como requestCode único do PendingIntent
        val notifId = reminder.id.toInt().coerceAtLeast(1)

        val intent = Intent(context, ReminderBroadcastReceiver::class.java).apply {
            putExtra(ReminderBroadcastReceiver.EXTRA_REMINDER_ID,    reminder.id)
            putExtra(ReminderBroadcastReceiver.EXTRA_PET_NAME,       petName)
            putExtra(ReminderBroadcastReceiver.EXTRA_PET_PHOTO_PATH, petPhotoPath)
            putExtra(ReminderBroadcastReceiver.EXTRA_CATEGORY,       reminder.category)
            putExtra(ReminderBroadcastReceiver.EXTRA_TITLE,          reminder.title)
            putExtra(ReminderBroadcastReceiver.EXTRA_NOTIF_ID,       notifId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notifId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminder.dateTimeMillis,
                pendingIntent,
            )
            // TODO DEBUG — remover antes do release
            val fmt = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            val humanTime = fmt.format(Date(reminder.dateTimeMillis))
            val nowMs = System.currentTimeMillis()
            val diffMin = (reminder.dateTimeMillis - nowMs) / 60_000
            Log.d(
                "ReminderScheduler",
                "setExactAndAllowWhileIdle agendado | id=${reminder.id} " +
                "targetMs=${reminder.dateTimeMillis} ($humanTime) | " +
                "nowMs=$nowMs | dispara em ${diffMin}min",
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminder.dateTimeMillis,
                pendingIntent,
            )
        }
    }

    /** Cancela o alarme agendado para este lembrete. */
    fun cancel(reminder: Reminder) {
        val notifId = reminder.id.toInt().coerceAtLeast(1)
        val intent = Intent(context, ReminderBroadcastReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notifId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.cancel(pendingIntent)
    }
}
