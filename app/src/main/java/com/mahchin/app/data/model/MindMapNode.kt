package com.mahchin.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mind_map_nodes",
    indices = [Index("projectId"), Index("parentId")]
)
data class MindMapNode(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val parentId: Long? = null,
    val title: String,
    val description: String = "",
    val orderIndex: Int = 0,
    val x: Float? = null,
    val y: Float? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
