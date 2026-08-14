package com.example.data.repository

import com.example.data.local.dao.PomodoroSessionDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.dao.UserSettingsDao
import com.example.data.local.entity.PomodoroSessionEntity
import com.example.data.local.entity.SessionType
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class PomodoroRepository(
    private val taskDao: TaskDao,
    private val sessionDao: PomodoroSessionDao,
    private val settingsDao: UserSettingsDao
) {
    // Tasks
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val completedTasksCount: Flow<Int> = taskDao.getCompletedTasksCount()
    val activeTasksCount: Flow<Int> = taskDao.getActiveTasksCount()

    suspend fun getTaskById(id: Long): TaskEntity? = taskDao.getTaskById(id)

    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)

    suspend fun incrementTaskPomodoro(id: Long) = taskDao.incrementCompletedPomodoro(id)

    suspend fun toggleTaskCompletion(id: Long, currentStatus: Boolean) {
        taskDao.updateCompletionStatus(id, !currentStatus)
    }

    // Sessions & Stats
    val allSessions: Flow<List<PomodoroSessionEntity>> = sessionDao.getAllSessions()

    fun getTodaySessions(): Flow<List<PomodoroSessionEntity>> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000 - 1
        return sessionDao.getSessionsBetween(startOfDay, endOfDay)
    }

    fun getWeekSessions(): Flow<List<PomodoroSessionEntity>> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -6)
        }
        val startOfWeek = calendar.timeInMillis
        val endOfWeek = System.currentTimeMillis()
        return sessionDao.getSessionsBetween(startOfWeek, endOfWeek)
    }

    fun getTodayFocusMinutes(): Flow<Int> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return sessionDao.getTotalFocusMinutesSince(calendar.timeInMillis).map { it ?: 0 }
    }

    fun getTodayFocusCount(): Flow<Int> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return sessionDao.getFocusCountSince(calendar.timeInMillis)
    }

    suspend fun logCompletedSession(
        sessionType: SessionType,
        durationMinutes: Int,
        taskId: Long? = null,
        taskTitle: String? = null
    ): Long {
        val session = PomodoroSessionEntity(
            taskId = taskId,
            taskTitle = taskTitle,
            sessionType = sessionType,
            durationMinutes = durationMinutes,
            timestamp = System.currentTimeMillis()
        )
        val id = sessionDao.insertSession(session)
        if (sessionType == SessionType.FOCUS && taskId != null) {
            taskDao.incrementCompletedPomodoro(taskId)
        }
        return id
    }

    // Settings
    val userSettings: Flow<UserSettingsEntity> = settingsDao.getSettings().map {
        it ?: UserSettingsEntity()
    }

    suspend fun getSettingsSync(): UserSettingsEntity {
        return settingsDao.getSettingsSync() ?: UserSettingsEntity().also {
            settingsDao.saveSettings(it)
        }
    }

    suspend fun saveSettings(settings: UserSettingsEntity) {
        settingsDao.saveSettings(settings)
    }
}
