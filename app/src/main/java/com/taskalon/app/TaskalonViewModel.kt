package com.taskalon.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.taskalon.app.data.Settings
import com.taskalon.app.data.SortMode
import com.taskalon.app.data.AppFont
import com.taskalon.app.data.Tag
import com.taskalon.app.data.Task
import com.taskalon.app.data.TaskalonRepository
import com.taskalon.app.data.ThemeMode
import com.taskalon.app.util.completedTasksFor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class Screen { Library, Editor }

data class UiState(
    val loaded: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val settings: Settings = Settings(),
    val screen: Screen = Screen.Library,
    val activeId: String? = null,
    val search: String = "",
    val tagFilter: String? = null,
    val completedOpen: Boolean = true,
    val dragId: String? = null,
    val saving: Boolean = false,
    val settingsOpen: Boolean = false,
    val menuOpen: Boolean = false,
    val tagSheetOpen: Boolean = false,
    val toast: String? = null,
) {
    val activeTask: Task? get() = tasks.firstOrNull { it.id == activeId }
}

class TaskalonViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = TaskalonRepository(app)
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var saveJob: Job? = null
    private var toastJob: Job? = null

    init {
        viewModelScope.launch {
            val data = repo.load()
            _state.update {
                it.copy(loaded = true, tasks = data.tasks, tags = data.tags, settings = data.settings)
            }
        }
    }

    // ---- persistence ----
    private fun saveTasksDebounced() {
        _state.update { it.copy(saving = true) }
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(550)
            repo.saveTasks(_state.value.tasks)
            _state.update { it.copy(saving = false) }
        }
    }

    private fun saveTasksNow() {
        saveJob?.cancel()
        viewModelScope.launch {
            repo.saveTasks(_state.value.tasks)
            _state.update { it.copy(saving = false) }
        }
    }

    private fun saveTags() = viewModelScope.launch { repo.saveTags(_state.value.tags) }
    private fun saveSettings() = viewModelScope.launch { repo.saveSettings(_state.value.settings) }

    // ---- navigation ----
    fun newTask() {
        val now = System.currentTimeMillis()
        val task = Task(id = UUID.randomUUID().toString(), created = now, updated = now)
        _state.update {
            it.copy(tasks = listOf(task) + it.tasks, activeId = task.id, screen = Screen.Editor, menuOpen = false)
        }
        // No persist yet — it's empty; persists on first edit, or is pruned on exit.
    }

    fun openTask(id: String) =
        _state.update { it.copy(activeId = id, screen = Screen.Editor, menuOpen = false) }

    fun closeEditor() {
        _state.update { st ->
            val active = st.tasks.firstOrNull { it.id == st.activeId }
            val tasks = if (active != null && active.isEmpty) st.tasks.filterNot { it.id == active.id } else st.tasks
            st.copy(tasks = tasks, screen = Screen.Library, activeId = null, menuOpen = false)
        }
        saveTasksNow()
    }

    // ---- task edits ----
    private fun patch(id: String, transform: (Task) -> Task) {
        _state.update { st ->
            st.copy(tasks = st.tasks.map {
                if (it.id == id) transform(it).copy(updated = System.currentTimeMillis()) else it
            })
        }
        saveTasksDebounced()
    }

    fun setTitle(id: String, v: String) = patch(id) { it.copy(title = v) }
    fun setNotes(id: String, v: String) = patch(id) { it.copy(notes = v) }
    fun setPriority(id: String, p: Int) = patch(id) { it.copy(priority = if (it.priority == p) 0 else p) }
    fun setDue(id: String, due: String) = patch(id) { it.copy(due = due) }
    fun toggleDone(id: String) = patch(id) { it.copy(done = !it.done) }
    fun toggleTag(id: String, tagId: String) = patch(id) { t ->
        t.copy(tags = if (t.tags.contains(tagId)) t.tags - tagId else t.tags + tagId)
    }

    fun duplicateTask(id: String) {
        _state.update { st ->
            val src = st.tasks.firstOrNull { it.id == id } ?: return@update st
            val now = System.currentTimeMillis()
            val copy = src.copy(id = UUID.randomUUID().toString(), done = false, created = now, updated = now)
            val idx = st.tasks.indexOfFirst { it.id == id }
            val list = st.tasks.toMutableList().apply { add(idx + 1, copy) }
            st.copy(tasks = list, menuOpen = false, toast = "Task duplicated")
        }
        saveTasksNow()
        scheduleToastDismiss()
    }

    fun deleteTask(id: String) {
        _state.update { st ->
            st.copy(
                tasks = st.tasks.filterNot { it.id == id },
                screen = Screen.Library, activeId = null, menuOpen = false,
                toast = "Task deleted",
            )
        }
        saveTasksNow()
        scheduleToastDismiss()
    }

    fun clearCompleted() {
        _state.update { st ->
            val ids = completedTasksFor(st.tasks, st.search, st.tagFilter).map { it.id }.toSet()
            if (ids.isEmpty()) return@update st
            val n = ids.size
            st.copy(
                tasks = st.tasks.filterNot { it.id in ids },
                toast = "$n task${if (n == 1) "" else "s"} cleared",
            )
        }
        saveTasksNow()
        scheduleToastDismiss()
    }

    // ---- drag reorder (manual sort only) ----
    fun startDrag(id: String) = _state.update { it.copy(dragId = id) }
    fun endDrag() {
        _state.update { it.copy(dragId = null) }
        saveTasksNow()
    }
    fun reorderOver(overId: String) {
        _state.update { st ->
            val dragId = st.dragId ?: return@update st
            if (dragId == overId) return@update st
            val list = st.tasks.toMutableList()
            val from = list.indexOfFirst { it.id == dragId }
            val to = list.indexOfFirst { it.id == overId }
            if (from < 0 || to < 0) return@update st
            list.add(to, list.removeAt(from))
            st.copy(tasks = list)
        }
    }

    // ---- tags ----
    fun addTag(name: String) {
        val n = name.trim()
        if (n.isEmpty()) return
        _state.update { it.copy(tags = it.tags + Tag(UUID.randomUUID().toString(), n)) }
        saveTags()
    }
    fun renameTag(id: String, name: String) {
        _state.update { it.copy(tags = it.tags.map { t -> if (t.id == id) t.copy(name = name) else t }) }
        saveTags()
    }
    fun deleteTag(id: String) {
        _state.update { st ->
            st.copy(
                tags = st.tags.filterNot { it.id == id },
                tasks = st.tasks.map { it.copy(tags = it.tags - id) },
                tagFilter = if (st.tagFilter == id) null else st.tagFilter,
            )
        }
        saveTags()
        saveTasksNow()
    }

    // ---- settings ----
    private fun updateSettings(transform: (Settings) -> Settings) {
        _state.update { it.copy(settings = transform(it.settings)) }
        saveSettings()
    }
    fun setTheme(mode: ThemeMode) = updateSettings { it.copy(theme = mode) }
    fun setAccent(hex: String) = updateSettings { it.copy(accent = hex) }
    fun setSort(sort: SortMode) = updateSettings { it.copy(sort = sort) }
    fun setShowCompleted(show: Boolean) = updateSettings { it.copy(showCompleted = show) }
    fun setFont(font: AppFont) = updateSettings { it.copy(font = font) }

    // ---- transient UI ----
    fun setSearch(v: String) = _state.update { it.copy(search = v) }
    fun setTagFilter(id: String?) = _state.update { it.copy(tagFilter = id) }
    fun toggleCompletedOpen() = _state.update { it.copy(completedOpen = !it.completedOpen) }
    fun openSettings() = _state.update { it.copy(settingsOpen = true, menuOpen = false) }
    fun closeSettings() = _state.update { it.copy(settingsOpen = false) }
    fun openMenu() = _state.update { it.copy(menuOpen = true) }
    fun closeMenu() = _state.update { it.copy(menuOpen = false) }
    fun openTagSheet() = _state.update { it.copy(tagSheetOpen = true, settingsOpen = false, menuOpen = false) }
    fun closeTagSheet() = _state.update { it.copy(tagSheetOpen = false) }

    private fun scheduleToastDismiss() {
        toastJob?.cancel()
        toastJob = viewModelScope.launch {
            delay(2000)
            _state.update { it.copy(toast = null) }
        }
    }
    fun dismissToast() {
        toastJob?.cancel()
        _state.update { it.copy(toast = null) }
    }
}
