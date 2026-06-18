# CLAUDE.md

Guidance for Claude Code when working in this repository.

## What this is

**Taskalon** — a fast, local-first to-do app for Android. Native app built with
**Kotlin + Jetpack Compose + Material 3**, implemented from a high-fidelity Claude Design
handoff (the handoff README + screenshots live under `design_handoff_taskalon/`, which is
**gitignored** — not committed). The build/CI infrastructure mirrors its sibling project
[markalon](https://github.com/lekky/markalon).

Two screens — **Library** (task list) and **Editor** — plus three overlays: **Settings**
and **Manage Tags** bottom sheets, and a per-editor overflow menu. Everything persists
locally; no account, no network.

## Build & run

Requires the **Android SDK** + JDK 17. The Gradle wrapper is committed.

Two product flavors — **`prod`** (`com.taskalon.app`, "Taskalon") and **`qa`**
(`com.taskalon.app.qa`, "Taskalon QA", versionName `…-qa`) — install side by side with
isolated local data. Variant tasks are `{prod,qa}{Debug,Release}`; `BuildConfig.IS_QA`
distinguishes them at runtime.

```bash
./gradlew assembleDebug          # build BOTH flavors' debug APKs (the compile check)
./gradlew assembleProdRelease    # prod release -> app/build/outputs/apk/prod/release/
./gradlew installProdDebug       # build + install prod on a connected device/emulator
./gradlew installQaDebug         # build + install qa (alongside prod)
./gradlew lint                   # Android lint
```

There are no unit/instrumentation tests of substance yet — **CI is the compile check**
(`.github/workflows/android.yml`). It builds both flavors' debug APKs on PRs to `main` and,
on push to `main`, publishes the `prod` + `qa` release APKs as a GitHub Release.

### Environment caveat (important)
This project is often edited from **Claude Code on the web / a sandbox with no Android SDK
and no Gradle network access** — so you cannot compile locally there. Do a careful static
review, then **rely on the CI build (open/push to a PR) as the real compiler check.** Do not
assume a change compiles just because it looks right.

## Architecture

State-driven navigation (no nav library). A single `TaskalonViewModel` holds all UI state as
`StateFlow<UiState>`; `TaskalonApp` renders the current screen and routes events.

```
com.taskalon.app
├─ MainActivity            // enableEdgeToEdge + setContent { TaskalonApp() }
├─ TaskalonApp             // theme, Library<->Editor switch, Settings/ManageTags sheets, toast
├─ TaskalonViewModel       // UiState, patch()+debounced save (550ms), pruning, sort/filter, CRUD
├─ data/
│  ├─ Task                 // { id,title,notes,done,priority(0..3),due'YYYY-MM-DD',tags[],created,updated } @Serializable
│  ├─ Tag                  // { id, name } @Serializable
│  ├─ Settings             // { theme, accent(hex), sort, showCompleted, font } @Serializable
│  ├─ Accents              // 8-color accent palette (default Green #0E9F6E)
│  └─ TaskalonRepository   // DataStore(Preferences) + kotlinx.serialization JSON; seeds demo data
├─ library/
│  ├─ LibraryScreen        // header, tag-filter chips, search, list, Completed section, FAB
│  └─ TaskCard             // checkbox, title, notes preview, due badge + tag pills, priority dot
├─ editor/EditorScreen     // toolbar (save indicator), title, tags, priority, due, notes, overflow menu
├─ settings/SettingsSheet  // appearance, accent, font, sort, completed, manage-tags row
├─ tags/ManageTagsSheet    // add / inline-rename / delete tags
├─ ui/components/Components // SegmentedControl, TaskCheckbox, TaskalonBottomSheet, Toast, Logo
├─ ui/theme/Tokens         // TaskalonColors (light/dark) + LocalTaskalonColors/LocalAccent/LocalAppFontFamily; priority/due colors; hexColor()
├─ ui/theme/Theme          // TaskalonTheme(settings) — applies tokens + M3 ColorScheme
├─ ui/theme/Type           // AppFont.toFontFamily() + TkText (bespoke type scale)
└─ util/                   // DateUtil (due labels + quick-sets), TaskQueries (filter/sort)
```

### Key design decisions
- **Theming is custom, not Material defaults.** Colors come from `LocalTaskalonColors`
  (`bg/surface/surface2/border/fg1/fg2/fg3`) and `LocalAccent`; the app font from
  `LocalAppFontFamily`. A matching M3 `ColorScheme` is provided so standard components
  inherit it, but **read colors from the composition locals**. All hex/spacing/radii values
  are authoritative from the handoff — don't "round" them.
- **One `TaskalonViewModel` owns all state.** Loads once via `repo.load()`, then writes back.
  Task edits go through `patch(id){…}` which stamps `updated` and triggers a **debounced
  save (550ms)** (Saving…→Saved). Tag and settings changes save immediately.
- **Empty-task pruning.** The FAB inserts an empty task and opens the editor; leaving the
  editor while it's still empty removes it, and persisted empty tasks are filtered on load.
- **Editor text uses local `TextFieldValue`** keyed on `task.id` (title + notes) to avoid the
  controlled-String cursor-jump bug — keep them as `TextFieldValue`, not `String`.
- **Persistence:** DataStore single instance, JSON-encoded, three keys
  (`tasklon.tasks.v1` / `tasklon.tags.v1` / `tasklon.settings.v1`). New persisted fields go
  on the `@Serializable` data classes with defaults (`ignoreUnknownKeys`, so additive is safe).

### Gotchas / things that bit us
- **Composition-local default type inference.** `staticCompositionLocalOf { FontFamily.SansSerif }`
  infers `CompositionLocal<GenericFontFamily>` (the default's concrete type), so providing a
  `FontFamily` fails to compile. Pin the type: `staticCompositionLocalOf<FontFamily> { … }`.
  (This was the one CI compile failure.)
- **`java.time` on minSdk 24** requires **core-library desugaring** (enabled in
  `app/build.gradle.kts` + `desugar_jdk_libs`). Don't remove it while using `LocalDate` etc.
- `enableEdgeToEdge()` makes the manifest's `adjustResize` a no-op — the keyboard is handled
  with `Modifier.imePadding()` on the screen containers. Keep it.
- Material icons: prefer the `Icons.Filled.*` already used (plus `Icons.AutoMirrored.Filled.ArrowBack`).
  Some extended icons may not exist in every namespace — verify via CI if you swap them.

### Known substitutions / follow-ups
- **Fonts** are platform substitutes (Spline/Plex→sans, Newsreader→serif, JetBrains→monospace).
  The type **scale** is exact; only the faces differ. To use the real Google Fonts, wire
  downloadable fonts in `ui/theme/Type.kt` — call sites won't change.
- **Drag-reorder** (manual sort) uses a long-press neighbour-swap gesture rather than a
  full positional drag; wants an on-device feel check / possible polish.

## Git / CI
- Default branch is `main`. CI builds both flavors on PRs and releases the `prod` + `qa`
  release APKs on merge to `main`.
- **Signing:** release builds use a real keystore when `TASKALON_KEYSTORE` (path) +
  `TASKALON_KEYSTORE_PASSWORD` / `TASKALON_KEY_ALIAS` / `TASKALON_KEY_PASSWORD` are present in
  the environment; otherwise they fall back to the **debug key** (installable, not
  Play-Store-ready). To ship a real prod build, generate a keystore and provide those four
  values to the workflow.
