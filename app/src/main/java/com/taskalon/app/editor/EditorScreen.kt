package com.taskalon.app.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taskalon.app.TaskalonViewModel
import com.taskalon.app.UiState
import com.taskalon.app.data.Task
import com.taskalon.app.ui.components.SegmentOption
import com.taskalon.app.ui.components.SegmentedControl
import com.taskalon.app.ui.components.TaskCheckbox
import com.taskalon.app.ui.theme.LocalAccent
import com.taskalon.app.ui.theme.LocalAppFontFamily
import com.taskalon.app.ui.theme.LocalTaskalonColors
import com.taskalon.app.ui.theme.OverdueRed
import com.taskalon.app.ui.theme.PriorityColors
import com.taskalon.app.ui.theme.TkText
import com.taskalon.app.util.DueQuick
import com.taskalon.app.util.dueLabel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditorScreen(state: UiState, vm: TaskalonViewModel) {
    val colors = LocalTaskalonColors.current
    val accent = LocalAccent.current
    val fontFamily = LocalAppFontFamily.current
    val task = state.activeTask ?: return
    val hasContent = !task.isEmpty

    var titleTfv by remember(task.id) { mutableStateOf(TextFieldValue(task.title)) }
    var notesTfv by remember(task.id) { mutableStateOf(TextFieldValue(task.notes)) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {

            // Toolbar
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .padding(horizontal = 8.dp)
                    .height(52.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ToolbarIcon(Icons.AutoMirrored.Filled.ArrowBack, "Back") { vm.closeEditor() }
                Spacer(Modifier.weight(1f))
                if (hasContent) {
                    SaveIndicator(saving = state.saving)
                    Spacer(Modifier.width(6.dp))
                }
                ToolbarIcon(Icons.Filled.MoreVert, "More") { vm.openMenu() }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))

            // Body
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
                    .padding(bottom = 40.dp),
            ) {
                // Title row
                Row(verticalAlignment = Alignment.Top) {
                    if (hasContent) {
                        TaskCheckbox(
                            checked = task.done,
                            onToggle = { vm.toggleDone(task.id) },
                            size = 28.dp,
                            corner = 9.dp,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                    }
                    BasicTextField(
                        value = titleTfv,
                        onValueChange = { titleTfv = it; vm.setTitle(task.id, it.text) },
                        textStyle = TkText.editorTitle.copy(
                            fontFamily = fontFamily,
                            color = if (task.done) colors.fg3 else colors.fg1,
                            textDecoration = if (task.done) TextDecoration.LineThrough else null,
                        ),
                        cursorBrush = SolidColor(accent),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            Box {
                                if (titleTfv.text.isEmpty()) {
                                    Text(
                                        "What needs doing?",
                                        style = TkText.editorTitle.copy(fontFamily = fontFamily),
                                        color = colors.fg3,
                                    )
                                }
                                inner()
                            }
                        },
                    )
                }

                Spacer(Modifier.height(26.dp))

                // TAGS
                Section("Tags") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.tags.forEach { tag ->
                            val active = task.tags.contains(tag.id)
                            TogglePill(tag.name, active) { vm.toggleTag(task.id, tag.id) }
                        }
                        NewTagPill { vm.openTagSheet() }
                    }
                }

                // PRIORITY
                Section("Priority") {
                    SegmentedControl(
                        options = listOf(
                            SegmentOption("None"),
                            SegmentOption("Low", PriorityColors[0]),
                            SegmentOption("Med", PriorityColors[1]),
                            SegmentOption("High", PriorityColors[2]),
                        ),
                        selected = task.priority,
                        onSelect = { vm.setPriority(task.id, it) },
                    )
                }

                // DUE DATE
                Section("Due date") {
                    DueDateField(task, vm)
                }

                // NOTES
                Section("Notes") {
                    BasicTextField(
                        value = notesTfv,
                        onValueChange = { notesTfv = it; vm.setNotes(task.id, it.text) },
                        textStyle = TkText.body.copy(fontFamily = fontFamily, lineHeight = 24.sp, color = colors.fg1),
                        cursorBrush = SolidColor(accent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(13.dp))
                            .background(colors.surface)
                            .border(1.dp, colors.border, RoundedCornerShape(13.dp))
                            .defaultMinSize(minHeight = 130.dp)
                            .padding(14.dp),
                        decorationBox = { inner ->
                            Box(Modifier.fillMaxWidth()) {
                                if (notesTfv.text.isEmpty()) {
                                    Text(
                                        "Add details, links, sub-steps…",
                                        style = TkText.body.copy(fontFamily = fontFamily),
                                        color = colors.fg3,
                                    )
                                }
                                inner()
                            }
                        },
                    )
                }
            }
        }

        // Overflow menu
        if (state.menuOpen) {
            OverflowMenu(
                onDismiss = { vm.closeMenu() },
                onDuplicate = { vm.duplicateTask(task.id) },
                onSettings = { vm.openSettings() },
                onDelete = { vm.deleteTask(task.id) },
            )
        }
    }
}

@Composable
private fun ColumnScope.Section(label: String, content: @Composable ColumnScope.() -> Unit) {
    val colors = LocalTaskalonColors.current
    val fontFamily = LocalAppFontFamily.current
    Column(Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        Text(label.uppercase(), style = TkText.sectionLabel.copy(fontFamily = fontFamily), color = colors.fg3)
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun ToolbarIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, onClick: () -> Unit) {
    val colors = LocalTaskalonColors.current
    Box(
        Modifier.size(40.dp).clip(RoundedCornerShape(11.dp)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = desc, tint = colors.fg2, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun SaveIndicator(saving: Boolean) {
    val colors = LocalTaskalonColors.current
    val fontFamily = LocalAppFontFamily.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(if (saving) colors.fg3 else Color(0xFF3FA66A)))
        Spacer(Modifier.width(6.dp))
        Text(
            if (saving) "Saving…" else "Saved",
            style = TkText.body.copy(fontFamily = fontFamily, fontSize = 13.sp),
            color = colors.fg3,
        )
    }
}

@Composable
private fun TogglePill(label: String, active: Boolean, onClick: () -> Unit) {
    val colors = LocalTaskalonColors.current
    val accent = LocalAccent.current
    val fontFamily = LocalAppFontFamily.current
    val shape = RoundedCornerShape(11.dp)
    Box(
        Modifier
            .clip(shape)
            .background(if (active) accent else colors.surface2)
            .then(if (active) Modifier else Modifier.border(1.dp, colors.border, shape))
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 7.dp),
    ) {
        Text(
            label,
            style = TkText.chip.copy(fontFamily = fontFamily),
            color = if (active) Color.White else colors.fg2,
        )
    }
}

@Composable
private fun NewTagPill(onClick: () -> Unit) {
    val colors = LocalTaskalonColors.current
    val fontFamily = LocalAppFontFamily.current
    val shape = RoundedCornerShape(11.dp)
    Box(
        Modifier
            .clip(shape)
            .border(1.dp, colors.fg3, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 7.dp),
    ) {
        Text("+ New", style = TkText.chip.copy(fontFamily = fontFamily), color = colors.fg3)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnScope.DueDateField(task: Task, vm: TaskalonViewModel) {
    val colors = LocalTaskalonColors.current
    val accent = LocalAccent.current
    val fontFamily = LocalAppFontFamily.current
    var showPicker by remember { mutableStateOf(false) }

    val label = dueLabel(task.due)?.text ?: "No date set"
    val shape = RoundedCornerShape(13.dp)

    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.border, shape)
            .clickable { showPicker = true }
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = colors.fg2, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(
            label,
            style = TkText.body.copy(fontFamily = fontFamily),
            color = if (task.due.isBlank()) colors.fg3 else colors.fg1,
            modifier = Modifier.weight(1f),
        )
        if (task.due.isNotBlank()) {
            Box(
                Modifier.size(28.dp).clip(CircleShape).background(colors.surface2).clickable { vm.setDue(task.id, "") },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Clear date", tint = colors.fg2, modifier = Modifier.size(16.dp))
            }
        }
    }

    Spacer(Modifier.height(10.dp))

    // Quick-set pills
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        QuickPill("Today", task.due == DueQuick.today()) { vm.setDue(task.id, DueQuick.today()) }
        QuickPill("Tomorrow", task.due == DueQuick.tomorrow()) { vm.setDue(task.id, DueQuick.tomorrow()) }
        QuickPill("This weekend", task.due == DueQuick.thisWeekend()) { vm.setDue(task.id, DueQuick.thisWeekend()) }
        QuickPill("Next week", task.due == DueQuick.nextWeek()) { vm.setDue(task.id, DueQuick.nextWeek()) }
    }

    if (showPicker) {
        val initMillis = task.due.takeIf { it.isNotBlank() }?.let {
            runCatching { LocalDate.parse(it).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }.getOrNull()
        }
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initMillis)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { ms ->
                        val d = Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()
                        vm.setDue(task.id, d.toString())
                    }
                    showPicker = false
                }) { Text("OK", color = accent) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel", color = colors.fg2) }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun QuickPill(label: String, active: Boolean, onClick: () -> Unit) {
    val colors = LocalTaskalonColors.current
    val accent = LocalAccent.current
    val fontFamily = LocalAppFontFamily.current
    val shape = RoundedCornerShape(10.dp)
    Box(
        Modifier
            .clip(shape)
            .background(if (active) accent else colors.surface2)
            .then(if (active) Modifier else Modifier.border(1.dp, colors.border, shape))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            label,
            style = TkText.badge.copy(fontFamily = fontFamily, fontSize = 12.5.sp),
            color = if (active) Color.White else colors.fg2,
        )
    }
}

@Composable
private fun OverflowMenu(
    onDismiss: () -> Unit,
    onDuplicate: () -> Unit,
    onSettings: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = LocalTaskalonColors.current
    val fontFamily = LocalAppFontFamily.current
    Box(Modifier.fillMaxSize().clickable(onClick = onDismiss)) {
        Column(
            Modifier
                .statusBarsPadding()
                .padding(top = 50.dp, end = 12.dp)
                .align(Alignment.TopEnd)
                .width(200.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(15.dp))
                .padding(vertical = 6.dp),
        ) {
            MenuRow("Duplicate task", Icons.Filled.ContentCopy, colors.fg1) { onDuplicate() }
            MenuRow("Settings", Icons.Filled.Settings, colors.fg1) { onSettings() }
            Box(Modifier.fillMaxWidth().padding(vertical = 5.dp).height(1.dp).background(colors.border))
            MenuRow("Delete task", Icons.Filled.Delete, OverdueRed) { onDelete() }
        }
    }
}

@Composable
private fun MenuRow(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, onClick: () -> Unit) {
    val fontFamily = LocalAppFontFamily.current
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, style = TextStyle(fontFamily = fontFamily, fontSize = 15.sp), color = tint)
    }
}
