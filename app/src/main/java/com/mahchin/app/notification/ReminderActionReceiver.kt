package com.mahchin.app.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mahchin.app.data.db.AppDatabase
import com.mahchin.app.data.repository.TaskRepository
import com.mahchin.app.domain.JalaliCalendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_SNOOZE -> {
                ReminderScheduler.scheduleSnooze(context, 60)
                cancel(context)
            }
            ACTION_MOVE_TO_TOMORROW -> {
                val pending = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    val repo = TaskRepository(AppDatabase.getInstance(context).taskDao())
                    repo.moveRemainingToTomorrow(JalaliCalendar.today())
                    cancel(context)
                    pending.finish()
                }
            }
        }
    }

    private fun cancel(context: Context) {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(ReminderWorker.1406)
    }

    companion object {
        const val ACTION_SNOOZE = "com.mahchin.app.ACTION_SNOOZE"
        const val ACTION_MOVE_TO_TOMORROW = "com.mahchin.app.ACTION_MOVE_TO_TOMORROW"
    }
}
