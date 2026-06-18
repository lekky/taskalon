package com.taskalon.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "taskalon")

/** Everything the app loads at startup. */
data class AppData(
    val tasks: List<Task>,
    val tags: List<Tag>,
    val settings: Settings,
)

/**
 * Local persistence on Jetpack DataStore (Preferences), three JSON-encoded keys — the
 * platform-idiomatic replacement for the prototype's three `localStorage` keys. The
 * ViewModel owns state after [load]; it writes back via the `save*` methods.
 */
class TaskalonRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val tasksKey = stringPreferencesKey("tasklon.tasks.v1")
    private val tagsKey = stringPreferencesKey("tasklon.tags.v1")
    private val settingsKey = stringPreferencesKey("tasklon.settings.v1")

    suspend fun load(): AppData {
        val prefs = context.dataStore.data.first()
        val settings = prefs[settingsKey]
            ?.let { runCatching { json.decodeFromString<Settings>(it) }.getOrNull() }
            ?: Settings()

        // First run (nothing stored) → seed demo data.
        if (prefs[tasksKey] == null && prefs[tagsKey] == null) {
            val seed = seedData()
            saveTags(seed.tags)
            saveTasks(seed.tasks)
            return AppData(seed.tasks, seed.tags, settings)
        }

        val tags = prefs[tagsKey]
            ?.let { runCatching { json.decodeFromString<List<Tag>>(it) }.getOrNull() }
            ?: emptyList()
        // Self-healing: drop any persisted empty tasks on load.
        val tasks = (prefs[tasksKey]
            ?.let { runCatching { json.decodeFromString<List<Task>>(it) }.getOrNull() }
            ?: emptyList())
            .filterNot { it.isEmpty }
        return AppData(tasks, tags, settings)
    }

    suspend fun saveTasks(tasks: List<Task>) {
        context.dataStore.edit { it[tasksKey] = json.encodeToString(tasks) }
    }

    suspend fun saveTags(tags: List<Tag>) {
        context.dataStore.edit { it[tagsKey] = json.encodeToString(tags) }
    }

    suspend fun saveSettings(settings: Settings) {
        context.dataStore.edit { it[settingsKey] = json.encodeToString(settings) }
    }

    private fun seedData(): AppData {
        val now = System.currentTimeMillis()
        val today = LocalDate.now()
        val nextSat = today.with(TemporalAdjusters.next(DayOfWeek.SATURDAY))

        val work = Tag("work", "Work")
        val home = Tag("home", "Home")
        val errands = Tag("errands", "Errands")
        val health = Tag("health", "Health")
        val tags = listOf(work, home, errands, health)

        var t = now
        fun id() = UUID.randomUUID().toString()
        fun stamp() = (--t)  // strictly decreasing so the seeded array order is stable

        val tasks = listOf(
            Task(
                id = id(), title = "Finish the Q3 planning deck",
                notes = "Pull the latest numbers from the finance sheet",
                due = today.toString(), tags = listOf(work.id), priority = 3,
                created = stamp(), updated = stamp(),
            ),
            Task(
                id = id(), title = "Reply to the design review thread",
                due = today.plusDays(1).toString(), tags = listOf(work.id), priority = 2,
                created = stamp(), updated = stamp(),
            ),
            Task(
                id = id(), title = "Book dentist appointment",
                notes = "Cleaning is overdue — try for a morning",
                due = today.minusDays(1).toString(), tags = listOf(health.id, errands.id),
                priority = 1, created = stamp(), updated = stamp(),
            ),
            Task(
                id = id(), title = "Water the plants",
                tags = listOf(home.id), created = stamp(), updated = stamp(),
            ),
            Task(
                id = id(), title = "Pick up dry cleaning",
                notes = "Ticket is in the kitchen drawer.",
                due = nextSat.toString(), tags = listOf(errands.id),
                created = stamp(), updated = stamp(),
            ),
            Task(
                id = id(), title = "Send the invoice to Acme",
                done = true, tags = listOf(work.id), created = stamp(), updated = stamp(),
            ),
            Task(
                id = id(), title = "Take out the recycling",
                done = true, tags = listOf(home.id), created = stamp(), updated = stamp(),
            ),
        )
        return AppData(tasks, tags, Settings())
    }
}
