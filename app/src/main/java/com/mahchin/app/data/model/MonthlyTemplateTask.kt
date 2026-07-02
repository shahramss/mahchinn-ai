package com.mahchin.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_template_tasks")
data class MonthlyTemplateTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long? = null,
    val sourceMindMapNodeId: Long? = null,
    val title: String,
    val description: String = "",
    val dayOfMonth: Int,
    val jalaliYear: Int? = null,
    val jalaliMonth: Int? = null,
    val jalaliDay: Int? = null,
    val taskType: TaskType = TaskType.MONTHLY_TEMPLATE,
    val status: TaskStatus = TaskStatus.NOT_STARTED,
    val priority: TaskPriority = TaskPriority.NORMAL,
    val alarmHour: Int? = null,
    val alarmMinute: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val movedFromDate: String? = null,
    val movedToDate: String? = null,
    val isActive: Boolean = true
)
