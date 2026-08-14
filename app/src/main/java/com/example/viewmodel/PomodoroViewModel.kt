package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.PomodoroSessionEntity
import com.example.data.local.entity.SessionType
import com.example.data.local.entity.TaskCategory
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.TaskPriority
import com.example.data.local.entity.UserSettingsEntity
import com.example.data.repository.PomodoroRepository
import com.example.model.PomodoroMode
import com.example.model.TimerState
import com.example.util.SoundNotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PomodoroViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = PomodoroRepository(
        database.taskDao(),
        database.pomodoroSessionDao(),
        database.userSettingsDao()
    )
    private val soundHelper = SoundNotificationHelper(application)

    // User Settings
    val userSettings: StateFlow<UserSettingsEntity> = repository.userSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UserSettingsEntity()
        )

    // Tasks & Session Data
    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val todaySessions: StateFlow<List<PomodoroSessionEntity>> = repository.getTodaySessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val weekSessions: StateFlow<List<PomodoroSessionEntity>> = repository.getWeekSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val todayFocusMinutes: StateFlow<Int> = repository.getTodayFocusMinutes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val todayFocusCount: StateFlow<Int> = repository.getTodayFocusCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Active Timer State
    private val _currentMode = MutableStateFlow(PomodoroMode.FOCUS)
    val currentMode: StateFlow<PomodoroMode> = _currentMode.asStateFlow()

    private val _timerState = MutableStateFlow(TimerState.IDLE)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(25 * 60)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _totalSeconds = MutableStateFlow(25 * 60)
    val totalSeconds: StateFlow<Int> = _totalSeconds.asStateFlow()

    private val _currentCycleStep = MutableStateFlow(1)
    val currentCycleStep: StateFlow<Int> = _currentCycleStep.asStateFlow()

    private val _activeTaskId = MutableStateFlow<Long?>(null)
    val activeTaskId: StateFlow<Long?> = _activeTaskId.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            // Apply initial settings duration
            val settings = repository.getSettingsSync()
            val initialSeconds = settings.focusDurationMinutes * 60
            _totalSeconds.value = initialSeconds
            _remainingSeconds.value = initialSeconds
        }
    }

    fun startTimer() {
        if (_timerState.value == TimerState.RUNNING) return
        _timerState.value = TimerState.RUNNING

        val settings = userSettings.value
        soundHelper.playStartTone(settings.soundAlertsEnabled, settings.vibrationEnabled)

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_remainingSeconds.value > 0 && _timerState.value == TimerState.RUNNING) {
                delay(1000L)
                if (_remainingSeconds.value > 0) {
                    _remainingSeconds.value -= 1
                }
            }
            if (_remainingSeconds.value <= 0) {
                onSessionFinished()
            }
        }
    }

    fun pauseTimer() {
        if (_timerState.value == TimerState.RUNNING) {
            _timerState.value = TimerState.PAUSED
            timerJob?.cancel()
        }
    }

    fun resumeTimer() {
        if (_timerState.value == TimerState.PAUSED) {
            startTimer()
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        _timerState.value = TimerState.IDLE
        val durationMin = getDurationForMode(_currentMode.value)
        val seconds = durationMin * 60
        _totalSeconds.value = seconds
        _remainingSeconds.value = seconds
    }

    fun skipSession() {
        timerJob?.cancel()
        advanceToNextMode(autoStart = false)
    }

    fun switchMode(mode: PomodoroMode) {
        if (_currentMode.value == mode && _timerState.value != TimerState.COMPLETED) return
        timerJob?.cancel()
        _currentMode.value = mode
        _timerState.value = TimerState.IDLE
        val durationMin = getDurationForMode(mode)
        val seconds = durationMin * 60
        _totalSeconds.value = seconds
        _remainingSeconds.value = seconds
    }

    fun adjustRemainingTime(deltaSeconds: Int) {
        val newTime = (_remainingSeconds.value + deltaSeconds).coerceIn(10, 180 * 60)
        _remainingSeconds.value = newTime
        if (newTime > _totalSeconds.value) {
            _totalSeconds.value = newTime
        }
    }

    fun selectActiveTask(task: TaskEntity?) {
        _activeTaskId.value = task?.id
    }

    private fun getDurationForMode(mode: PomodoroMode): Int {
        val settings = userSettings.value
        return when (mode) {
            PomodoroMode.FOCUS -> settings.focusDurationMinutes
            PomodoroMode.SHORT_BREAK -> settings.shortBreakDurationMinutes
            PomodoroMode.LONG_BREAK -> settings.longBreakDurationMinutes
        }
    }

    private suspend fun onSessionFinished() {
        _timerState.value = TimerState.COMPLETED
        val completedMode = _currentMode.value
        val settings = userSettings.value

        soundHelper.playSessionCompleteTone(
            settings.soundAlertsEnabled,
            settings.vibrationEnabled
        )

        // Find active task name if any
        var currentTaskTitle: String? = null
        val taskId = _activeTaskId.value
        if (taskId != null) {
            val task = repository.getTaskById(taskId)
            currentTaskTitle = task?.title
        }

        // Log session in Database
        repository.logCompletedSession(
            sessionType = completedMode.sessionType,
            durationMinutes = getDurationForMode(completedMode),
            taskId = taskId,
            taskTitle = currentTaskTitle
        )

        val shouldAutoStart = if (completedMode == PomodoroMode.FOCUS) {
            settings.autoStartBreaks
        } else {
            settings.autoStartPomodoros
        }

        advanceToNextMode(autoStart = shouldAutoStart)
    }

    private fun advanceToNextMode(autoStart: Boolean) {
        val settings = userSettings.value
        when (_currentMode.value) {
            PomodoroMode.FOCUS -> {
                val nextStep = _currentCycleStep.value + 1
                if (nextStep > settings.longBreakInterval) {
                    _currentCycleStep.value = 1
                    _currentMode.value = PomodoroMode.LONG_BREAK
                } else {
                    _currentCycleStep.value = nextStep
                    _currentMode.value = PomodoroMode.SHORT_BREAK
                }
            }
            PomodoroMode.SHORT_BREAK, PomodoroMode.LONG_BREAK -> {
                _currentMode.value = PomodoroMode.FOCUS
            }
        }

        val durationMin = getDurationForMode(_currentMode.value)
        val seconds = durationMin * 60
        _totalSeconds.value = seconds
        _remainingSeconds.value = seconds
        _timerState.value = TimerState.IDLE

        if (autoStart) {
            startTimer()
        }
    }

    // Task CRUD operations
    fun createTask(
        title: String,
        description: String = "",
        targetPomodoros: Int = 4,
        priority: TaskPriority = TaskPriority.MEDIUM,
        category: TaskCategory = TaskCategory.WORK
    ) {
        viewModelScope.launch {
            val newTask = TaskEntity(
                title = title.trim(),
                description = description.trim(),
                targetPomodoros = targetPomodoros.coerceAtLeast(1),
                completedPomodoros = 0,
                isCompleted = false,
                priority = priority,
                category = category,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val newId = repository.insertTask(newTask)
            if (_activeTaskId.value == null) {
                _activeTaskId.value = newId
            }
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            if (_activeTaskId.value == task.id) {
                _activeTaskId.value = null
            }
            repository.deleteTask(task)
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task.id, task.isCompleted)
            if (!task.isCompleted && _activeTaskId.value == task.id) {
                _activeTaskId.value = null
            }
        }
    }

    // Settings update
    fun updateSettings(
        focusDurationMinutes: Int = userSettings.value.focusDurationMinutes,
        shortBreakDurationMinutes: Int = userSettings.value.shortBreakDurationMinutes,
        longBreakDurationMinutes: Int = userSettings.value.longBreakDurationMinutes,
        longBreakInterval: Int = userSettings.value.longBreakInterval,
        autoStartBreaks: Boolean = userSettings.value.autoStartBreaks,
        autoStartPomodoros: Boolean = userSettings.value.autoStartPomodoros,
        soundAlertsEnabled: Boolean = userSettings.value.soundAlertsEnabled,
        vibrationEnabled: Boolean = userSettings.value.vibrationEnabled,
        dailyGoalPomodoros: Int = userSettings.value.dailyGoalPomodoros
    ) {
        viewModelScope.launch {
            val updated = userSettings.value.copy(
                focusDurationMinutes = focusDurationMinutes,
                shortBreakDurationMinutes = shortBreakDurationMinutes,
                longBreakDurationMinutes = longBreakDurationMinutes,
                longBreakInterval = longBreakInterval,
                autoStartBreaks = autoStartBreaks,
                autoStartPomodoros = autoStartPomodoros,
                soundAlertsEnabled = soundAlertsEnabled,
                vibrationEnabled = vibrationEnabled,
                dailyGoalPomodoros = dailyGoalPomodoros
            )
            repository.saveSettings(updated)

            // If timer is idle, update timer duration immediately
            if (_timerState.value == TimerState.IDLE) {
                val durationMin = when (_currentMode.value) {
                    PomodoroMode.FOCUS -> updated.focusDurationMinutes
                    PomodoroMode.SHORT_BREAK -> updated.shortBreakDurationMinutes
                    PomodoroMode.LONG_BREAK -> updated.longBreakDurationMinutes
                }
                val sec = durationMin * 60
                _totalSeconds.value = sec
                _remainingSeconds.value = sec
            }
        }
    }

    fun resetSettingsToDefault() {
        viewModelScope.launch {
            val defaultSettings = UserSettingsEntity()
            repository.saveSettings(defaultSettings)
            if (_timerState.value == TimerState.IDLE) {
                val sec = defaultSettings.focusDurationMinutes * 60
                _totalSeconds.value = sec
                _remainingSeconds.value = sec
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        soundHelper.release()
    }
}
