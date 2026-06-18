package com.taskalon.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.taskalon.app.ui.theme.LocalAccent
import com.taskalon.app.ui.theme.LocalAppFontFamily
import com.taskalon.app.ui.theme.LocalTaskalonColors
import com.taskalon.app.ui.theme.TkText

/** Square task checkbox. Unchecked = `border` outline; checked = accent fill + white check. */
@Composable
fun TaskCheckbox(
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 25.dp,
    corner: Dp = 8.dp,
) {
    val colors = LocalTaskalonColors.current
    val accent = LocalAccent.current
    val shape = RoundedCornerShape(corner)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(if (checked) accent else Color.Transparent)
            .border(width = if (checked) 0.dp else 2.dp, color = if (checked) Color.Transparent else colors.border, shape = shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(size * 0.66f))
        }
    }
}

/** Accent rounded-square logo holding a white checkmark. */
@Composable
fun TaskalonLogo(modifier: Modifier = Modifier, size: Dp = 32.dp) {
    val accent = LocalAccent.current
    Box(
        modifier = modifier.size(size).clip(RoundedCornerShape(9.dp)).background(accent),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(size * 0.58f))
    }
}

data class SegmentOption(val label: String, val dotColor: Color? = null)

/** Track `surface-2` / 12px radius / 3px padding; active thumb `surface` + shadow, 9px radius. */
@Composable
fun SegmentedControl(
    options: List<SegmentOption>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalTaskalonColors.current
    val fontFamily = LocalAppFontFamily.current
    val thumbShape = RoundedCornerShape(9.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface2)
            .padding(3.dp),
    ) {
        options.forEachIndexed { i, opt ->
            val active = i == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .then(if (active) Modifier.shadow(2.dp, thumbShape) else Modifier)
                    .background(if (active) colors.surface else Color.Transparent, thumbShape)
                    .clip(thumbShape)
                    .clickable { onSelect(i) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    opt.dotColor?.let {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(it))
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        opt.label,
                        style = (if (active) TkText.chipActive else TkText.chip).copy(fontFamily = fontFamily),
                        color = if (active) colors.fg1 else colors.fg2,
                    )
                }
            }
        }
    }
}

/** Circular close (×) button used in sheet headers. */
@Composable
fun CloseButton(onClick: () -> Unit) {
    val colors = LocalTaskalonColors.current
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(colors.surface2)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.Close, contentDescription = "Close", tint = colors.fg2, modifier = Modifier.size(18.dp))
    }
}

/**
 * Bottom sheet matching the handoff mechanics: scrim (tap to dismiss), slide-up enter
 * (~.28s), 24px top corners, grab handle, title + close header, scrollable content.
 */
@Composable
fun TaskalonBottomSheet(
    title: String,
    heightFraction: Float,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalTaskalonColors.current
    val fontFamily = LocalAppFontFamily.current
    val visible = remember { MutableTransitionState(false).apply { targetState = true } }

    Box(Modifier.fillMaxSize()) {
        // Scrim
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0x6614100A))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                )
        )
        AnimatedVisibility(
            visibleState = visible,
            enter = slideInVertically(animationSpec = tween(280)) { it } + fadeIn(tween(180)),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Surface(
                color = colors.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier.fillMaxWidth().fillMaxHeight(heightFraction),
            ) {
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxWidth().padding(top = 10.dp), contentAlignment = Alignment.Center) {
                        Box(Modifier.size(38.dp, 4.dp).clip(RoundedCornerShape(2.dp)).background(colors.border))
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            title,
                            style = TkText.sheetTitle.copy(fontFamily = fontFamily),
                            color = colors.fg1,
                            modifier = Modifier.weight(1f),
                        )
                        CloseButton(onDismiss)
                    }
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 18.dp),
                        content = content,
                    )
                }
            }
        }
    }
}

/** Bottom-center toast: `fg-1` background, `bg` text. */
@Composable
fun BoxScope.TaskalonToast(message: String) {
    val colors = LocalTaskalonColors.current
    val fontFamily = LocalAppFontFamily.current
    Box(
        Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(bottom = 40.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(colors.fg1)
            .padding(horizontal = 18.dp, vertical = 11.dp),
    ) {
        Text(message, style = TkText.toast.copy(fontFamily = fontFamily), color = colors.bg)
    }
}
