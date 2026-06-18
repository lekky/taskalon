package com.taskalon.app.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale

enum class DueStyle { OVERDUE, SOON, FUTURE }

data class DueLabel(val text: String, val style: DueStyle)

/** Human due-date label + semantic style for a `YYYY-MM-DD` string (null if unset/invalid). */
fun dueLabel(due: String, today: LocalDate = LocalDate.now()): DueLabel? {
    if (due.isBlank()) return null
    val date = runCatching { LocalDate.parse(due) }.getOrNull() ?: return null
    val days = ChronoUnit.DAYS.between(today, date).toInt()
    return when {
        days < 0 -> DueLabel(if (days == -1) "Yesterday" else "${-days}d overdue", DueStyle.OVERDUE)
        days == 0 -> DueLabel("Today", DueStyle.SOON)
        days == 1 -> DueLabel("Tomorrow", DueStyle.SOON)
        days in 2..6 -> DueLabel(date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()), DueStyle.FUTURE)
        else -> DueLabel(date.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())), DueStyle.FUTURE)
    }
}

/** The four due-date quick-sets. "This weekend" = the next future Saturday (today excluded). */
object DueQuick {
    fun today(t: LocalDate = LocalDate.now()): String = t.toString()
    fun tomorrow(t: LocalDate = LocalDate.now()): String = t.plusDays(1).toString()
    fun thisWeekend(t: LocalDate = LocalDate.now()): String =
        t.with(TemporalAdjusters.next(DayOfWeek.SATURDAY)).toString()
    fun nextWeek(t: LocalDate = LocalDate.now()): String = t.plusDays(7).toString()
}
