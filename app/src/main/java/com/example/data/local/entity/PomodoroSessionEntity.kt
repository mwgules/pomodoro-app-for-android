package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SessionType(val displayName: String) {
    FOCUS("Odaklanma"),
    SHORT_BREAK("Kısa Mola"),
    LONG_BREAK("Uzun Mola")
}

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long? = null,
    val taskTitle: String? = null,
    val sessionType: SessionType = SessionType.FOCUS,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val wasInterrupted: Boolean = false
)
