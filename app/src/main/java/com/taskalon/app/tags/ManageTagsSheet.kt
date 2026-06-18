package com.taskalon.app.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.taskalon.app.TaskalonViewModel
import com.taskalon.app.UiState
import com.taskalon.app.data.Tag
import com.taskalon.app.ui.components.TaskalonBottomSheet
import com.taskalon.app.ui.theme.LocalAccent
import com.taskalon.app.ui.theme.LocalAppFontFamily
import com.taskalon.app.ui.theme.LocalTaskalonColors
import com.taskalon.app.ui.theme.TkText

@Composable
fun ManageTagsSheet(state: UiState, vm: TaskalonViewModel) {
    val colors = LocalTaskalonColors.current
    val accent = LocalAccent.current
    val fontFamily = LocalAppFontFamily.current

    TaskalonBottomSheet(title = "Manage tags", heightFraction = 0.8f, onDismiss = vm::closeTagSheet) {
        var newTag by remember { mutableStateOf("") }
        val add = {
            if (newTag.isNotBlank()) {
                vm.addTag(newTag)
                newTag = ""
            }
        }

        // Add row
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(13.dp))
                    .background(colors.surface2)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                if (newTag.isEmpty()) {
                    Text("New tag name", style = TkText.body.copy(fontFamily = fontFamily), color = colors.fg3)
                }
                BasicTextField(
                    value = newTag,
                    onValueChange = { newTag = it },
                    singleLine = true,
                    textStyle = TkText.body.copy(fontFamily = fontFamily, color = colors.fg1),
                    cursorBrush = SolidColor(accent),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { add() }),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Box(
                Modifier
                    .clip(RoundedCornerShape(11.dp))
                    .background(accent)
                    .clickable { add() }
                    .padding(horizontal = 18.dp, vertical = 13.dp),
            ) {
                Text("Add", style = TkText.bodyMed.copy(fontFamily = fontFamily), color = Color.White)
            }
        }

        Spacer(Modifier.height(16.dp))

        if (state.tags.isEmpty()) {
            Text(
                "No tags yet. Add one above.",
                style = TkText.body.copy(fontFamily = fontFamily),
                color = colors.fg3,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        } else {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(13.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(13.dp)),
            ) {
                state.tags.forEachIndexed { i, tag ->
                    TagRow(
                        tag = tag,
                        onRename = { vm.renameTag(tag.id, it) },
                        onDelete = { vm.deleteTag(tag.id) },
                    )
                    if (i < state.tags.lastIndex) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Spacer(Modifier.navigationBarsPadding())
    }
}

@Composable
private fun TagRow(tag: Tag, onRename: (String) -> Unit, onDelete: () -> Unit) {
    val colors = LocalTaskalonColors.current
    val accent = LocalAccent.current
    val fontFamily = LocalAppFontFamily.current
    var name by remember(tag.id) { mutableStateOf(tag.name) }

    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Sell, contentDescription = null, tint = colors.fg3, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = name,
            onValueChange = { name = it; onRename(it) },
            singleLine = true,
            textStyle = TkText.body.copy(fontFamily = fontFamily, color = colors.fg1),
            cursorBrush = SolidColor(accent),
            modifier = Modifier.weight(1f),
        )
        Box(
            Modifier.size(34.dp).clip(RoundedCornerShape(9.dp)).clickable(onClick = onDelete),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete tag", tint = colors.fg3, modifier = Modifier.size(18.dp))
        }
    }
}
