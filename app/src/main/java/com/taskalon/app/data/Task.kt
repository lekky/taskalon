package com.taskalon.app.data

import kotlinx.serialization.Serializable

/**
 * A single to-do item. Persisted as JSON (see [TaskalonRepository]).
 *
 * `priority` is 0..3 (None / Low / Medium / High); `due` is a `YYYY-MM-DD` string or "".
 * `tags` holds [Tag.id] references.
 */
@Serializable
data class Task(
    val id: String,
    val title: String = "",
    val notes: String = "",
    val done: Boolean = false,
    val priority: Int = 0,
    val due: String = "",
    val tags: List<String> = emptyList(),
    val created: Long = 0L,
    val updated: Long = 0L,
) {
    /**
     * A task is "empty" if it has no trimmed title, no trimmed notes, no tags, no due date,
     * and is not done. Empty tasks are pruned when the editor is left and on load.
     */
    val isEmpty: Boolean
        get() = title.isBlank() && notes.isBlank() && tags.isEmpty() && due.isBlank() && !done
}
