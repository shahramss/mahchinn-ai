package com.mahchin.app.data.model

data class TaskItem(
    val id: Long,
    val origin: TaskOrigin,
    val sourceTemplateId: Long? = null,
    val projectId: Long? = null,
    val projectName: String? = null,
    val sourceMindMapNodeId: Long? = null,
    val mindMapPath: String? = null,
    val title: String,
    val description: String,
    val dayOfMonth: Int,
    val jalaliYear: Int,
    val jalaliMonth: Int,
    val jalaliDay: Int,
    val taskType: TaskType,
    val status: TaskStatus,
    val priority: TaskPriority,
    val alarmAtMillis: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val movedFromDate: String?,
    val movedToDate: String?
) {
    val dateKey: String get() = "%04d-%02d-%02d".format(jalaliYear, jalaliMonth, jalaliDay)
}
