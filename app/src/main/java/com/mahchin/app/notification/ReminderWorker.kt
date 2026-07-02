package com.mahchin.app.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mahchin.app.domain.JalaliCalendar

class ReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        // SAFE STUB TO FIX BUILD
        val today = JalaliCalendar.today()

        return Result.success()
    }
}
