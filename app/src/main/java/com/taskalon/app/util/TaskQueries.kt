package com.taskalon.app.util

import com.taskalon.app.data.SortMode
import com.taskalon.app.data.Task

/** Does this task match the active tag filter + search text (title/notes, case-insensitive)? */
fun Task.matches(search: String, tagFilter: String?): Boolean {
    if (tagFilter != null && !tags.contains(tagFilter)) return false
    if (search.isNotBlank()) {
        val q = search.trim().lowercase()
        if (!title.lowercase().contains(q) && !notes.lowercase().contains(q)) return false
    }
    return true
}

/**
 * Open (not-done) tasks matching the filter, in the requested sort order.
 * `sortedWith` is stable, so ties keep manual (array) order.
 */
fun openTasksFor(tasks: List<Task>, search: String, tagFilter: String?, sort: SortMode): List<Task> {
    val open = tasks.filter { !it.done && it.matches(search, tagFilter) }
    return when (sort) {
        SortMode.MANUAL -> open
        SortMode.DUE -> open.sortedWith(compareBy<Task> { it.due.isBlank() }.thenBy { it.due })
        SortMode.PRIORITY -> open.sortedWith(compareByDescending<Task> { it.priority })
    }
}

/** Completed tasks matching the filter (display order = array order). */
fun completedTasksFor(tasks: List<Task>, search: String, tagFilter: String?): List<Task> =
    tasks.filter { it.done && it.matches(search, tagFilter) }
