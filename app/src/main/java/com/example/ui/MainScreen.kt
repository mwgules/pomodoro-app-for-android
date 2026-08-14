package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.PomodoroMode
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.TimerScreen
import com.example.viewmodel.PomodoroViewModel

enum class NavigationTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    TIMER("Zamanlayıcı", Icons.Filled.Timer, Icons.Outlined.Timer, "nav_timer"),
    TASKS("Görevler", Icons.Filled.Checklist, Icons.Outlined.Checklist, "nav_tasks"),
    STATS("İstatistik", Icons.Filled.BarChart, Icons.Outlined.BarChart, "nav_stats"),
    SETTINGS("Ayarlar", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: PomodoroViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(NavigationTab.TIMER) }

    val currentMode by viewModel.currentMode.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val remainingSeconds by viewModel.remainingSeconds.collectAsStateWithLifecycle()
    val totalSeconds by viewModel.totalSeconds.collectAsStateWithLifecycle()
    val currentCycleStep by viewModel.currentCycleStep.collectAsStateWithLifecycle()
    val activeTaskId by viewModel.activeTaskId.collectAsStateWithLifecycle()

    val userSettings by viewModel.userSettings.collectAsStateWithLifecycle()
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val todaySessions by viewModel.todaySessions.collectAsStateWithLifecycle()
    val weekSessions by viewModel.weekSessions.collectAsStateWithLifecycle()
    val todayFocusMinutes by viewModel.todayFocusMinutes.collectAsStateWithLifecycle()
    val todayFocusCount by viewModel.todayFocusCount.collectAsStateWithLifecycle()

    val activeTask = remember(tasks, activeTaskId) {
        tasks.find { it.id == activeTaskId }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🍅",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (selectedTab) {
                                NavigationTab.TIMER -> "Pomodoro"
                                NavigationTab.TASKS -> "Görevler"
                                NavigationTab.STATS -> "İstatistikler"
                                NavigationTab.SETTINGS -> "Ayarlar"
                            },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar")
            ) {
                NavigationTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "ScreenTransition",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { tab ->
            when (tab) {
                NavigationTab.TIMER -> {
                    TimerScreen(
                        currentMode = currentMode,
                        timerState = timerState,
                        remainingSeconds = remainingSeconds,
                        totalSeconds = totalSeconds,
                        currentCycleStep = currentCycleStep,
                        userSettings = userSettings,
                        activeTask = activeTask,
                        todayFocusCount = todayFocusCount,
                        todayFocusMinutes = todayFocusMinutes,
                        onStartTimer = viewModel::startTimer,
                        onPauseTimer = viewModel::pauseTimer,
                        onResetTimer = viewModel::resetTimer,
                        onSkipSession = viewModel::skipSession,
                        onSwitchMode = viewModel::switchMode,
                        onAdjustTime = viewModel::adjustRemainingTime,
                        onSelectTaskClick = { selectedTab = NavigationTab.TASKS },
                        onClearActiveTask = { viewModel.selectActiveTask(null) },
                        onToggleTaskCompletion = viewModel::toggleTaskCompletion
                    )
                }
                NavigationTab.TASKS -> {
                    TasksScreen(
                        tasks = tasks,
                        activeTaskId = activeTaskId,
                        onToggleTaskCompletion = viewModel::toggleTaskCompletion,
                        onSelectForTimer = { task ->
                            viewModel.selectActiveTask(task)
                            selectedTab = NavigationTab.TIMER
                        },
                        onCreateTask = viewModel::createTask,
                        onUpdateTask = viewModel::updateTask,
                        onDeleteTask = viewModel::deleteTask
                    )
                }
                NavigationTab.STATS -> {
                    StatsScreen(
                        todayFocusCount = todayFocusCount,
                        todayFocusMinutes = todayFocusMinutes,
                        userSettings = userSettings,
                        tasks = tasks,
                        todaySessions = todaySessions,
                        weekSessions = weekSessions
                    )
                }
                NavigationTab.SETTINGS -> {
                    SettingsScreen(
                        userSettings = userSettings,
                        onUpdateSettings = { focus, shortBreak, longBreak, interval, autoBreaks, autoPoms, sound, vib, goal ->
                            viewModel.updateSettings(
                                focusDurationMinutes = focus,
                                shortBreakDurationMinutes = shortBreak,
                                longBreakDurationMinutes = longBreak,
                                longBreakInterval = interval,
                                autoStartBreaks = autoBreaks,
                                autoStartPomodoros = autoPoms,
                                soundAlertsEnabled = sound,
                                vibrationEnabled = vib,
                                dailyGoalPomodoros = goal
                            )
                        },
                        onResetDefaults = viewModel::resetSettingsToDefault
                    )
                }
            }
        }
    }
}
