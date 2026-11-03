# TASK — Nexus AI (complete app build)

## GOAL
Build the complete Nexus AI Android app: premium Deep Core glassmorphism UI,
Room-persisted chat history, and a Puter.js WebView bridge for real AI chat
with popup sign-in. Push the finished source to GitHub.

## STEPS
- [x] Inspect existing scaffold (Compose, AGP 8.2.2, Kotlin 1.9.22, Room/KSP wired)
- [x] Room layer — `data/ChatDatabase.kt` (chat_sessions + messages, DAO, singleton)
- [x] `AiViewModel.kt` — AndroidViewModel + PuterEvents, session/title logic, streaming
- [x] Puter bridge — `puter/PuterWebView.kt` + `assets/puter_bridge.html` (popup auth, streaming)
- [x] UI components — Foundation, TopBar, NexusDrawer, EmptyState, Messages, ChatInput, Sheets
- [x] `ui/ChatScreen.kt` — ModalNavigationDrawer shell + WebView command pump + wiring
- [x] `MainActivity.kt`, theme expansion, manifest, gradle deps, CI workflow
- [x] Static compile-safety review (missing NexusTopBar found + added, JS try/catch balanced,
      invalid `weight` import removed, bridge class made public for reflection)
- [ ] Commit + push to https://github.com/alsbtv/Nexus_V3

## DECISIONS
- Compose (not WebView UI) — the scaffold is Compose and the brief names Compose files.
- Puter.js runs in a 1dp WebView served from `https://appassets.androidplatform.net`
  (real HTTPS origin, so localStorage/cookies for auth behave correctly).
- Puter auth popups handled via `WebChromeClient.onCreateWindow` + a full-screen Dialog.
- Streaming tokens flow JS -> `@JavascriptInterface` -> main-thread handlers -> StateFlow.
- Selected model persisted in SharedPreferences; chats/messages in Room.
- Curated model catalogue, merged with the live Puter model list when available.
- CI publishes a debug APK release on every push to `main`.

## NOW
Source complete and statically reviewed. Push blocked by the environment:
`/root/Nexus_V3` has no `.git` directory and every shell command fails with
`libdl.so: cannot open shared object file`. Needs a working shell + git auth.
