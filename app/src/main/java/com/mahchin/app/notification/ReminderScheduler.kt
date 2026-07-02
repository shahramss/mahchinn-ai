package com.mahchin.app.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mahchin.app.data.model.ReminderIntensity
import com.mahchin.app.data.model.UserSettings
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val PERIODIC_WORK = "mahchin_periodic_reminders"
    private const val SNOOZE_WORK = "mahchin_snooze_reminder"
    private const val IMMEDIATE_CHECK_WORK = "mahchin_immediate_reminder_check"

    fun schedulePeriodic(context: Context, settings: UserSettings) {
        val intervalHours = when (settings.reminderIntensity) {
            ReminderIntensity.CALM -> 24L
            ReminderIntensity.NORMAL -> 3L
            ReminderIntensity.SERIOUS,
            ReminderIntensity.VERY_SERIOUS -> 1L
        }

        val request = PeriodicWorkRequestBuilder<ReminderWorker>(intervalHours.coerceAtLeast(1), TimeUnit.HOURS)
            .setInitialDelay(calculateInitialDelayMillis(settings), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )

        // چک سریع بعد از ذخیره/باز شدن اپ؛ چون PeriodicWork ممکن است اولین اجرا را دیرتر انجام دهد.
        scheduleImmediateCheck(context, delayMinutes = 1)
    }

    fun scheduleImmediateCheck(context: Context, delayMinutes: Long = 1) {
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMinutes.coerceAtLeast(0), TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE_CHECK_WORK,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun scheduleSnooze(context: Context, minutes: Long = 60) {
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(minutes, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            SNOOZE_WORK,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun calculateInitialDelayMillis(settings: UserSettings): Long {
        val now = LocalDateTime.now()
        val currentTime = now.toLocalTime()
        val start = LocalTime.of(settings.startHour.coerceIn(0, 23), 0)
        val endHour = settings.endHour.coerceIn(1, 24)
        val inWindow = currentTime >= start && (endHour == 24 || currentTime.hour < endHour)
        if (inWindow) return 5 * 60 * 1000L

        var next = now.withHour(settings.startHour.coerceIn(0, 23)).withMinute(0).withSecond(0).withNano(0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return Duration.between(now, next).toMillis().coerceAtLeast(0)
    }
}
