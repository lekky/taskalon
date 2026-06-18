package com.taskalon.app.settings

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taskalon.app.TaskalonViewModel
import com.taskalon.app.UiState
import com.taskalon.app.data.Accents
import com.taskalon.app.data.AppFont
import com.taskalon.app.data.SortMode
import com.taskalon.app.data.ThemeMode
import com.taskalon.app.ui.components.SegmentOption
import com.taskalon.app.ui.components.SegmentedControl
import com.taskalon.app.ui.components.TaskalonBottomSheet
import com.taskalon.app.ui.theme.LocalAccent
import com.taskalon.app.ui.theme.LocalAppFontFamily
import com.taskalon.app.ui.theme.LocalTaskalonColors
import com.taskalon.app.ui.theme.TkText
import com.taskalon.app.ui.theme.hexColor
import com.taskalon.app.ui.theme.toFontFamily

@Composable
fun SettingsSheet(state: UiState, vm: TaskalonViewModel) {
    TaskalonBottomSheet(title = "Settings", heightFraction = 0.88f, onDismiss = vm::closeSettings) {
        val s = state.settings

        Section("Appearance") {
            SegmentedControl(
                options = listOf(SegmentOption("System"), SegmentOption("Light"), SegmentOption("Dark")),
                selected = when (s.theme) {
                    ThemeMode.SYSTEM -> 0; ThemeMode.LIGHT -> 1; ThemeMode.DARK -> 2
                },
                onSelect = {
                    vm.setTheme(when (it) { 0 -> ThemeMode.SYSTEM; 1 -> ThemeMode.LIGHT; else -> ThemeMode.DARK })
                },
            )
        }

        Section("Accent color") {
            AccentSwatches(current = s.accent, onPick = vm::setAccent)
        }

        Section("Font") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppFont.entries.forEach { font ->
                    FontRow(font = font, active = s.font == font) { vm.setFont(font) }
                }
            }
        }

        Section("Sort") {
            SegmentedControl(
                options = listOf(SegmentOption("Manual"), SegmentOption("Due date"), SegmentOption("Priority")),
                selected = when (s.sort) {
                    SortMode.MANUAL -> 0; SortMode.DUE -> 1; SortMode.PRIORITY -> 2
                },
                onSelect = {
                    vm.setSort(when (it) { 0 -> SortMode.MANUAL; 1 -> SortMode.DUE; else -> SortMode.PRIORITY })
                },
            )
        }

        Section("Completed") {
            ToggleRow("Show completed", s.showCompleted, vm::setShowCompleted)
        }

        Section("Tags") {
            ManageTagsRow(count = state.tags.size, onClick = vm::openTagSheet)
        }

        Spacer(Modifier.height(8.dp))
        Spacer(Modifier.navigationBarsPadding())
    }
}

@Composable
private fun ColumnScope.Section(label: String, content: @Composable ColumnScope.() -> Unit) {
    val colors = LocalTaskalonColors.current
    val fontFamily = LocalAppFontFamily.current
    Column(Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        Text(label.uppercase(), style = TkText.sectionLabel.copy(fontFamily = fontFamily), color = colors.fg3)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccentSwatches(current: String, onPick: (String) -> Unit) {
    val colors = LocalTaskalonColors.current
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Accents.entries.forEach { a ->
            val selected = a.hex.equals(current, ignoreCase = true)
            Box(
                Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .then(if (selected) Modifier.border(2.dp, colors.fg1, CircleShape) else Modifier)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(hexColor(a.hex))
                    .clickable { onPick(a.hex) },
            )
        }
    }
}

@Composable
private fun FontRow(font: AppFont, active: Boolean, onClick: () -> Unit) {
    val colors = LocalTaskalonColors.current
    val accent = LocalAccent.current
    val fontFamily = LocalAppFontFamily.current
    val family = font.toFontFamily()
    val shape = RoundedCornerShape(13.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.bg)
            .border(1.dp, colors.border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Aa", style = TkText.body.copy(fontFamily = family, fontSize = 22.sp, fontWeight = FontWeight.SemiBold), color = colors.fg1)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(font.label, style = TkText.bodyMed.copy(fontFamily = fontFamily), color = colors.fg1)
            Text(font.descriptor, style = TkText.body.copy(fontFamily = fontFamily, fontSize = 13.sp), color = colors.fg3)
        }
        if (active) {
            Icon(Icons.Filled.Check, contentDescription = "Selected", tint = accent, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val colors = LocalTaskalonColors.current
    val accent = LocalAccent.current
    val fontFamily = LocalAppFontFamily.current
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = TkText.bodyMed.copy(fontFamily = fontFamily), color = colors.fg1, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accent,
                uncheckedTrackColor = colors.surface2,
                uncheckedBorderColor = colors.border,
            ),
        )
    }
}

@Composable
private fun ManageTagsRow(count: Int, onClick: () -> Unit) {
    val colors = LocalTaskalonColors.current
    val fontFamily = LocalAppFontFamily.current
    val shape = RoundedCornerShape(13.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.bg)
            .border(1.dp, colors.border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Manage tags", style = TkText.bodyMed.copy(fontFamily = fontFamily), color = colors.fg1, modifier = Modifier.weight(1f))
        Text("$count", style = TkText.body.copy(fontFamily = fontFamily, fontSize = 14.sp), color = colors.fg3)
        Spacer(Modifier.width(8.dp))
        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = colors.fg3, modifier = Modifier.size(20.dp))
    }
}
