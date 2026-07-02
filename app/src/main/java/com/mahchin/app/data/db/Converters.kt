package com.mahchin.app.data.db

import androidx.room.TypeConverter
import com.mahchin.app.data.model.ReminderIntensity
import com.mahchin.app.data.model.TaskPriority
import com.mahchin.app.data.model.TaskStatus
import com.mahchin.app.data.model.TaskType

class Converters {
    @TypeConverter fun taskStatusToString(value: TaskStatus?): String? = value?.name
    @TypeConverter fun stringToTaskStatus(value: String?): TaskStatus = value?.let { TaskStatus.valueOf(it) } ?: TaskStatus.NOT_STARTED

    @TypeConverter fun taskTypeToString(value: TaskType?): String? = value?.name
    @TypeConverter fun stringToTaskType(value: String?): TaskType = value?.let { TaskType.valueOf(it) } ?: TaskType.ONE_TIME

    @TypeConverter fun priorityToString(value: TaskPriority?): String? = value?.name
    @TypeConverter fun stringToPriority(value: String?): TaskPriority = value?.let { TaskPriority.valueOf(it) } ?: TaskPriority.NORMAL

    @TypeConverter fun reminderToString(value: ReminderIntensity?): String? = value?.name
    @TypeConverter fun stringToReminder(value: String?): ReminderIntensity = value?.let { ReminderIntensity.valueOf(it) } ?: ReminderIntensity.SERIOUS
}
