package com.mahchin.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "finance_tasks",
    indices = [Index("projectId"), Index(value = ["jalaliYear", "jalaliMonth"])]
)
data class FinanceTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val title: String,
    val amount: Long,
    val jalaliYear: Int,
    val jalaliMonth: Int,
    val customerPaymentDateKey: String? = null,
    val completedDateKey: String? = null,
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
) {
    val monthKey: String get() = "%04d-%02d".format(jalaliYear, jalaliMonth)
}
