package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH
}

enum class TaskCategory(val displayName: String, val iconName: String) {
    WORK("İş", "Work"),
    STUDY("Ders / Çalışma", "School"),
    DEVELOPMENT("Yazılım", "Code"),
    READING("Okuma", "Book"),
    PERSONAL("Kişisel", "Person"),
    OTHER("Diğer", "Category")
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val targetPomodoros: Int = 4,
    val completedPomodoros: Int = 0,
    val isCompleted: Boolean = false,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val category: TaskCategory = TaskCategory.WORK,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
