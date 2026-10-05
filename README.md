# Arrow Flow
Original offline-first Android puzzle game inspired by the arrow-escape genre.

## v0.1
- Kotlin Android project, portrait and one-hand-friendly
- Deterministic 4x4 to 7x7 procedural levels
- Direction-aware blocking and escape order
- Undo, reset, hint, stars, coins and local progress
- Responsive custom Canvas board and haptic feedback
- Core gameplay works without internet

## Architecture
UI -> GameViewModel -> PuzzleEngine -> PuzzleGenerator/Validator -> SaveManager.

The engine is independent from rendering so Compose UI, richer animations, daily challenge, Firebase, ads and leaderboards can be added later.

## Build
Open in Android Studio and run the app configuration, or run: ./gradlew assembleDebug
GitHub Actions builds the debug APK on pushes and pull requests.
