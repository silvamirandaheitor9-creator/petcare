package com.petcare.app.data.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.room.Room
import com.petcare.app.data.db.PetCareDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.LOCKED_BOOT_COMPLETED") return

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val db = Room.databaseBuilder(context, PetCareDatabase::class.java, "petcare.db")
                .fallbackToDestructiveMigration()
                .build()
            val pending = db.reminderDao().getAllPendingRemindersOnce()
            val scheduler = ReminderScheduler(context)
            pending.forEach { reminder ->
                if (reminder.dateTimeMillis > System.currentTimeMillis()) {
                    scheduler.schedule(reminder)
                }
            }
            db.close()
        }
    }
}
