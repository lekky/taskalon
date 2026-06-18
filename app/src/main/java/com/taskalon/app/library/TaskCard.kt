package com.taskalon.app.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.taskalon.app.data.Tag
import com.taskalon.app.data.Task
import com.taskalon.app.ui.components.TaskCheckbox
import com.taskalon.app.ui.theme.LocalAccent
import com.taskalon.app.ui.theme.LocalAppFontFamily
import com.taskalon.app.ui.theme.LocalTaskalonColors
import com.taskalon.app.ui.theme.OverdueRed
import com.taskalon.app.ui.theme.TaskalonColors
import com.taskalon.app.ui.theme.TkText
import com.taskalon.app.ui.theme.priorityColor
import com.taskalon.app.util.DueLabel
import com.taskalon.app.util.DueStyle
import com.taskalon.app.util.dueLabel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskCard(
    task: Task,
    tagsById: Map<String, Tag>,
    dragging: Boolean,
    onToggleDone: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTaskalonColors.current
    val accent = LocalAccent.current
    val fontFamily = LocalAppFontFamily.current
    val due = dueLabel(task.due)
    val tagNames = task.tags.mapNotNull { tagsById[it]?.name }
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (task.done) 0.58f else 1f)
            .then(if (dragging) Modifier.shadow(14.dp, shape) else Modifier)
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, if (dragging) accent else colors.border, shape)
            .clickable(onClick = onClick)
            .padding(15.dp),
        horizontalArrangement = Arrangement.spacedBy(13.dp),
        verticalAlignment = Alignment.Top,
    ) {
        TaskCheckbox(checked = task.done, onToggle = onToggleDone)

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(
                task.title.ifBlank { "Untitled task" },
                style = TkText.cardTitle.copy(
                    fontFamily = fontFamily,
                    textDecoration = if (task.done) TextDecoration.LineThrough else null,
                ),
                color = if (task.done) colors.fg3 else colors.fg1,
            )
            if (task.notes.isNotBlank() && !task.done) {
                Text(
                    task.notes,
                    style = TkText.cardNotes.copy(fontFamily = fontFamily),
                    color = colors.fg3,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (due != null || tagNames.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    if (due != null) DueBadge(due, accent, colors, fontFamily)
                    tagNames.forEach { TagPill(it, colors, fontFamily) }
                }
            }
        }

        val pColor = priorityColor(task.priority)
        if (pColor != null && !task.done) {
            Box(Modifier.padding(top = 4.dp).size(8.dp).clip(CircleShape).background(pColor))
        }
    }
}

@Composable
private fun DueBadge(due: DueLabel, accent: Color, colors: TaskalonColors, fontFamily: FontFamily) {
    val fg: Color
    val bg: Color
    when (due.style) {
        DueStyle.OVERDUE -> { fg = OverdueRed; bg = OverdueRed.copy(alpha = 0.12f) }
        DueStyle.SOON -> { fg = accent; bg = accent.copy(alpha = 0.15f) }
        DueStyle.FUTURE -> { fg = colors.fg2; bg = colors.surface2 }
    }
    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(bg).padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(due.text, style = TkText.badgeStrong.copy(fontFamily = fontFamily), color = fg)
    }
}

@Composable
private fun TagPill(name: String, colors: TaskalonColors, fontFamily: FontFamily) {
    Box(Modifier.clip(RoundedCornerShape(8.dp)).background(colors.surface2).padding(horizontal = 8.dp, vertical = 3.dp)) {
        androidx.compose.material3.Text(name, style = TkText.badge.copy(fontFamily = fontFamily), color = colors.fg2)
    }
}
