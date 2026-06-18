# CLAUDE.md

Guidance for Claude Code when working in this repository.

## What this is

**Taskalon** — a fast, local-first task app for Android. Native app built with
**Kotlin + Jetpack Compose + Material 3**. The build/CI infrastructure mirrors its sibling
project [markalon](https://github.com/lekky/markalon).

**Status: scaffold.** The project currently compiles to a placeholder landing screen
(`TaskalonApp`). The real screens are implemented from a high-fidelity design handoff
(markdown spec + screenshots) that is **not** in the repo. When the handoff arrives, flesh
out the app under `com.taskalon.app` following the architecture notes below.

## Build & run

Requires the **Android SDK** + JDK 17. The Gradle wrapper is committed.

There are two product flavors — **`prod`** (`com.taskalon.app`, "Taskalon") and **`qa`**
(`com.taskalon.app.qa`, "Taskalon QA", versionName `…-qa`) — so QA and prod install side by
side with isolated local data. Variant tasks are `{prod,qa}{Debug,Release}`; `BuildConfig.IS_QA`
distinguishes them at runtime.

```bash
./gradlew assembleDebug          # build BOTH flavors' debug APKs (the compile check)
./gradlew assembleProdRelease    # prod release -> app/build/outputs/apk/prod/release/
./gradlew assembleQaRelease      # qa release   -> app/build/outputs/apk/qa/release/
./gradlew installProdDebug       # build + install prod on a connected device/emulator
./gradlew installQaDebug         # build + install qa (alongside prod)
./gradlew lint                   # Android lint
./gradlew test                   # unit tests (none of substance yet)
```

There are no unit/instrumentation tests of substance yet — **CI is the compile check**
(`.github/workflows/android.yml`). It builds both flavors' debug APKs on PRs to `main` and,
on push to `main`, publishes the `prod` **and** `qa` release APKs as a GitHub Release
(tag `build-<run#>`).

### Environment caveat (important)
This project is often edited from **Claude Code on the web**, where the container has
**no Android SDK and no network access for Gradle** — so you cannot compile locally there.
Do a careful static review, then **rely on the CI build (open/push to a PR) as the real
compiler check.** Do not assume a change compiles just because it looks right.

## Architecture (current)

State-driven, single-Activity Compose. No nav library — render screens off UI state.

```
com.taskalon.app
├─ MainActivity            // enableEdgeToEdge + setContent { TaskalonApp() }
├─ TaskalonApp             // placeholder landing screen (replace with real nav + screens)
└─ ui/theme/Theme          // TaskalonTheme — minimal M3 light/dark (replace with handoff tokens)
```

When implementing the handoff, follow markalon's proven patterns:
- A single `TaskalonViewModel` holding all UI state as a `StateFlow`; `TaskalonApp` renders
  the current screen and routes events.
- **Persistence:** one DataStore (Preferences) instance, JSON via kotlinx.serialization.
  The VM owns state after the initial load and writes back — don't continuously collect the
  repo flow into the VM (it fights its own writes). New persisted fields go on `@Serializable`
  data classes with defaults (use `ignoreUnknownKeys` so additive changes are safe).
- **Theming:** if the handoff specifies a custom palette, expose it via composition locals
  (e.g. `LocalTaskalonColors` / `LocalAccent`) and provide a matching M3 `ColorScheme` so
  standard components inherit it. Treat handoff hex values as authoritative — don't "round".

## Conventions
- Kotlin DSL Gradle; dependencies declared in `gradle/libs.versions.toml` (version catalog).
- `minSdk 24 / targetSdk 35 / compileSdk 35`, Java/Kotlin target 11.
- `enableEdgeToEdge()` makes the manifest's `adjustResize` a no-op — handle the keyboard with
  `Modifier.imePadding()` on the screen container.

## Git / CI
- Default branch is `main`. CI builds both flavors on PRs and releases the `prod` + `qa`
  release APKs on merge to `main`.
- **Signing:** release builds use a real keystore when `TASKALON_KEYSTORE` (path) +
  `TASKALON_KEYSTORE_PASSWORD` / `TASKALON_KEY_ALIAS` / `TASKALON_KEY_PASSWORD` are present
  in the environment; otherwise they fall back to the **debug key** (installable, not
  Play-Store-ready). To ship a real prod build, generate a keystore and provide those four
  values to the workflow (e.g. decode a base64 repo secret to a file and export the path).
