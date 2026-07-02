package com.mahchin.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build

object NotificationChannels {
    const val CHANNEL_NORMAL = "mahchin_reminders"
    const val CHANNEL_STRICT = "mahchin_reminders_strict"
    const val CHANNEL_ALARM = "mahchin_task_alarms"

    fun create(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val normal = NotificationChannel(
                CHANNEL_NORMAL,
                "یادآوری‌های ماه‌چین",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "یادآوری کارهای باقی‌مانده امروز"
                enableVibration(false)
            }
            val strict = NotificationChannel(
                CHANNEL_STRICT,
                "یادآوری خیلی جدی ماه‌چین",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "یادآوری همراه با صدا و ویبره"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
            }
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val alarmAttrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val alarm = NotificationChannel(
                CHANNEL_ALARM,
                "آلارم تسک‌های ماه‌چین",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "آلارم اختصاصی هر تسک با صدای پیش‌فرض آلارم گوشی"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(alarmSound, alarmAttrs)
            }
            manager.createNotificationChannels(listOf(normal, strict, alarm))
        }
    }
}
