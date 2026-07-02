package com.mahchin.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.mahchin.app.MainActivity
import com.mahchin.app.data.model.TaskItem
import com.mahchin.app.data.model.TaskOrigin

object TaskAlarmScheduler {
    fun schedule(context: Context, task: TaskItem) {
        val alarmAt = task.alarmAtMillis ?: return
        if (alarmAt <= System.currentTimeMillis()) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = pendingIntent(context, task)

        // برای آلارم واقعی کاربر از AlarmClock استفاده می‌کنیم؛ این روش در حالت Doze/Standby
        // قابل اعتمادتر است و گوشی را در زمان مشخص برای آلارم بیدار می‌کند.
        runCatching {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(alarmAt, openAppPendingIntent(context, task)),
                pendingIntent
            )
        }.onFailure {
            scheduleExactFallback(alarmManager, alarmAt, pendingIntent)
        }
    }

    private fun scheduleExactFallback(
        alarmManager: AlarmManager,
        alarmAt: Long,
        pendingIntent: PendingIntent
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmAt, pendingIntent)
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmAt, pendingIntent)
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmAt, pendingIntent)
        }
    }

    fun cancel(context: Context, task: TaskItem) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent(context, task))
        alarmManager.cancel(openAppPendingIntent(context, task))
    }

    fun rescheduleAll(context: Context, tasks: List<TaskItem>) {
        tasks.forEach { schedule(context, it) }
    }

    private fun requestCode(task: TaskItem): Int {
        val base = when (task.origin) {
            TaskOrigin.DAILY_INSTANCE -> 910000
            TaskOrigin.ONE_TIME -> 920000
            TaskOrigin.TEMPLATE -> 930000
        }
        return base + (task.id % 90_000L).toInt()
    }

    private fun pendingIntent(context: Context, task: TaskItem): PendingIntent {
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            putExtra(TaskAlarmReceiver.EXTRA_TITLE, task.title)
            putExtra(TaskAlarmReceiver.EXTRA_PROJECT, task.projectName ?: "")
            putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, task.id)
            putExtra(TaskAlarmReceiver.EXTRA_TASK_ORIGIN, task.origin.name)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode(task),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun openAppPendingIntent(context: Context, task: TaskItem): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("open_alarm_task_id", task.id)
            putExtra("open_alarm_task_origin", task.origin.name)
        }
        return PendingIntent.getActivity(
            context,
            1_100_000 + (task.id % 90_000L).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
