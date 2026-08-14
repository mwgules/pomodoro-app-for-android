package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.data.local.entity.SessionType

enum class PomodoroMode(
    val title: String,
    val subtitle: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val sessionType: SessionType
) {
    FOCUS(
        title = "Odaklanma",
        subtitle = "Görevinize tam odaklanın",
        primaryColor = Color(0xFFE53935),
        secondaryColor = Color(0xFFFFCDD2),
        sessionType = SessionType.FOCUS
    ),
    SHORT_BREAK(
        title = "Kısa Mola",
        subtitle = "Derin bir nefes alın ve dinlenin",
        primaryColor = Color(0xFF10B981),
        secondaryColor = Color(0xFFA7F3D0),
        sessionType = SessionType.SHORT_BREAK
    ),
    LONG_BREAK(
        title = "Uzun Mola",
        subtitle = "Zihninizi tamamen tazeleyin",
        primaryColor = Color(0xFF6366F1),
        secondaryColor = Color(0xFFC7D2FE),
        sessionType = SessionType.LONG_BREAK
    )
}
