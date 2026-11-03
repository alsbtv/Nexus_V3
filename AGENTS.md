# AGENTS.md — Nexus AI

## What this is
Nexus AI — a premium Android AI chat app built with Jetpack Compose.
Package: `com.hiyako.app`. Deep Core dark theme (near-black, neon cyan,
neon magenta, violet). Real chat runs through Puter.js in an invisible
WebView; conversations persist locally with Room.

Repo: https://github.com/alsbtv/Nexus_V3

## Stack
- AGP 8.2.2, Gradle 8.2 wrapper (custom minimal `gradlew`), Kotlin 1.9.22
- Compose Compiler 1.5.8, Compose BOM 2024.02.00, Material 3
- compileSdk/targetSdk 34, minSdk 24, Java/JVM 17
- Room 2.6.1 + KSP 1.9.22-1.0.17, Coil 2.5.0, Coroutines 1.7.3
- androidx.webkit 1.9.0 (WebViewAssetLoader), Activity Compose 1.8.2

## Architecture
```
MainActivity.kt                     edge-to-edge entry → ChatScreen
AiViewModel.kt                      single source of truth: sessions, messages,
                                    streaming, auth, model choice; implements PuterEvents
data/ChatDatabase.kt                Room: chat_sessions + messages (FK cascade), DAO, singleton
puter/PuterWebView.kt               invisible 1dp WebView, AiBridge @JavascriptInterface,
                                    popup sign-in via WebChromeClient.onCreateWindow + Dialog
assets/puter_bridge.html            Puter.js SDK + checkAuth/startSignIn/signOut/
                                    fetchModels/askPuter (streaming)/stopGeneration
ui/ChatScreen.kt                    ModalNavigationDrawer shell, command pump, sheets
ui/components/Foundation.kt         glass modifier, aurora+starfield background, avatar,
                                    buttons, stat tiles, NEXUS_VERSION
ui/components/TopBar.kt             menu orb + live chat title + pulsing status chip
ui/components/NexusDrawer.kt        history, workspace, update, clear, auth card
ui/components/EmptyState.kt         5 quick-action cards (Build App/Image/Video/Research/Ghost Fix)
ui/components/Messages.kt           bubbles, streaming caret, thinking dots, Coil images, errors
ui/components/ChatInput.kt          pill input, model picker trigger, send/stop orb
ui/components/Sheets.kt             model sheet, workspace, update, clear dialog, error banner
ui/theme/{Color,Theme}.kt           palette + typography + shapes, dark-only
```

## Data flow
1. `AiViewModel` emits JS through `commands` (Channel) → ChatScreen collects it
   → `WebView.evaluateJavascript`.
2. JS calls `NexusBridge.onXxx` → `AiBridge` posts to main thread → `PuterEvents`.
3. Streaming tokens append to `streamingText`; completion/stop saves the assistant
   message to Room. First user prompt auto-names the session.
4. Selected model persisted in SharedPreferences (`nexus_prefs` / `selected_model`).

## Commands
CI (recommended): `.github/workflows/build.yml` builds on every push to `main`
and publishes `app-debug.apk` as a GitHub release asset (`contents: write`).
Local:
- `./gradlew assembleDebug`
- `./gradlew lint`

## Decisions (11/2026)
- Compose UI (not WebView UI) — native look, Compose design system.
- Puter.js served from `https://appassets.androidplatform.net/assets/…` so cookies
  and localStorage behave like a real HTTPS origin.
- Auth popups captured with `onCreateWindow` + WebViewTransport inside a full-screen
  Compose Dialog; cookies shared app-wide via CookieManager.
- Model UI: curated catalogue merged with live `puter.ai.listModels()` results.
- Curated catalogue keeps the app beautiful offline; failures surface as a friendly
  banner, never a crash.

## State
- Feature-complete source: UI, Room layer, Puter bridge, CI build/release.
- No local Gradle build has been run (per task instruction). CI is the build of record.
- Static compile-safety review completed; `NexusTopBar` was added during review.
- Push was not performed from this container: workspace has no `.git` directory and
  the shell is unavailable (`libdl.so` load error). Source is complete on disk.
