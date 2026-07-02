package com.mahchin.app.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mahchin.app.MainActivity

object NotificationHelper {

    fun canNotify(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    fun showTestNotification(context: Context): Boolean {
        if (!canNotify(context)) return false

        val intent = Intent(context, MainActivity::class.java)

        val pending = PendingIntent.getActivity(
            context,
            20,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "mahchin_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("یادآوری ماه‌چین")
            .setContentText("سیستم یادآوری فعال است")
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(1406, notification)
            true
        } catch (e: Exception) {
            false
        }
    }
}
