package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.UserSettingsEntity
import com.example.ui.components.DurationSettingCard
import com.example.ui.components.SwitchSettingCard

@Composable
fun SettingsScreen(
    userSettings: UserSettingsEntity,
    onUpdateSettings: (
        focusMinutes: Int,
        shortBreakMinutes: Int,
        longBreakMinutes: Int,
        longBreakInterval: Int,
        autoStartBreaks: Boolean,
        autoStartPomodoros: Boolean,
        soundEnabled: Boolean,
        vibrationEnabled: Boolean,
        dailyGoal: Int
    ) -> Unit,
    onResetDefaults: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showResetDialog by remember { mutableStateOf(false) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("settings_screen_scroll")
    ) {
        // Section: Timer Durations
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Zamanlayıcı Süreleri",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Focus duration
        item {
            DurationSettingCard(
                title = "Odaklanma Süresi",
                subtitle = "Her odak seansı için ayrılan süre",
                valueMinutes = userSettings.focusDurationMinutes,
                range = 1f..90f,
                accentColor = MaterialTheme.colorScheme.primary,
                onValueChange = { newVal ->
                    onUpdateSettings(
                        newVal,
                        userSettings.shortBreakDurationMinutes,
                        userSettings.longBreakDurationMinutes,
                        userSettings.longBreakInterval,
                        userSettings.autoStartBreaks,
                        userSettings.autoStartPomodoros,
                        userSettings.soundAlertsEnabled,
                        userSettings.vibrationEnabled,
                        userSettings.dailyGoalPomodoros
                    )
                }
            )
        }

        // Short break duration
        item {
            DurationSettingCard(
                title = "Kısa Mola Süresi",
                subtitle = "Odak seansları arasındaki dinlenme",
                valueMinutes = userSettings.shortBreakDurationMinutes,
                range = 1f..30f,
                accentColor = Color(0xFF10B981),
                onValueChange = { newVal ->
                    onUpdateSettings(
                        userSettings.focusDurationMinutes,
                        newVal,
                        userSettings.longBreakDurationMinutes,
                        userSettings.longBreakInterval,
                        userSettings.autoStartBreaks,
                        userSettings.autoStartPomodoros,
                        userSettings.soundAlertsEnabled,
                        userSettings.vibrationEnabled,
                        userSettings.dailyGoalPomodoros
                    )
                }
            )
        }

        // Long break duration
        item {
            DurationSettingCard(
                title = "Uzun Mola Süresi",
                subtitle = "Tam bir döngü bittiğinde verilecek mola",
                valueMinutes = userSettings.longBreakDurationMinutes,
                range = 1f..60f,
                accentColor = Color(0xFF6366F1),
                onValueChange = { newVal ->
                    onUpdateSettings(
                        userSettings.focusDurationMinutes,
                        userSettings.shortBreakDurationMinutes,
                        newVal,
                        userSettings.longBreakInterval,
                        userSettings.autoStartBreaks,
                        userSettings.autoStartPomodoros,
                        userSettings.soundAlertsEnabled,
                        userSettings.vibrationEnabled,
                        userSettings.dailyGoalPomodoros
                    )
                }
            )
        }

        // Long break interval
        item {
            DurationSettingCard(
                title = "Uzun Mola Sıklığı",
                subtitle = "Kaç pomodoro sonra uzun mola verilsin",
                valueMinutes = userSettings.longBreakInterval,
                range = 2f..10f,
                unitLabel = "pomodoro",
                accentColor = MaterialTheme.colorScheme.primary,
                onValueChange = { newVal ->
                    onUpdateSettings(
                        userSettings.focusDurationMinutes,
                        userSettings.shortBreakDurationMinutes,
                        userSettings.longBreakDurationMinutes,
                        newVal,
                        userSettings.autoStartBreaks,
                        userSettings.autoStartPomodoros,
                        userSettings.soundAlertsEnabled,
                        userSettings.vibrationEnabled,
                        userSettings.dailyGoalPomodoros
                    )
                }
            )
        }

        // Section: Goals
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TrackChanges,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Hedefler",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        item {
            DurationSettingCard(
                title = "Günlük Hedef",
                subtitle = "Günde tamamlamayı hedeflediğiniz pomodoro sayısı",
                valueMinutes = userSettings.dailyGoalPomodoros,
                range = 1f..20f,
                unitLabel = "pomodoro",
                accentColor = MaterialTheme.colorScheme.primary,
                onValueChange = { newVal ->
                    onUpdateSettings(
                        userSettings.focusDurationMinutes,
                        userSettings.shortBreakDurationMinutes,
                        userSettings.longBreakDurationMinutes,
                        userSettings.longBreakInterval,
                        userSettings.autoStartBreaks,
                        userSettings.autoStartPomodoros,
                        userSettings.soundAlertsEnabled,
                        userSettings.vibrationEnabled,
                        newVal
                    )
                }
            )
        }

        // Section: Automation & Alerts
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Otomasyon & Bildirimler",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        item {
            SwitchSettingCard(
                title = "Molaları Otomatik Başlat",
                subtitle = "Odak seansı bitince molayı hemen başlat",
                checked = userSettings.autoStartBreaks,
                onCheckedChange = { newVal ->
                    onUpdateSettings(
                        userSettings.focusDurationMinutes,
                        userSettings.shortBreakDurationMinutes,
                        userSettings.longBreakDurationMinutes,
                        userSettings.longBreakInterval,
                        newVal,
                        userSettings.autoStartPomodoros,
                        userSettings.soundAlertsEnabled,
                        userSettings.vibrationEnabled,
                        userSettings.dailyGoalPomodoros
                    )
                }
            )
        }

        item {
            SwitchSettingCard(
                title = "Pomodoroları Otomatik Başlat",
                subtitle = "Mola bitince sonraki odak seansını hemen başlat",
                checked = userSettings.autoStartPomodoros,
                onCheckedChange = { newVal ->
                    onUpdateSettings(
                        userSettings.focusDurationMinutes,
                        userSettings.shortBreakDurationMinutes,
                        userSettings.longBreakDurationMinutes,
                        userSettings.longBreakInterval,
                        userSettings.autoStartBreaks,
                        newVal,
                        userSettings.soundAlertsEnabled,
                        userSettings.vibrationEnabled,
                        userSettings.dailyGoalPomodoros
                    )
                }
            )
        }

        item {
            SwitchSettingCard(
                title = "Sesli Uyarılar",
                subtitle = "Seans başladığında ve bittiğinde ses çal",
                checked = userSettings.soundAlertsEnabled,
                onCheckedChange = { newVal ->
                    onUpdateSettings(
                        userSettings.focusDurationMinutes,
                        userSettings.shortBreakDurationMinutes,
                        userSettings.longBreakDurationMinutes,
                        userSettings.longBreakInterval,
                        userSettings.autoStartBreaks,
                        userSettings.autoStartPomodoros,
                        newVal,
                        userSettings.vibrationEnabled,
                        userSettings.dailyGoalPomodoros
                    )
                }
            )
        }

        item {
            SwitchSettingCard(
                title = "Titreşim Uyarısı",
                subtitle = "Seans bitiminde titreşim ile haber ver",
                checked = userSettings.vibrationEnabled,
                onCheckedChange = { newVal ->
                    onUpdateSettings(
                        userSettings.focusDurationMinutes,
                        userSettings.shortBreakDurationMinutes,
                        userSettings.longBreakDurationMinutes,
                        userSettings.longBreakInterval,
                        userSettings.autoStartBreaks,
                        userSettings.autoStartPomodoros,
                        userSettings.soundAlertsEnabled,
                        newVal,
                        userSettings.dailyGoalPomodoros
                    )
                }
            )
        }

        // Reset to Defaults Button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showResetDialog = true },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_reset_settings")
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Varsayılan Ayarlara Sıfırla")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Ayarları Sıfırla") },
            text = { Text("Tüm odak ve mola süreleri varsayılan değerlerine (25 dk odak, 5 dk mola) döndürülecek. Devam etmek istiyor musunuz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetDefaults()
                        showResetDialog = false
                    }
                ) {
                    Text("Sıfırla", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}
