# Practical Training Project

An Android application for monitoring possible falls, visualising safety data, and helping users contact emergency contacts when assistance may be needed.

The app is built as a Jetpack Compose application. It reads accelerometer and gyroscope data, presents live monitoring information in a dashboard, records detected events, and can include the user's location in emergency information.

## Features

- Email/password login and registration flows.
- Dashboard with:
  - Safety score and activity summary.
  - Fall count and recent activity.
  - Monitoring, sensor accuracy, and battery status.
  - Hourly and weekly impact charts.
- Fall detection using the device accelerometer and gyroscope.
- Ten-second cancellation countdown before a detected event is confirmed.
- Fall history with:
  - Confirmed and cancelled events.
  - Impact and rotation measurements.
  - Timestamp and location details when available.
  - Filtering, event details, clearing, and export.
- CSV and text-based report sharing through Android's share sheet.
- Foreground monitoring service for background or locked-screen monitoring.
- Emergency contacts with SMS alerts and WhatsApp/browser fallback.
- Optional location links that open in Google Maps.
- Settings for background monitoring, location, sensor accuracy, notifications, sound, automatic emergency actions, theme, and data sharing.
- Profile and emergency-contact management screens.

## Technology stack

- Kotlin
- Android Gradle Plugin 8.7.3
- Gradle 8.9
- Jetpack Compose and Material 3
- Android SDK 35
- Minimum Android SDK 29
- Kotlin JVM target 11
- AndroidX Navigation Compose
- AndroidX DataStore Preferences
- Kotlin Coroutines
- Vico charts
- Google Maps Compose

## Project structure

```text
app/src/main/java/com/example/practicaltrainingproject/
├── data/
│   ├── auth/          Authentication models and repository
│   ├── local/         Sample impact data
│   ├── manager/       Settings, notifications, and emergency helpers
│   ├── model/         Local impact data stores
│   └── service/       Fall detection and location services
├── presentation/
│   ├── theme/         Compose theme, colors, and typography
│   └── ui/
│       ├── dashboardScreen/
│       ├── fallDetectionScreen/
│       ├── fallHistoryScreen/
│       ├── loginScreen/
│       ├── profileScreen/
│       ├── settingsScreen/
│       └── ...
└── MainActivity.kt
```

## Requirements

- Android Studio with Android SDK 35 installed.
- JDK 11.
- A physical Android device or emulator running Android 10 (API 29) or newer.
- A device with an accelerometer and gyroscope for meaningful fall-detection testing.
- Google Maps configuration if map functionality is used.

## Getting started

1. Clone the repository and open it in Android Studio.
2. Allow Gradle to download the project dependencies.
3. Confirm that the Android SDK and JDK 11 are configured in Android Studio.
4. Run the `app` configuration on a connected device or emulator.

To build from a Windows terminal:

```powershell
.\gradlew.bat assembleDebug
```

The generated debug APK is written to:

```text
app/build/outputs/apk/debug/app-debug.apk
```

To install the debug build on a connected device:

```powershell
.\gradlew.bat installDebug
```

## Permissions

The application requests permissions for capabilities that are controlled by Android and may also require runtime approval:

- Physical activity-related foreground service operation.
- Accelerometer and gyroscope access.
- Notifications.
- Fine and coarse location.
- SMS sending.
- Phone calls.
- Network access for Maps and related functionality.

Grant only the permissions needed for the features you intend to use. Emergency SMS, calls, and location sharing depend on device settings, Android permission state, available hardware, network connectivity, and installed applications.

## Fall detection flow

When monitoring is active, the app reads accelerometer and gyroscope values. The foreground UI combines net acceleration and rotation thresholds to identify a possible fall. A notification and ten-second countdown are shown so the user can cancel a false alarm. If the countdown is not cancelled, the event is added to history and emergency handling is triggered.

The background foreground service also registers the accelerometer and gyroscope so monitoring can continue when the app is not visible. Treat this as a safety-support feature rather than a medical device: sensor readings can produce false positives or miss falls, and emergency actions should always be verified.

## Data and emergency handling

- Settings are stored locally with Android DataStore Preferences.
- Impact data and fall events are handled by the app's local state and local data stores.
- Location is read only when the relevant permission and monitoring behavior allow it.
- Confirmed events can contain timestamp, acceleration, rotation, latitude, longitude, and a readable location string.
- CSV exports include event measurements and location fields.
- Emergency messages can contain the user's name, event time, impact severity, and a Google Maps link.
- SMS and WhatsApp delivery are dependent on Android permissions and external services.

Do not use real personal or emergency-contact information while developing or testing unless the device and recipients are prepared for test alerts.

## Development notes

The authentication repository currently provides a local prototype implementation rather than a production backend. Login and registration behavior should be replaced with a secure server-backed identity system before release.

The project also contains sample impact data and prototype service integrations. Review thresholds, persistence, lifecycle handling, permission flows, and emergency behavior on the target Android versions before using the application outside development or coursework.

## Testing

Run the available unit tests with:

```powershell
.\gradlew.bat test
```

Run instrumented tests on a connected device or emulator with:

```powershell
.\gradlew.bat connectedAndroidTest
```

For sensor and emergency workflows, test on a physical device using safe, simulated movements. Never create a real fall or send an emergency alert as part of a test.

## License

No license has been specified for this project. Until a license is added, assume that all rights are reserved by the copyright holder.