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
        const val DEBUG_PREFS       = "petcare_debug"
        const val KEY_LAST_BOOT_MS  = "last_boot_ms"
        const val KEY_BOOT_FOUND    = "boot_found"
        const val KEY_BOOT_SCHEDULED = "boot_scheduled"
        const val KEY_BOOT_SKIPPED  = "boot_skipped"

        /** Lembretes perdidos até 2h antes do boot ainda disparam (quase imediatamente). */
        private const val GRACE_MS      = 2L * 60 * 60 * 1_000   // 2 horas em ms
        /** Atraso do disparo imediato após o boot — dá tempo ao sistema de estabilizar. */
        private const val FIRE_DELAY_MS = 5_000L                  // 5 segundos
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

                val now       = System.currentTimeMillis()
                val pending   = db.reminderDao().getAllPendingRemindersOnce()
                val scheduler = ReminderScheduler(context)

                // TODO DEBUG — remover antes do release
                Log.d("BootReceiver", "Lembretes não-concluídos no banco: ${pending.size}")

                var scheduled = 0
                var skipped   = 0

                for (reminder in pending) {
                    // Lembrete futuro → reagenda normalmente.
                    // Lembrete perdido durante o boot (até 2h atrás) → dispara em 5s.
                    // Lembrete mais antigo que 2h → descarta (já não faz sentido).
                    val targetMs = when {
                        reminder.dateTimeMillis > now ->
                            reminder.dateTimeMillis
                        now - reminder.dateTimeMillis <= GRACE_MS ->
                            now + FIRE_DELAY_MS          // disparar quase imediatamente
                        else -> {
                            skipped++
                            Log.d("BootReceiver", "  SKIP id=${reminder.id} — perdido há ${(now - reminder.dateTimeMillis) / 60_000}min")
                            continue
                        }
                    }

                    val pet = db.petDao().getPetByIdOnce(reminder.petId)
                    scheduler.schedule(
                        reminder     = reminder.copy(dateTimeMillis = targetMs),
                        petName      = pet?.name ?: "",
                        petPhotoPath = pet?.photoPath ?: "",
                    )
                    scheduled++
                    Log.d("BootReceiver", "  SCHEDULED id=${reminder.id} targetMs=$targetMs (em ${(targetMs - now) / 1_000}s)")
                }

                // TODO DEBUG — grava contagens para exibição na aba Perfil
                context.getSharedPreferences(DEBUG_PREFS, Context.MODE_PRIVATE).edit()
                    .putInt(KEY_BOOT_FOUND,     pending.size)
                    .putInt(KEY_BOOT_SCHEDULED, scheduled)
                    .putInt(KEY_BOOT_SKIPPED,   skipped)
                    .apply()

                Log.d("BootReceiver", "Concluído: encontrados=${pending.size} reagendados=$scheduled ignorados=$skipped")

                db.close()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
