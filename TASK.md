# TASK — Nexus AI Android scaffold

GOAL: Generate the complete "Nexus AI" Android project (package `com.hiyako.app`)
with the Deep Core Compose theme. No Gradle build is run at this stage (user request).

## Steps
- [x] Create the project directory structure under `app/src/main`.
- [x] Write `settings.gradle.kts`, root `build.gradle.kts`, `gradle.properties`.
- [x] Write `app/build.gradle.kts` with all requested dependencies.
- [x] Write `AndroidManifest.xml` (INTERNET permission, Nexus AI label, dark NoActionBar theme).
- [x] Write `ui/theme/Color.kt` and `ui/theme/Theme.kt` (Deep Core palette).
- [x] Write `MainActivity.kt` with the full-screen "Nexus AI is Ready" screen.
- [x] Write `res/drawable/ic_launcher.xml` and `res/values/strings.xml`.
- [x] Verify the resulting file tree.

## Decisions
- AGP 8.2.2, Kotlin 1.9.22, Compose Compiler 1.5.8, Compose BOM 2024.02.00.
- compileSdk/targetSdk 34, minSdk 24, Java/JVM 17.
- Room compiler wired through KSP 1.9.22-1.0.17 so Room is usable immediately.
- Dark-only "Deep Core" theme; no light color scheme by design.
- Vector drawable launcher icon (not adaptive), per the initial spec.
- No Gradle wrapper generated because no Gradle command was allowed yet.

## NOW
Scaffold complete. Next: add the Gradle wrapper (or open the project in Android
Studio) and run `./gradlew assembleDebug`.
