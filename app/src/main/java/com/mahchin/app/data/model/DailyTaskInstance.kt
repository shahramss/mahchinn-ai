package com.mahchin.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_task_instances",
    indices = [Index(value = ["sourceTemplateId", "jalaliYear", "jalaliMonth", "jalaliDay"], unique = true)]
)
data class DailyTaskInstance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceTemplateId: Long? = null,
    val projectId: Long? = null,
    val sourceMindMapNodeId: Long? = null,
    val title: String,
    val description: String = "",
    val dayOfMonth: Int,
    val jalaliYear: Int,
    val jalaliMonth: Int,
    val jalaliDay: Int,
    val taskType: TaskType = TaskType.DAILY_FROM_TEMPLATE,
    val status: TaskStatus = TaskStatus.NOT_STARTED,
    val priority: TaskPriority = TaskPriority.NORMAL,
    val alarmAtMillis: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val movedFromDate: String? = null,
    val movedToDate: String? = null
)
