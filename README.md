# Neuro-Minimalist Android Launcher

A production-ready, ultra-minimalist Android launcher engineered specifically for neurodivergent accessibility. Designed to drastically reduce sensory overstimulation, eliminate visual clutter, dismantle compulsive screen checking, and maximize daily utility through intentional friction and frictionless note-taking.

---

## 🎨 Sensory Constraints & Design System

1. **Pure OLED Black (`#000000`)**:
   - Reduces eye strain and visual noise to zero.
   - Saves battery on OLED displays while creating a calm canvas.
2. **Strict Typography-First Hierarchy**:
   - **Zero App Icons or Colorful Badges**: All applications are represented strictly by clean text typography. Eliminates dopamine triggers engineered by saturated app icons.
   - Primary Text: Muted off-white (`#E0E0E0`).
   - Secondary Text: Muted ash (`#808080`).
   - Zero high-saturation accent colors.
3. **Motion & Haptics**:
   - Zero spring physics or bouncy transitions.
   - Immediate or subtle transitions (<=100ms).
   - Only light deliberate haptic ticks (`EFFECT_TICK` / 10ms micro-pulses) on explicit user actions. No jarring vibration patterns.
4. **Ergonomic Spacing**:
   - Touch targets are generously padded (minimum 48dp–52dp per list item) to prevent mis-clicks and sensory panic.

---

## 🏗️ Architecture & Features

### 1. Launcher Shell (`AndroidManifest.xml`)
- Configured with `android.intent.category.HOME`, `DEFAULT`, and `LAUNCHER`.
- Requests `android.permission.QUERY_ALL_PACKAGES` and `android.permission.VIBRATE`.
- All external intent launches safely append `Intent.FLAG_ACTIVITY_NEW_TASK`.

### 2. Application Engine (`AppRepository.kt`)
- Asynchronously queries Android `PackageManager` on `Dispatchers.IO`.
- Filters out self-package and maps apps to an immutable `StateFlow<List<AppItem>>`.
- Dynamically registers a `BroadcastReceiver` for package install/uninstall/update events to keep the app drawer synchronized.

### 3. Core Home Screen UI (`HomeScreen.kt`)
- **Digital Clock Header**: Low-contrast clock (`HH:mm`) and date (`EEEE, MMMM d`). Tapping the time opens the system clock; tapping the date opens the system calendar.
- **Pinned Essentials**: Top section displaying up to 4 user-customizable text apps. Long-pressing any app allows pinning/unpinning.
- **Search & Drawer**: Integrated plain-text search bar with instant, real-time case-insensitive filtering over all installed applications.

### 4. Intentional Friction Mechanism ("Dopamine Intercept") (`FrictionInterceptorScreen.kt`)
- Allows tagging any app as "Distracting / High Stimulation" (persisted locally via Jetpack DataStore Preferences).
- When attempting to open a tagged app, presents a calming, dark interstitial screen with a prompt:
  > *"Take a breath. Do you need to open this right now?"*
- Features an unskippable 5-second countdown timer.
- Provides an immediate "Cancel" button and a disabled "Continue" button that activates only after the 5 seconds elapse.

### 5. Integrated Frictionless Utility
- **Left-Swipe Scratchpad (`ScratchpadScreen.kt`)**: A horizontal swipe to the left page reveals a persistent plain-text scratchpad and task list that auto-saves to Jetpack DataStore.
- **Inline Math Calculation (`MathEvaluator.kt`)**: Typing math expressions directly into the search bar (e.g. `45 * 12`, `(10 + 25) / 5`, `2 ^ 8`) evaluates and renders the computed answer inline without opening a separate calculator app. Tapping the result copies it to the clipboard.

---

## 🛠️ Build & Verification

```bash
# Run unit tests
./gradlew test

# Assemble debug APK
./gradlew assembleDebug
```

The compiled APK is available at:
`app/build/outputs/apk/debug/app-debug.apk`
