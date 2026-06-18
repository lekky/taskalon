package com.taskalon.app.library

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChecklistRtl
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taskalon.app.Screen
import com.taskalon.app.TaskalonViewModel
import com.taskalon.app.UiState
import com.taskalon.app.data.SortMode
import com.taskalon.app.data.Tag
import com.taskalon.app.ui.components.TaskalonLogo
import com.taskalon.app.ui.theme.LocalAccent
import com.taskalon.app.ui.theme.LocalAppFontFamily
import com.taskalon.app.ui.theme.LocalTaskalonColors
import com.taskalon.app.ui.theme.TkText
import com.taskalon.app.util.completedTasksFor
import com.taskalon.app.util.openTasksFor

@Composable
fun LibraryScreen(state: UiState, vm: TaskalonViewModel) {
    val colors = LocalTaskalonColors.current
    val accent = LocalAccent.current
    val fontFamily = LocalAppFontFamily.current

    val tagsById = remember(state.tags) { state.tags.associateBy { it.id } }
    val openTasks = openTasksFor(state.tasks, state.search, state.tagFilter, state.settings.sort)
    val completed = completedTasksFor(state.tasks, state.search, state.tagFilter)
    val openCount = state.tasks.count { !it.done }
    val doneCount = state.tasks.count { it.done }
    val canDrag = state.settings.sort == SortMode.MANUAL
    val openUpdated = rememberUpdatedState(openTasks)
    val swapPx = with(LocalDensity.current) { 64.dp.toPx() }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {

            // Header
            Row(
                Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TaskalonLogo()
                Spacer(Modifier.width(10.dp))
                Text(
                    "Taskalon",
                    style = TkText.wordmark.copy(fontFamily = fontFamily),
                    color = colors.fg1,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    Modifier.size(40.dp).clip(RoundedCornerShape(11.dp)).clickable { vm.openSettings() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = colors.fg2, modifier = Modifier.size(22.dp))
                }
            }

            // Title block
            Column(Modifier.padding(horizontal = 18.dp).padding(top = 6.dp, bottom = 14.dp)) {
                Text("Tasks", style = TkText.pageTitle.copy(fontFamily = fontFamily), color = colors.fg1)
                Spacer(Modifier.height(2.dp))
                Text(
                    "$openCount tasks open · $doneCount done",
                    style = TkText.body.copy(fontFamily = fontFamily, fontSize = 13.sp),
                    color = colors.fg3,
                )
            }

            // Tag filter row
            TagFilterRow(
                tags = state.tags,
                active = state.tagFilter,
                onSelect = vm::setTagFilter,
                onManage = vm::openTagSheet,
            )

            Spacer(Modifier.height(12.dp))

            // Search
            SearchField(state.search, vm::setSearch)

            Spacer(Modifier.height(10.dp))

            // List
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp)
                    .padding(bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                openTasks.forEach { task ->
                    val dragMod = if (canDrag) Modifier.pointerInput(task.id) {
                        var acc = 0f
                        detectDragGesturesAfterLongPress(
                            onDragStart = { vm.startDrag(task.id); acc = 0f },
                            onDragEnd = { vm.endDrag() },
                            onDragCancel = { vm.endDrag() },
                            onDrag = { change, amount ->
                                change.consume()
                                acc += amount.y
                                val list = openUpdated.value
                                val cur = list.indexOfFirst { it.id == task.id }
                                if (cur >= 0) {
                                    if (acc <= -swapPx && cur > 0) {
                                        vm.reorderOver(list[cur - 1].id); acc = 0f
                                    } else if (acc >= swapPx && cur < list.lastIndex) {
                                        vm.reorderOver(list[cur + 1].id); acc = 0f
                                    }
                                }
                            },
                        )
                    } else Modifier

                    TaskCard(
                        task = task,
                        tagsById = tagsById,
                        dragging = state.dragId == task.id,
                        onToggleDone = { vm.toggleDone(task.id) },
                        onClick = { vm.openTask(task.id) },
                        modifier = dragMod,
                    )
                }

                // Completed section
                if (state.settings.showCompleted && completed.isNotEmpty()) {
                    CompletedHeader(
                        count = completed.size,
                        open = state.completedOpen,
                        onToggle = vm::toggleCompletedOpen,
                        onClear = vm::clearCompleted,
                    )
                    if (state.completedOpen) {
                        completed.forEach { task ->
                            TaskCard(
                                task = task,
                                tagsById = tagsById,
                                dragging = false,
                                onToggleDone = { vm.toggleDone(task.id) },
                                onClick = { vm.openTask(task.id) },
                            )
                        }
                    }
                }

                // Empty state
                if (openTasks.isEmpty() && (!state.settings.showCompleted || completed.isEmpty())) {
                    val filtering = state.search.isNotBlank() || state.tagFilter != null
                    EmptyState(
                        heading = if (filtering) "Nothing here" else "All clear",
                        sub = if (filtering) "Try a different filter or search." else "Tap + to add your first task.",
                    )
                }
            }
        }

        // FAB
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 18.dp, bottom = 22.dp)
                .size(58.dp)
                .shadow(16.dp, RoundedCornerShape(19.dp), spotColor = accent, ambientColor = accent)
                .clip(RoundedCornerShape(19.dp))
                .background(accent)
                .clickable { vm.newTask() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "New task", tint = Color.White, modifier = Modifier.size(26.dp))
        }
    }
}

@Composable
private fun TagFilterRow(
    tags: List<Tag>,
    active: String?,
    onSelect: (String?) -> Unit,
    onManage: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterChip("All", active == null) { onSelect(null) }
        tags.forEach { tag -> FilterChip(tag.name, active == tag.id) { onSelect(tag.id) } }
        IconPillButton(Icons.Filled.Add, "Add tag", onManage)
        IconPillButton(Icons.Filled.Edit, "Manage tags", onManage)
    }
}

@Composable
private fun FilterChip(label: String, active: Boolean, onClick: () -> Unit) {
    val colors = LocalTaskalonColors.current
    val accent = LocalAccent.current
    val fontFamily = LocalAppFontFamily.current
    val shape = RoundedCornerShape(21.dp)
    Box(
        Modifier
            .height(42.dp)
            .clip(shape)
            .background(if (active) accent else colors.surface2)
            .then(if (active) Modifier else Modifier.border(1.dp, colors.border, shape))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = (if (active) TkText.chipActive else TkText.chip).copy(fontFamily = fontFamily),
            color = if (active) Color.White else colors.fg2,
        )
    }
}

@Composable
private fun IconPillButton(icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, onClick: () -> Unit) {
    val colors = LocalTaskalonColors.current
    val shape = RoundedCornerShape(13.dp)
    Box(
        Modifier
            .size(width = 46.dp, height = 42.dp)
            .clip(shape)
            .background(colors.surface2)
            .border(1.dp, colors.border, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = desc, tint = colors.fg2, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SearchField(value: String, onValue: (String) -> Unit) {
    val colors = LocalTaskalonColors.current
    val accent = LocalAccent.current
    val fontFamily = LocalAppFontFamily.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(colors.surface2)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = colors.fg3, modifier = Modifier.size(18.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) {
                Text("Search tasks", style = TkText.body.copy(fontFamily = fontFamily), color = colors.fg3)
            }
            BasicTextField(
                value = value,
                onValueChange = onValue,
                singleLine = true,
                textStyle = TkText.body.copy(fontFamily = fontFamily, color = colors.fg1),
                cursorBrush = SolidColor(accent),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CompletedHeader(count: Int, open: Boolean, onToggle: () -> Unit, onClear: () -> Unit) {
    val colors = LocalTaskalonColors.current
    val accent = LocalAccent.current
    val fontFamily = LocalAppFontFamily.current
    val rotation by animateFloatAsState(if (open) 90f else 0f, label = "chevron")
    Row(
        Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 2.dp).clickable(onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.fg3,
            modifier = Modifier.size(20.dp).rotate(rotation),
        )
        Spacer(Modifier.width(4.dp))
        Text("Completed", style = TkText.bodyMed.copy(fontFamily = fontFamily, fontSize = 14.sp), color = colors.fg2)
        Spacer(Modifier.width(6.dp))
        Text("$count", style = TkText.body.copy(fontFamily = fontFamily, fontSize = 13.sp), color = colors.fg3)
        Spacer(Modifier.weight(1f))
        Text(
            "Clear",
            style = TkText.body.copy(fontFamily = fontFamily, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold),
            color = accent,
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onClear).padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun EmptyState(heading: String, sub: String) {
    val colors = LocalTaskalonColors.current
    val fontFamily = LocalAppFontFamily.current
    Column(
        Modifier.fillMaxWidth().padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Filled.ChecklistRtl, contentDescription = null, tint = colors.fg3, modifier = Modifier.size(46.dp))
        Spacer(Modifier.height(14.dp))
        Text(heading, style = TkText.sheetTitle.copy(fontFamily = fontFamily, fontSize = 18.sp), color = colors.fg1)
        Spacer(Modifier.height(6.dp))
        Text(sub, style = TkText.body.copy(fontFamily = fontFamily, fontSize = 14.sp), color = colors.fg3)
    }
}
