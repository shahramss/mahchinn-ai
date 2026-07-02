package com.mahchin.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey val id: Int = 1,
    val reminderIntensity: ReminderIntensity = ReminderIntensity.SERIOUS,
    val startHour: Int = 8,
    val endHour: Int = 22,
    val soundEnabled: Boolean = false,
    val vibrationEnabled: Boolean = false,
    val darkMode: Boolean = true,
    val backupEnabled: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)
