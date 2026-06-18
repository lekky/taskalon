# Taskalon

A fast, **local-first** to-do app for Android. Capture tasks, organize them with custom
tags, set priority and due dates, reorder them, and review/clear completed work — no
account, no cloud. Native implementation of the Taskalon design handoff, built with
**Kotlin + Jetpack Compose + Material 3**. Sibling product to
[markalon](https://github.com/lekky/markalon), sharing its calm, card-based visual language.

## Features

- **Task list** — browse, search (title + notes), and filter by tag; collapsible
  **Completed** section with one-tap Clear; manual **drag-to-reorder**; FAB to create.
- **Task editor** — title, completion, tag toggles, 4-level priority, due date (native
  picker + Today/Tomorrow/This weekend/Next week quick-sets), and notes. Auto-saves
  (debounced) with a Saving…/Saved indicator. Backing out of an untouched new task discards it.
- **Sorting** — Manual (drag), Due date, or Priority.
- **Settings** — light/dark/system theme, 8 accent colors, 4 fonts, sort mode,
  show/hide completed, and a shortcut to Manage Tags. All persisted.
- **Tags** — create, rename, and delete tags in a dedicated sheet.

## Tech

| Concern | Choice |
| --- | --- |
| UI | Jetpack Compose, Material 3 |
| State | `TaskalonViewModel` (`StateFlow<UiState>`), state-driven navigation |
| Persistence | Jetpack DataStore (Preferences) + kotlinx.serialization (JSON) |
| Dates | `java.time` (core-library desugaring for minSdk 24) |
| Min / target SDK | 24 / 35 |

## Architecture

```
com.taskalon.app
├─ MainActivity            // hosts TaskalonApp
├─ TaskalonApp             // theme + state-driven nav (Library ⇄ Editor) + sheets + toast
├─ TaskalonViewModel       // UiState, debounced save (550ms), pruning, sort/filter, CRUD
├─ data/                   // Task, Tag, Settings, Accents, TaskalonRepository (DataStore)
├─ library/                // LibraryScreen + TaskCard
├─ editor/                 // EditorScreen (date picker, quick-sets, overflow menu)
├─ settings/SettingsSheet  // appearance, accent, font, sort, completed, tags
├─ tags/ManageTagsSheet    // add / rename / delete tags
├─ ui/components/          // segmented control, checkbox, bottom sheet, toast, logo
└─ ui/theme/               // Tokens (custom palette), Theme, Type (scale)
```

## Building

Requires the **Android SDK** + JDK 17. The Gradle wrapper is committed. Two product flavors —
**`prod`** (`com.taskalon.app`) and **`qa`** (`com.taskalon.app.qa`, "Taskalon QA") — install
side by side with isolated local data.

```bash
./gradlew assembleDebug          # build BOTH flavors' debug APKs (the compile check)
./gradlew installProdDebug       # build + install prod on a connected device/emulator
./gradlew installQaDebug         # build + install qa (alongside prod)
```

## CI / releases

`.github/workflows/android.yml` builds both flavors' debug APKs on every PR/push to `main`
and, on push to `main`, publishes the `prod` + `qa` release APKs as a GitHub Release
(tag `build-<run#>`). Release builds are signed with a real keystore when the `TASKALON_*`
secrets are configured, else debug-signed so the APK is still installable.

## Notes / fonts

The design specifies brand fonts (Spline Sans, IBM Plex Sans, Newsreader, JetBrains Mono).
This implementation currently uses the platform's generic families (sans / serif / monospace)
as substitutes; to match the brand exactly, wire Google downloadable fonts (or drop font
files into `app/src/main/res/font/`) and point `ui/theme/Type.kt` at them — call sites won't
change.
