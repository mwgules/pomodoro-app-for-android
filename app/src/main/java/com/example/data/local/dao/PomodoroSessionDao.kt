package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.PomodoroSessionEntity
import com.example.data.local.entity.SessionType
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroSessionDao {
    @Query("SELECT * FROM pomodoro_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<PomodoroSessionEntity>>

    @Query("SELECT * FROM pomodoro_sessions WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getSessionsBetween(startTime: Long, endTime: Long): Flow<List<PomodoroSessionEntity>>

    @Query("SELECT * FROM pomodoro_sessions WHERE sessionType = 'FOCUS' AND timestamp >= :startTime AND timestamp <= :endTime")
    fun getFocusSessionsBetween(startTime: Long, endTime: Long): Flow<List<PomodoroSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PomodoroSessionEntity): Long

    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE sessionType = 'FOCUS' AND timestamp >= :startTime")
    fun getFocusCountSince(startTime: Long): Flow<Int>

    @Query("SELECT SUM(durationMinutes) FROM pomodoro_sessions WHERE sessionType = 'FOCUS' AND timestamp >= :startTime")
    fun getTotalFocusMinutesSince(startTime: Long): Flow<Int?>

    @Query("DELETE FROM pomodoro_sessions")
    suspend fun clearAllSessions()
}
