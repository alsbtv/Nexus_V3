# AGENTS.md — Nexus AI

## What this is
Nexus AI — a new Android app built with Jetpack Compose.
Package: `com.hiyako.app`. Current screen: "Nexus AI is Ready".

## Stack
- AGP 8.2.2 (requires Gradle 8.2+), Kotlin 1.9.22
- Compose Compiler 1.5.8, Compose BOM 2024.02.00
- compileSdk 34, targetSdk 34, minSdk 24, Java/JVM 17
- Room 2.6.1 with KSP 1.9.22-1.0.17
- Media3 ExoPlayer 1.2.1, Coil 2.5.0, Navigation Compose 2.7.7, OkHttp 4.12.0, WebKit 1.9.0

## Layout
```
app/src/main/java/com/hiyako/app/MainActivity.kt   app entry + Compose screen
app/src/main/java/com/hiyako/app/ui/theme/Color.kt Deep Core palette
app/src/main/java/com/hiyako/app/ui/theme/Theme.kt dark-only Material3 theme
app/src/main/res/drawable/ic_launcher.xml         vector launcher icon
```

## Design
Deep Core dark theme only: background `#05060F`, primary neon cyan `#00E5FF`,
secondary neon magenta `#FF3DCB`. No light scheme by design.

## Commands
No Gradle wrapper exists yet (no build was run at scaffold time).
Once a wrapper is added or the project is opened in Android Studio:
- `./gradlew assembleDebug`
- `./gradlew lint`

## Decisions
- Dark-only theme; `NexusAITheme` ignores system light mode.
- Plain vector drawable launcher icon per initial spec (not adaptive).
- Room compiler included through KSP so entities/DAOs can be added immediately.

## State
Scaffold complete; no Gradle build has been run.
