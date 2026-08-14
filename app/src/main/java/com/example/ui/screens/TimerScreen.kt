package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserSettingsEntity
import com.example.model.PomodoroMode
import com.example.model.TimerState
import com.example.ui.components.ActiveTaskBanner
import com.example.ui.components.CircularTimer
import com.example.util.TimeFormatter

@Composable
fun TimerScreen(
    currentMode: PomodoroMode,
    timerState: TimerState,
    remainingSeconds: Int,
    totalSeconds: Int,
    currentCycleStep: Int,
    userSettings: UserSettingsEntity,
    activeTask: TaskEntity?,
    todayFocusCount: Int,
    todayFocusMinutes: Int,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onSkipSession: () -> Unit,
    onSwitchMode: (PomodoroMode) -> Unit,
    onAdjustTime: (Int) -> Unit,
    onSelectTaskClick: () -> Unit,
    onClearActiveTask: () -> Unit,
    onToggleTaskCompletion: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = listOf(PomodoroMode.FOCUS, PomodoroMode.SHORT_BREAK, PomodoroMode.LONG_BREAK)
    val selectedTabIndex = modes.indexOf(currentMode).coerceAtLeast(0)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Mode Selector Tab Row
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                modes.forEach { mode ->
                    val isSelected = currentMode == mode
                    val tabBgColor by animateColorAsState(
                        targetValue = if (isSelected) mode.primaryColor else Color.Transparent,
                        label = "TabBg"
                    )
                    val tabTextColor by animateColorAsState(
                        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "TabText"
                    )

                    Surface(
                        onClick = { onSwitchMode(mode) },
                        shape = RoundedCornerShape(20.dp),
                        color = tabBgColor,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_mode_${mode.name.lowercase()}")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        ) {
                            Text(
                                text = mode.title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = tabTextColor
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Circular Timer Component
        CircularTimer(
            mode = currentMode,
            timerState = timerState,
            remainingSeconds = remainingSeconds,
            totalSeconds = totalSeconds,
            currentCycleStep = currentCycleStep,
            maxCycleSteps = userSettings.longBreakInterval,
            onAdjustTime = onAdjustTime
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Timer Control Action Buttons (Reset, Play/Pause, Skip)
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Reset Button
            FilledTonalIconButton(
                onClick = onResetTimer,
                modifier = Modifier
                    .size(52.dp)
                    .testTag("btn_timer_reset"),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Sıfırla",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Main Action Button (Start / Pause)
            Button(
                onClick = {
                    if (timerState == TimerState.RUNNING) {
                        onPauseTimer()
                    } else {
                        onStartTimer()
                    }
                },
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = currentMode.primaryColor,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
                modifier = Modifier
                    .height(64.dp)
                    .testTag("btn_timer_toggle")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (timerState == TimerState.RUNNING) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (timerState == TimerState.RUNNING) "Duraklat" else "Başlat",
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (timerState == TimerState.RUNNING) "Duraklat" else "Başlat",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Skip Button
            FilledTonalIconButton(
                onClick = onSkipSession,
                modifier = Modifier
                    .size(52.dp)
                    .testTag("btn_timer_skip"),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Atla",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Active Task Banner
        ActiveTaskBanner(
            activeTask = activeTask,
            onSelectTaskClick = onSelectTaskClick,
            onClearActiveTask = onClearActiveTask,
            onToggleTaskCompletion = onToggleTaskCompletion
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Today's Quick Summary Row
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🍅",
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "$todayFocusCount Pomodoro",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Bugün",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = TimeFormatter.formatMinutesToHourMin(todayFocusMinutes),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Toplam Süre",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
