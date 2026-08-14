package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeFormatter {
    fun formatSecondsToMinutesAndSeconds(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    fun formatMinutesToHourMin(minutes: Int): String {
        val hours = minutes / 60
        val remainingMin = minutes % 60
        return when {
            hours > 0 && remainingMin > 0 -> "${hours}s ${remainingMin}dk"
            hours > 0 -> "${hours}s"
            else -> "${remainingMin} dk"
        }
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale("tr", "TR"))
        return sdf.format(Date(timestamp))
    }

    fun formatDayName(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEE", Locale("tr", "TR"))
        return sdf.format(Date(timestamp))
    }
}
