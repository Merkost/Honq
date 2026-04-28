# Honq — Australian DKT Test

> Clean, focused practice for the Australian Driver Knowledge Test. No ads, no subscriptions, no clutter.

Honq is a Kotlin Multiplatform app (Android + iOS) for studying the Australian driver-licence knowledge test. It ships timed mock tests, full-history practice, smart spaced repetition, per-category drilling, and an offline-first content cache.

This repository contains the source for the production app available on the App Store and Google Play.

> ⚠️ **Independent app.** Honq is not affiliated with, endorsed by, or representing any Australian government department or licensing authority. It is a study tool. Always defer to the official sources linked in-app for authoritative information.

---

## Status

| Platform | Status |
|---|---|
| Android | Released |
| iOS | Released |
| Question banks | NSW, NT (more states linked to official handbooks where no public bank exists) |

---

## Tech stack

- **Kotlin Multiplatform** — shared business logic, data layer, repositories, use cases
- **Compose Multiplatform** — shared UI for both platforms
- **Room** (KMP) — offline question + progress cache
- **Cloud Firestore** — content backend (questions, categories, mock-test rules)
- **Firebase Hosting** — question images, served via CDN
- **Firebase Crashlytics** — crash reporting
- **RevenueCat** — Pro upgrade (one-time purchase, no subscriptions)
- **Amplitude** — privacy-respecting analytics
- **Koin** — dependency injection
- **Ktor** — networking
- **Coil** — image loading
- **FlowMVI** — UI state management
- **Cedar** — structured logging

---

## Project layout

```
.
├── shared/                    # Kotlin Multiplatform module
│   └── src/
│       ├── commonMain/        # Shared code: domain, data, presentation
│       ├── androidMain/       # Android-only platform glue
│       ├── iosMain/           # iOS-only platform glue
│       └── commonTest/        # Shared tests
├── androidApp/                # Android entry point + Play Integrity glue
├── iosApp/                    # iOS entry point (SwiftUI shell + Compose host)
├── public/                    # Firebase Hosting publish dir (question images)
│   └── questions/             # Per-state PNG subdirectories — gitignored
├── firebase.json              # Firestore + Hosting config
├── firestore.rules            # Public-read security rules
├── docs/                      # Public docs (data sources, schema)
└── scripts/                   # Tooling (one-shot data exporters, etc.)
```

---

## Build & run

### Prerequisites

- **JDK 17+** (Android Gradle Plugin 9.x requirement)
- **Xcode 15+** for the iOS app
- **Android Studio Iguana or newer** (or IntelliJ + KMM plugin)
- **Firebase CLI** for Hosting deploys: `brew install firebase-cli`

### Configuration

Copy `local.properties.example` to `local.properties` (if present) and provide:

```properties
# Required
firebase.project.id=<your firebase project id>
firebase.hosting.base.url=https://<your-project>.web.app

# Optional — only if you want analytics or RevenueCat in dev
amplitude.api.key=<your amplitude write key>
google.cloud.project.number=<your gcp project number>
```

Place these per-platform Firebase config files (downloaded from the Firebase Console):
- `androidApp/google-services.json`
- `iosApp/iosApp/GoogleService-Info.plist`

Both are gitignored.

### Android

```bash
./gradlew :androidApp:installDebug
```

Or open in Android Studio and use the run configuration.

### iOS

Open `iosApp/HonqApp.xcodeproj` in Xcode, select a simulator or device, and ⌘R.

Or build the shared framework first from the command line, then build via Xcode:

```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
open iosApp/HonqApp.xcodeproj
```

---

## Running tests

```bash
./gradlew :shared:testDebugUnitTest
```

Tests are sparse on purpose — focused on pure-logic helpers (URL builders, mappers, etc.) where regressions are likely. UI and Firestore-bound code is covered by manual smoke testing on real devices.

---

## Content workflow

Question content (text, options, explanations, images) lives in Firestore + Firebase Hosting. Updates are made by editing Firestore documents directly via the Firebase Console (or a small import script for bulk changes), then deploying any new images:

```bash
# Add new images under public/questions/<state>/, then:
firebase deploy --only hosting
```

The image URL pattern is `https://<your-project>.web.app/questions/<state>/<filename>.png`. Images are served with a year-long immutable cache header — to replace an image, pick a new filename and update the corresponding question doc's `image_url` field.

See [`docs/FIRESTORE_SCHEMA.md`](docs/FIRESTORE_SCHEMA.md) for the full schema.

---

## Data sources

In-app practice question banks are sourced from the official handbooks of states that publish a public question bank (NSW, NT). For other states, Honq links out to the official practice tests and handbooks rather than fabricating questions.

Official sources are documented in [`docs/DATA_SOURCES.md`](docs/DATA_SOURCES.md).

---

## License

TBD — choose a license before pushing public.

---

## Acknowledgements

- All Australian state and territory road authorities for their freely available study materials.
- The Kotlin and Compose Multiplatform teams for making cross-platform sharing actually pleasant.
