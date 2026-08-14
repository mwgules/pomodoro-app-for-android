package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PomodoroMode
import com.example.model.TimerState
import com.example.util.TimeFormatter

@Composable
fun CircularTimer(
    mode: PomodoroMode,
    timerState: TimerState,
    remainingSeconds: Int,
    totalSeconds: Int,
    currentCycleStep: Int,
    maxCycleSteps: Int,
    onAdjustTime: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (totalSeconds > 0) {
        remainingSeconds.toFloat() / totalSeconds.toFloat()
    } else 1f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "TimerProgress"
    )

    val activeColor = mode.primaryColor
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // Mode & Cycle Badge
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = mode.primaryColor.copy(alpha = 0.12f),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = mode.title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = mode.primaryColor
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "•",
                    color = mode.primaryColor.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Döngü $currentCycleStep/$maxCycleSteps",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // Circular Timer Canvas
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(270.dp)
                .padding(8.dp)
                .testTag("circular_timer_box")
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 14.dp.toPx()

                // Background Track
                drawCircle(
                    color = trackColor,
                    style = Stroke(width = strokeWidth)
                )

                // Progress Arc (runs clockwise, from top -90 deg)
                val sweepAngle = animatedProgress * 360f
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to activeColor.copy(alpha = 0.85f),
                        1.0f to activeColor
                    ),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round
                    )
                )
            }

            // Center Content: Time & State
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = TimeFormatter.formatSecondsToMinutesAndSeconds(remainingSeconds),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("timer_countdown_text")
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = when (timerState) {
                        TimerState.RUNNING -> "Çalışıyor..."
                        TimerState.PAUSED -> "Duraklatıldı"
                        TimerState.COMPLETED -> "Tamamlandı!"
                        TimerState.IDLE -> mode.subtitle
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (timerState == TimerState.RUNNING) mode.primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (timerState == TimerState.RUNNING) FontWeight.SemiBold else FontWeight.Normal
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Cycle dots indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..maxCycleSteps) {
                        val isFilled = i <= currentCycleStep
                        Box(
                            modifier = Modifier
                                .size(if (i == currentCycleStep) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) mode.primaryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick adjust buttons (+5m, -1m)
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            AssistChip(
                onClick = { onAdjustTime(-60) },
                label = { Text("-1 dk") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "1 dakika azalt",
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier.testTag("chip_minus_1m")
            )

            Spacer(modifier = Modifier.width(12.dp))

            AssistChip(
                onClick = { onAdjustTime(300) },
                label = { Text("+5 dk") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "5 dakika ekle",
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier.testTag("chip_plus_5m")
            )
        }
    }
}
