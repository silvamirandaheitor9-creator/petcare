package com.petcare.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    foreignKeys = [ForeignKey(
        entity = Pet::class,
        parentColumns = ["id"],
        childColumns = ["petId"],
        onDelete = ForeignKey.CASCADE,
    )]
)
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val title: String,
    val category: String,    // vacina, consulta, banho, medicação, alimentação, vermífugo, personalizado
    val dateTimeMillis: Long,
    val recurrence: String = "none", // none, daily, weekly, monthly
    val isCompleted: Boolean = false,
    val notes: String = "",
    val notificationId: Int = 0,
)
