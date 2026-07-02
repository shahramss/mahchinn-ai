package com.mahchin.app.notification

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mahchin.app.MainActivity
import com.mahchin.app.R

object NotificationHelper {
    fun canNotify(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    fun showTestNotification(context: Context): Boolean {
        if (!canNotify(context)) return false

        val openIntent = Intent(context, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            context,
            20,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_STRICT)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("یادآوری ماه‌چین فعال است")
            .setContentText("اگر کاری از امروز باقی بماند، ماه‌چین بهت یادآوری می‌کند.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("این یک نوتیفیکیشن آزمایشی است. اگر کاری از امروز باقی بماند، ماه‌چین طبق تنظیمات یادآوری می‌کند."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 180, 80, 180))
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(1406, notification)
            true
        } catch (_: SecurityException) {
            false
        }
    }

    fun clearTodayReminder(context: Context) {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(ReminderWorker.NOTIFICATION_ID)
    }
}
