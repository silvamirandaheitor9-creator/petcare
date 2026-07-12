package com.petcare.app.data.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.room.Room
import com.petcare.app.data.db.PetCareDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Reagenda todos os lembretes pendentes após reinicialização do dispositivo.
 * O AlarmManager não persiste alarmes entre reboots — este receiver corrige isso.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        // TODO DEBUG — remover antes do release
        const val DEBUG_PREFS    = "petcare_debug"
        const val KEY_LAST_BOOT_MS = "last_boot_ms"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // TODO DEBUG — remover antes do release
        Log.d("BootReceiver", "onReceive chamado | action=${intent.action}")
        // Grava timestamp para exibição na aba Perfil (sem Logcat/adb)
        context.getSharedPreferences(DEBUG_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_BOOT_MS, System.currentTimeMillis())
            .apply()

        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.LOCKED_BOOT_COMPLETED"
        ) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = Room.databaseBuilder(context, PetCareDatabase::class.java, "petcare.db")
                    .fallbackToDestructiveMigration()
                    .build()

                val now      = System.currentTimeMillis()
                val pending  = db.reminderDao().getAllPendingRemindersOnce()
                val scheduler = ReminderScheduler(context)

                for (reminder in pending) {
                    if (reminder.dateTimeMillis <= now) continue

                    // Busca dados do pet para notificação rica
                    val pet = db.petDao().getPetByIdOnce(reminder.petId)
                    scheduler.schedule(
                        reminder    = reminder,
                        petName     = pet?.name ?: "",
                        petPhotoPath = pet?.photoPath ?: "",
                    )
                }
                db.close()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
