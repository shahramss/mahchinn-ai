package com.mahchin.app

import android.app.Application
import com.mahchin.app.data.db.AppDatabase
import com.mahchin.app.data.repository.TaskRepository
import com.mahchin.app.notification.NotificationChannels

class MahChinApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: TaskRepository by lazy { TaskRepository(database.taskDao()) }

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.create(this)
    }
}
