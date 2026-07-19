package com.petcare.app.data.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import com.petcare.app.MainActivity
import com.petcare.app.R
import java.io.File

/**
 * Recebe alarmes agendados pelo [ReminderScheduler] e exibe a notificação rica:
 *  - Título e corpo contextuais baseados na categoria
 *  - Foto do pet como ícone grande (ou ícone de categoria como fallback)
 *  - Botões de ação "Concluir" e "Adiar 1h"
 *  - Vibração personalizada
 *  - Agrupamento nativo por GROUP_KEY
 */
class ReminderBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId   = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        val petName      = intent.getStringExtra(EXTRA_PET_NAME) ?: ""
        val petPhotoPath = intent.getStringExtra(EXTRA_PET_PHOTO_PATH) ?: ""
        val category     = intent.getStringExtra(EXTRA_CATEGORY) ?: "personalizado"
        val title        = intent.getStringExtra(EXTRA_TITLE) ?: "Lembrete PetCare"
        val notifId      = intent.getIntExtra(EXTRA_NOTIF_ID, reminderId.toInt().coerceAtLeast(1))

        val body = contextualBody(category, petName)

        // ── Intent principal: abre o app ──────────────────────────────────────
        val mainPi = PendingIntent.getActivity(
            context,
            notifId,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // ── Ação "Concluir" ───────────────────────────────────────────────────
        val completePi = PendingIntent.getBroadcast(
            context,
            notifId + 10_000,
            Intent(context, CompleteReminderReceiver::class.java).apply {
                putExtra(EXTRA_REMINDER_ID, reminderId)
                putExtra(EXTRA_NOTIF_ID, notifId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // ── Ação "Adiar 1h" ───────────────────────────────────────────────────
        val snoozePi = PendingIntent.getBroadcast(
            context,
            notifId + 20_000,
            Intent(context, SnoozeReminderReceiver::class.java).apply {
                putExtra(EXTRA_REMINDER_ID,    reminderId)
                putExtra(EXTRA_PET_NAME,       petName)
                putExtra(EXTRA_PET_PHOTO_PATH, petPhotoPath)
                putExtra(EXTRA_CATEGORY,       category)
                putExtra(EXTRA_TITLE,          title)
                putExtra(EXTRA_NOTIF_ID,       notifId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // ── Ícone grande: foto do pet → ícone de categoria → null ─────────────
        val largeBitmap: Bitmap? = loadLargeIcon(context, petPhotoPath, category)

        // ── Constrói a notificação ────────────────────────────────────────────
        val builder = NotificationCompat.Builder(context, NotificationChannels.REMINDERS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(mainPi)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(NotificationChannels.VIBRATION_PATTERN)
            .setGroup(GROUP_KEY)
            .addAction(R.drawable.ic_notification, "Concluir", completePi)
            .addAction(R.drawable.ic_notification, "Adiar 1h", snoozePi)

        if (largeBitmap != null) builder.setLargeIcon(largeBitmap)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notifId, builder.build())

        // ── Notificação de agrupamento (summary) ──────────────────────────────
        // É necessária para que o Android agrupe visualmente as notificações.
        val summaryBuilder = NotificationCompat.Builder(context, NotificationChannels.REMINDERS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("PetCare")
            .setContentText("Lembretes pendentes dos seus pets")
            .setStyle(
                NotificationCompat.InboxStyle()
                    .setSummaryText("PetCare Lembretes")
            )
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
        manager.notify(SUMMARY_NOTIF_ID, summaryBuilder.build())
    }

    // ─────────────────────────────────────────────────────────────────────────

    companion object {
        const val GROUP_KEY       = "com.petcare.app.REMINDERS_GROUP"
        const val SUMMARY_NOTIF_ID = 999_999

        const val EXTRA_REMINDER_ID    = "extra_reminder_id"
        const val EXTRA_PET_NAME       = "extra_pet_name"
        const val EXTRA_PET_PHOTO_PATH = "extra_pet_photo_path"
        const val EXTRA_CATEGORY       = "extra_category"
        const val EXTRA_TITLE          = "extra_title"
        const val EXTRA_NOTIF_ID       = "extra_notif_id"

        /**
         * Gera corpo contextual baseado na categoria e no nome do pet.
         */
        fun contextualBody(category: String, petName: String): String {
            val name = petName.ifBlank { "seu pet" }
            return when (category) {
                "vacina"      -> "Hora da vacina de $name! Mantenha a saúde em dia."
                "consulta"    -> "Consulta de $name agendada. Não esqueça!"
                "banho"       -> "$name está esperando pelo banho e tosa!"
                "medicacao"   -> "Hora da medicação de $name. Não pule a dose!"
                "alimentacao" -> "Hora de alimentar $name. Ele está com fome!"
                "vermifugo"   -> "Vermífugo de $name — previna parasitas!"
                else          -> "Lembrete especial para $name."
            }
        }

        /**
         * Tenta carregar a foto do pet do sistema de arquivos; se não encontrar,
         * decodifica o PNG de categoria como fallback.
         */
        fun loadLargeIcon(context: Context, petPhotoPath: String, category: String): Bitmap? {
            if (petPhotoPath.isNotBlank()) {
                val file = File(petPhotoPath)
                if (file.exists()) {
                    return BitmapFactory.decodeFile(petPhotoPath)
                }
            }
            return try {
                BitmapFactory.decodeResource(context.resources, categoryIconRes(category))
            } catch (_: Exception) {
                null
            }
        }

        /**
         * Mapeia categoria para o drawable de ícone correspondente.
         */
        fun categoryIconRes(category: String): Int = when (category) {
            "vacina"      -> R.drawable.icone_vacina
            "consulta"    -> R.drawable.icone_consulta
            "banho"       -> R.drawable.icone_banho
            "medicacao"   -> R.drawable.icone_medicacao
            "alimentacao" -> R.drawable.icone_alimentacao
            "vermifugo"   -> R.drawable.icone_vermifugo
            else          -> R.drawable.icone_personalizado
        }
    }
}
