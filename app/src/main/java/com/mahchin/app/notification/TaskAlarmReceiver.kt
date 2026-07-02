package com.mahchin.app.notification

import android.Manifest
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mahchin.app.MainActivity
import android.app.PendingIntent

class TaskAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "یادآوری تسک"
        val project = intent.getStringExtra(EXTRA_PROJECT).orEmpty()
        val openIntent = PendingIntent.getActivity(
            context,
            7001,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_ALARM)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("آلارم تسک")
            .setContentText(if (project.isBlank()) title else "$title • $project")
            .setStyle(NotificationCompat.BigTextStyle().bigText(if (project.isBlank()) title else "پروژه: $project\n$title"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(openIntent)
            .setFullScreenIntent(openIntent, true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 700))

        if (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), builder.build())
        }
    }

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_PROJECT = "extra_project"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_ORIGIN = "extra_task_origin"
    }
}
