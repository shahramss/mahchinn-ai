package com.mahchin.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mahchin.app.data.db.AppDatabase
import com.mahchin.app.data.model.UserSettings
import com.mahchin.app.data.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val dao = AppDatabase.getInstance(context).taskDao()
            val settings = dao.getSettings() ?: UserSettings()
            ReminderScheduler.schedulePeriodic(context, settings)
            val repository = TaskRepository(dao)
            TaskAlarmScheduler.rescheduleAll(context, repository.getFutureAlarms())
            pending.finish()
        }
    }
}
