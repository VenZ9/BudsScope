# BudsScope

> **Premium Universal True Wireless Stereo (TWS) Earbud Companion for Android**

![Platform](https://img.shields.io/badge/Platform-Android_8.0+_(API_26--35)-black?style=flat-square&logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple?style=flat-square&logo=kotlin)
![Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-blue?style=flat-square&logo=jetpackcompose)
![License](https://img.shields.io/badge/License-Apache_2.0-green?style=flat-square)
![Privacy](https://img.shields.io/badge/Privacy-100%25_Offline_•_No_Trackers-cyan?style=flat-square)

---

## 1. Overview & Mission

Most earbud manufacturers bundle proprietary, ad-heavy, or cloud-tethered companion applications that only support their own brand. **BudsScope** is an open, universal companion application engineered for Android. It interfaces directly with standard Android Bluetooth APIs and media sessions to provide a unified, responsive dashboard for your audio devices—without accounts, cloud telemetry, analytics SDKs, or internet requirements.

BudsScope strictly adheres to a **truth-in-hardware policy**: it never simulates readings, fabricates battery percentages, or creates artificial connected states when hardware does not expose them.

---

## 2. Key Features

### 🎧 Live Dashboard
- **Vector Earbud Visualizer**: Custom-rendered Compose Canvas illustration of dual TWS earbuds with reactive status aura and charging LED indicators.
- **Granular Battery Gauges**:
  - Independent **Left** and **Right** earbud percentages when exposed by device hardware.
  - **Charging Case** battery level when available through supported profiles.
  - Automatic fallback to **Combined Channel** gauge for single-value Bluetooth audio profiles.
  - Explicit discrete states: `Available`, `Charging (⚡)`, `Temporarily Unavailable`, `Stale`, and `Unsupported`.
- **Connection Details**: Real-time display of Bluetooth MAC address, active profiles (A2DP Audio, HFP/Headset, GATT Battery), and last sync timestamp.
- **Quick Controls**: Instant shortcut to system Bluetooth settings and manual refresh action.

### 🎵 Now Playing: Music as a Living Interface
- **Android Media Session Integration**: Discovers active playback sessions from Spotify, YouTube Music, Apple Music, Audible, and other players via Android's `MediaSessionManager`.
- **Metadata**: Real-time track title, artist, album, album art, elapsed position, and track duration.
- **Fluid Liquid Sound Wave**: Canvas-rendered flowing multi-wave ribbon animation that dynamically reacts to `Playing` and `Paused` states, tapering gracefully when music stops.
- **Playback Controls**: Direct previous, play/pause, and next actions through standard `MediaController.TransportControls`.
- **Privacy & Security**: Operates 100% locally via Android's `NotificationListenerService` without logging or transmitting playback data.

### 📊 Battery Intelligence & Drain Analysis
- **Local Persistence via Room**: Timestamped snapshots captured when meaningful percentage changes occur or after sampling intervals elapse.
- **Canvas History Timeline Chart**: Visualizes battery discharge curves over time with gradient shading and gridlines.
- **Observed Drain Rate**: Calculates discharge speed in `%/hour` across non-charging samples.
- **Estimated Listening Time**: Computes remaining listening hours based on observed drain trends, with explicit calculation basis and uncertainty labels.
- **Hardware Health Honesty**: Standard Bluetooth profiles do not report physical lithium cell degradation; BudsScope clearly explains that observed drain trends reflect discharge behavior rather than inventing fake battery health percentages.

### 📡 Reconnect Radar & Connection Intelligence
- **Paired Audio Device Directory**: Inspect bonded headsets and see last-seen connection times (*"Just now"*, *"2 hr ago"*, *"Yesterday"*).
- **Nearby Device Discovery**: BLE and classic discovery to find advertising earbuds.
- **Smart Disconnection Alerts**: Optional system notifications if earbuds unexpectedly disconnect.

### 📱 Home Screen Widgets
- **Compact Battery Widget**: Glanceable home-screen widget displaying connected device name, status, and primary battery level.
- **Expanded Dual Battery Widget**: Multi-column widget displaying independent Left, Right, and Case battery readings with status badges.
- Built with standard `RemoteViews` for minimal battery and memory impact.

### ⚙️ Diagnostics & Settings
- **Configurable Low-Battery Alert**: Threshold slider (5% to 40%) with anti-spam alert deduplication.
- **Fluid Wave Toggle**: Low-power toggle to pause wave animations on battery saver.
- **System & Permission Diagnostics**: Real-time audit of `BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`, `POST_NOTIFICATIONS`, and `NotificationListenerService` access.
- **Data Erasure**: One-tap action to wipe all local Room database history and DataStore preferences.

---

## 3. Product Identity & Design System

BudsScope features a restrained, near-black aesthetic designed for readability:

| Element | Color Value | Description |
| :--- | :--- | :--- |
| **Background** | `#080A0E` | Near-black backdrop to maximize OLED efficiency |
| **Primary Surface** | `#12151B` | Deep graphite surface for navigation and cards |
| **Card Surface** | `#181C24` | Elevated graphite surface |
| **Borders** | `#262E3D` | 1dp subtle structural borders |
| **Primary Accent** | `#00E5FF` | Electric cyan for active states and gauges |
| **Secondary Accent** | `#3B82F6` | Muted electric blue for waveform ribbons |
| **Status Indicators** | Green (`#10B981`), Amber (`#F59E0B`), Red (`#EF4444`) | Battery thresholds and charging state |

---

## 4. Universal Bluetooth Compatibility Architecture

```
                                  Android Bluetooth Subsystem
                                               │
            ┌──────────────────────────────────┼──────────────────────────────────┐
            ▼                                  ▼                                  ▼
 BluetoothAdapter (Bonded)        BluetoothProfile.A2DP / HEADSET    android.bluetooth.device.action.
            │                                  │                     BATTERY_LEVEL_CHANGED
            ▼                                  ▼                                  │
 BluetoothDevice.getBatteryLevel()   GATT Battery Service (0x180F)                 │
            │                                  │                                  │
            └──────────────────────────────────┼──────────────────────────────────┘
                                               │
                                               ▼
                                   [ BatteryValueValidator ]
                                 (Discard -1, 127, 255 sentinels;
                                  validate 0..100; check staleness)
                                               │
                                               ▼
                                 [ DeviceCapabilityResolver ]
                                 (Dual channel vs Combined)
                                               │
                                               ▼
                                   [ BatteryRepository ]
                                 (Room DB + Drain Analysis)
                                               │
                                               ▼
                              [ Jetpack Compose UI & Widgets ]
```

### Supported Hardware Battery Mechanisms:
1. **Standard Android Battery Intent**: `android.bluetooth.device.action.BATTERY_LEVEL_CHANGED` with `android.bluetooth.device.extra.BATTERY_LEVEL` (Android 8.1+ / API 27+).
2. **Hidden Reflection Interface**: Safely queries `BluetoothDevice.getBatteryLevel()` when exposed by the OEM Android framework.
3. **Standard Bluetooth SIG GATT Battery Service**: UUID `0000180f-0000-1000-8000-00805f9b34fb` with characteristic `00002a19-0000-1000-8000-00805f9b34fb`.
4. **Graceful Degradation**: When hardware exposes no battery profile, the application cleanly displays "Unsupported by Hardware" without crashing or faking data.

---

## 5. Security, Permissions, and Privacy

BudsScope requires zero network permissions (`android.permission.INTERNET` is **not** requested).

| Permission | API Level | Purpose |
| :--- | :--- | :--- |
| `BLUETOOTH_CONNECT` | 31+ | Query connected earbud names, active profiles, and battery levels |
| `BLUETOOTH_SCAN` | 31+ | Scan for nearby advertising TWS earbuds (`neverForLocation` flag declared) |
| `BLUETOOTH` & `BLUETOOTH_ADMIN` | 26–30 | Legacy Bluetooth access on Android 8 through 11 |
| `ACCESS_FINE_LOCATION` | 26–30 | Required by legacy Android for Bluetooth scanning |
| `POST_NOTIFICATIONS` | 33+ | Deliver low-battery alerts and unexpected disconnection notifications |
| `BIND_NOTIFICATION_LISTENER_SERVICE` | 26+ | Read track metadata from active media sessions locally |

---

## 6. Architecture & Tech Stack

- **UI**: Jetpack Compose, Material 3, Compose Navigation.
- **Language**: Kotlin 2.0.21.
- **Concurrency**: Coroutines & Flow (`StateFlow`, `SharedFlow`).
- **Database**: Room 2.6.1 (`BatterySnapshotEntity`, `ConnectionEventEntity`).
- **Settings**: Jetpack DataStore Preferences 1.1.1.
- **Build System**: Android Gradle Plugin 8.7.3, Gradle 8.9, JDK 17 / 21.

---

## 7. Testing & Verification

Comprehensive automated test suite located in `app/src/test/java/dev/budsscope/app/`:

- `BatteryValueValidatorTest`: Boundary validation [0..100], sentinel filtering (-1, 127, 255), null handling, staleness calculation, charging states.
- `DeviceCapabilityResolverTest`: Detection of dual-channel, case battery, combined channel, and unsupported devices.
- `BatteryHistoryCalculatorTest`: Non-charging sample filtering, 5-minute minimum observation threshold, drain rate (`%/hr`), remaining listening time estimation, alert deduplication.
- `MediaMetadataParserTest`: Safe parsing of metadata, null handling, progress calculation, time formatters (`mm:ss`).
- `ConnectionStateTrackerTest`: Debouncing rapid Bluetooth connection/disconnection event flutters, human-readable last-seen formatting.

---

## 8. Continuous Integration & APK Pipeline

The project includes an automated GitHub Actions workflow (`.github/workflows/android-apk.yml`):
- Triggers on `push`, `pull_request`, and `workflow_dispatch`.
- Sets up JDK 17 and Android SDK Platform 35 / Build Tools 35.0.0.
- Executes `./gradlew testDebugUnitTest --stacktrace`.
- Assembles `./gradlew assembleDebug --stacktrace`.
- Uploads the debug APK as an artifact (`budsscope-debug-apk`).

---

## 9. Building Locally

```bash
# Clone the repository
git clone https://github.com/VenZ9/BudsScope.git
cd BudsScope

# Grant execute permission
chmod +x gradlew

# Run unit tests
./gradlew testDebugUnitTest

# Assemble debug APK
./gradlew assembleDebug

# Output APK path
# app/build/outputs/apk/debug/app-debug.apk
```
