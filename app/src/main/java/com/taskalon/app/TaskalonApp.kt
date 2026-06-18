package com.taskalon.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.taskalon.app.editor.EditorScreen
import com.taskalon.app.library.LibraryScreen
import com.taskalon.app.settings.SettingsSheet
import com.taskalon.app.tags.ManageTagsSheet
import com.taskalon.app.ui.components.TaskalonToast
import com.taskalon.app.ui.theme.LocalTaskalonColors
import com.taskalon.app.ui.theme.TaskalonTheme

@Composable
fun TaskalonApp() {
    val vm: TaskalonViewModel = viewModel()
    val state by vm.state.collectAsState()

    // System back: dismiss an open overlay, else leave the editor. Only falls through to the
    // OS (exits the app) on the bare task list with nothing open.
    BackHandler(enabled = state.tagSheetOpen || state.settingsOpen || state.menuOpen || state.screen == Screen.Editor) {
        when {
            state.tagSheetOpen -> vm.closeTagSheet()
            state.settingsOpen -> vm.closeSettings()
            state.menuOpen -> vm.closeMenu()
            state.screen == Screen.Editor -> vm.closeEditor()
        }
    }

    TaskalonTheme(state.settings) {
        val colors = LocalTaskalonColors.current
        Box(Modifier.fillMaxSize().background(colors.bg)) {
            if (state.loaded) {
                when (state.screen) {
                    Screen.Library -> LibraryScreen(state, vm)
                    Screen.Editor -> EditorScreen(state, vm)
                }
            }
            if (state.settingsOpen) SettingsSheet(state, vm)
            if (state.tagSheetOpen) ManageTagsSheet(state, vm)
            state.toast?.let { TaskalonToast(it) }
        }
    }
}
