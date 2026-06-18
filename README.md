# Taskalon

A fast, **local-first** task app for Android. Native implementation built with **Kotlin +
Jetpack Compose + Material 3** — no account, no cloud.

> **Status:** project scaffold. The build, CI, and release infrastructure are in place
> (mirroring [markalon](https://github.com/lekky/markalon)) and the app compiles to a
> placeholder landing screen. The real task screens are implemented from a design handoff.

## Tech

| Concern | Choice |
| --- | --- |
| UI | Jetpack Compose, Material 3 |
| Persistence | Jetpack DataStore (Preferences) + kotlinx.serialization (JSON) |
| Min / target SDK | 24 / 35 |
| Build | Gradle (Kotlin DSL), version catalog, wrapper committed |

## Building

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
```

Open the project in Android Studio (Ladybug or newer) and run the `app` configuration.

## CI / releases

`.github/workflows/android.yml` builds both flavors' debug APKs on every PR/push to `main`
and, on push to `main`, publishes the `prod` **and** `qa` release APKs as a GitHub Release
(tag `build-<run#>`).

Release builds are signed with a real keystore when `TASKALON_KEYSTORE` (path) +
`TASKALON_KEYSTORE_PASSWORD` / `TASKALON_KEY_ALIAS` / `TASKALON_KEY_PASSWORD` are present in
the environment; otherwise they fall back to the debug key so the APK is still installable.
