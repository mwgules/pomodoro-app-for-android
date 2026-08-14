package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PomodoroSessionEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserSettingsEntity
import com.example.ui.components.DailyGoalCard
import com.example.ui.components.StatSummaryCard
import com.example.ui.components.WeeklyChartCard
import com.example.util.TimeFormatter

@Composable
fun StatsScreen(
    todayFocusCount: Int,
    todayFocusMinutes: Int,
    userSettings: UserSettingsEntity,
    tasks: List<TaskEntity>,
    todaySessions: List<PomodoroSessionEntity>,
    weekSessions: List<PomodoroSessionEntity>,
    modifier: Modifier = Modifier
) {
    val completedTasksCount = tasks.count { it.isCompleted }
    val totalFocusSessionsAllTime = weekSessions.count { it.sessionType.name == "FOCUS" }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("stats_screen_scroll")
    ) {
        // Daily Goal Card
        item {
            DailyGoalCard(
                todayFocusCount = todayFocusCount,
                dailyGoal = userSettings.dailyGoalPomodoros,
                todayFocusMinutes = todayFocusMinutes
            )
        }

        // 2x2 Stat Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatSummaryCard(
                        title = "Bugünkü Pomodoro",
                        value = "$todayFocusCount",
                        subtitle = "Seans tamamlandı",
                        icon = Icons.Default.Timer,
                        iconColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )

                    StatSummaryCard(
                        title = "Odaklanma Süresi",
                        value = TimeFormatter.formatMinutesToHourMin(todayFocusMinutes),
                        subtitle = "Bugün kaydedilen",
                        icon = Icons.Default.LocalFireDepartment,
                        iconColor = Color(0xFFEA580C),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatSummaryCard(
                        title = "Biten Görevler",
                        value = "$completedTasksCount",
                        subtitle = "${tasks.size} görevden",
                        icon = Icons.Default.TaskAlt,
                        iconColor = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )

                    StatSummaryCard(
                        title = "Haftalık Seans",
                        value = "$totalFocusSessionsAllTime",
                        subtitle = "Son 7 günde",
                        icon = Icons.Default.BarChart,
                        iconColor = Color(0xFF6366F1),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Weekly Bar Chart
        item {
            WeeklyChartCard(weekSessions = weekSessions)
        }

        // Today's Session History Title
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bugünkü Seans Geçmişi",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (todaySessions.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Bugün henüz tamamlanmış bir seans bulunmuyor.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(todaySessions, key = { it.id }) { session ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val dotColor = when (session.sessionType.name) {
                                "FOCUS" -> MaterialTheme.colorScheme.primary
                                "SHORT_BREAK" -> Color(0xFF10B981)
                                else -> Color(0xFF6366F1)
                            }
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = session.taskTitle ?: session.sessionType.displayName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${session.sessionType.displayName} • ${session.durationMinutes} dakika",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = TimeFormatter.formatDate(session.timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
