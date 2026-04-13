# Mercury Messenger Portal

Native Android application for field messenger operations, job management, and delivery tracking.

## Overview

Mercury Messenger Portal is a mobile-first solution designed for delivery companies to streamline field operations. Messengers can efficiently manage job assignments, track locations via GPS, capture proof-of-delivery photos, and maintain real-time communication with dispatch centers.

### Key Features

- 🎯 **Job Management** - View, complete, delay, and reorder daily job assignments
- 📍 **GPS Tracking** - Real-time location capture with address geocoding
- 📸 **Photo Capture** - Proof-of-delivery images with automatic compression
- ⏱️ **Time Tracking** - Clock in/out with location verification
- 🔄 **Job Reordering** - Drag-and-drop interface to optimize delivery route
- ❌ **Delay Management** - Report delays with required reason tracking
- 📊 **Day Summary** - Complete job statistics before clocking out
- 🏠 **Home Dashboard** - At-a-glance work status and quick actions

### Target Users

- **Messengers** - Field workers managing deliveries
- **Dispatchers** - Supervisors tracking job status (backend admin panel)
- **Administrators** - System setup and user management

---

## Quick Start

### Prerequisites

- Android 5.0 (API 21) or later
- 50MB free storage
- GPS and Camera access (optional but recommended)
- Internet connection (WiFi or mobile data)

### Installation

1. **From Google Play Store**
   ```
   Open Google Play → Search "Mercury Messenger Portal" → Install
   ```

2. **From APK (Internal Testing)**
   ```bash
   adb install app/build/outputs/apk/release/app-release.apk
   ```

### First Login

```
Employee ID:  EMP001 (or your assigned ID)
Password:     [your password]
→ Tap Login
→ Grant location & camera permissions when prompted
```

### Daily Workflow

```
1. Clock In         → Start your workday (captures GPS location)
2. View Jobs        → See all assigned deliveries for today
3. Complete Jobs    → Travel and mark jobs complete with photos
4. Clock Out        → End workday (requires all jobs completed/delayed)
```

**More details:** See [USER_GUIDE.md](USER_GUIDE.md)

---

## Technology Stack

### Frontend (Mobile)

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Kotlin | 1.9+ |
| UI Framework | Jetpack Compose | Latest stable |
| Architecture | MVVM + Clean Architecture | - |
| State Management | Kotlin Flow + StateFlow | - |
| Async Operations | Coroutines | Latest stable |
| DI Framework | Hilt | Latest stable |

### Backend Integration

| Component | Technology |
|-----------|-----------|
| HTTP Client | Retrofit2 + OkHttp3 |
| JSON Serialization | Gson |
| Data Serialization | Kotlinx Serialization |

### Local Storage

| Component | Technology |
|-----------|-----------|
| Database | Room (SQLite wrapper) |
| Encryption | EncryptedSharedPreferences |
| Caching | In-memory + Database |

### Android Framework

| Component | Technology |
|-----------|-----------|
| Minimum API | 21 (Android 5.0) |
| Target API | 34 (Android 14) |
| Material Design | Material Design 3 |
| Navigation | Jetpack Navigation |
| Lifecycle | AndroidX Lifecycle |

### Development Tools

- **Android Studio** - IDE (Flamingo 2022.2.1+)
- **Gradle** - Build system
- **JUnit4** - Unit testing
- **Mockk** - Mocking framework

---

## Architecture

The app follows **MVVM architecture** with **clean separation of concerns**:

```
┌─────────────────────────┐
│   Presentation Layer    │
│  (Compose UI + VMs)     │
├─────────────────────────┤
│    Domain Layer         │
│  (Models & Logic)       │
├─────────────────────────┤
│     Data Layer          │
│  (API + Database)       │
└─────────────────────────┘
```

**Key Principles:**
- Offline-first (local database is source of truth)
- Reactive UI (automatic updates via StateFlow)
- Type-safe (Kotlin strongly-typed throughout)
- Testable (each layer independently testable)

**Deep Dive:** See [ARCHITECTURE.md](ARCHITECTURE.md)

---

## Project Structure

```
app/src/main/
├── java/com/mercury/messengerportal/
│   ├── ui/                    # Presentation layer (Compose screens)
│   ├── domain/model/          # Domain models (Job, DayLog, etc.)
│   ├── data/
│   │   ├── repository/        # Data orchestration
│   │   ├── remote/            # API (Retrofit)
│   │   └── local/             # Database (Room)
│   ├── di/                    # Dependency injection (Hilt)
│   ├── util/                  # Utilities (GPS, photos, helpers)
│   ├── worker/                # Background tasks
│   ├── service/               # Services (push notifications)
│   └── MainActivity.kt        # Entry point
│
└── res/                       # Resources (layouts, colors, strings)
```

**More details:** See [DEVELOPER_SETUP.md](DEVELOPER_SETUP.md#project-structure)

---

## Building from Source

### Requirements

- JDK 17 (comes with Android Studio)
- Android SDK 34 (API level)
- Gradle 8.0+

### Setup

```bash
# 1. Clone repository
git clone https://github.com/PandoBox/MercuryPortal.git
cd MercuryPortal

# 2. Configure local environment
cp local.properties.example local.properties
# Edit local.properties with your SDK path and API endpoints

# 3. Build debug APK
./gradlew assembleDebug

# 4. Install on device/emulator
./gradlew installDebug
```

### Build Variants

```bash
# Debug (development)
./gradlew assembleDebug        # APK for emulator/testing

# Release (production)
./gradlew assembleRelease      # Signed APK for Play Store
./gradlew bundleRelease        # AAB for Play Store distribution
```

**More details:** See [DEVELOPER_SETUP.md](DEVELOPER_SETUP.md#build--run)

---

## API Integration

The app communicates with a REST backend via Retrofit2. All endpoints are documented with request/response examples.

### Key Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/auth/login` | Authenticate user |
| GET | `/jobs?messengerId={id}&date={date}` | Fetch daily jobs |
| PATCH | `/jobs/{jobId}/status` | Update job status |
| POST | `/day-log/clock-in` | Clock in with location |
| POST | `/day-log/clock-out` | Clock out with location |
| POST | `/photos/upload` | Upload delivery photo |

### Configuration

```kotlin
// Set API base URL in build.gradle.kts
buildTypes {
    debug {
        buildConfigField("String", "API_BASE_URL", 
            "\"https://dev-api.example.com/v1/\"")
    }
    release {
        buildConfigField("String", "API_BASE_URL", 
            "\"https://api.example.com/v1/\"")
    }
}
```

**Complete reference:** See [API_INTEGRATION.md](API_INTEGRATION.md)

---

## Development

### Local Setup

```bash
# 1. Install dependencies
./gradlew build

# 2. Open in Android Studio
open -a "Android Studio" .

# 3. Start emulator or connect device
adb devices

# 4. Run app (Shift+F10)
./gradlew installDebug
```

### Development Tools

- **Logcat** - View app logs
  ```bash
  adb logcat | grep com.mercury.messengerportal
  ```

- **Database Inspector** - Inspect Room database
  - View → Tool Windows → App Inspection

- **Layout Inspector** - Debug UI layouts
  - Tools → Layout Inspector

**More details:** See [DEVELOPER_SETUP.md](DEVELOPER_SETUP.md#development-tools)

### Running Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Code quality checks
./gradlew lint
```

---

## Deployment

### Release Process

1. **Prepare Release**
   - Bump version in `app/build.gradle.kts`
   - Update [CHANGELOG.md](CHANGELOG.md)
   - Create release branch

2. **Build Release APK/AAB**
   ```bash
   ./gradlew bundleRelease
   ```

3. **Sign APK**
   - Configure keystore in `local.properties`
   - Gradle automatically signs release builds

4. **Upload to Google Play**
   - Via Play Console web interface
   - Or use CI/CD pipeline

5. **Monitor Release**
   - Watch crash rates, reviews, ratings
   - Expand rollout gradually (10% → 100%)

**Complete guide:** See [DEPLOYMENT.md](DEPLOYMENT.md)

---

## Documentation

| Document | Purpose |
|----------|---------|
| [README.md](README.md) | Project overview (this file) |
| [ARCHITECTURE.md](ARCHITECTURE.md) | System design & architecture |
| [API_INTEGRATION.md](API_INTEGRATION.md) | Backend API specification |
| [DEVELOPER_SETUP.md](DEVELOPER_SETUP.md) | Local development guide |
| [DEPLOYMENT.md](DEPLOYMENT.md) | Build, sign & release guide |
| [USER_GUIDE.md](USER_GUIDE.md) | Field messenger operations |
| [CHANGELOG.md](CHANGELOG.md) | Version history |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Developer contribution guidelines |

---

## Version History

**Latest: v1.0.3** (Apr 13, 2026)

### v1.0.3 - Bug Fixes & Polish
- Fixed "Depart Next Job" button visibility after completion
- Improved job reordering UX with drag feedback
- Added required reason validation for job delays

### v1.0.2 - Core Features
- Job status transitions with GPS tracking
- Photo capture for deliveries
- Day summary with statistics
- Clock in/out workflow

### v1.0.1 - Initial Release
- User authentication
- Job list with reordering
- Basic job completion

**Full history:** See [CHANGELOG.md](CHANGELOG.md)

---

## Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for:

- Code of conduct
- Development workflow (branching, commits, PRs)
- Code style guidelines
- Testing requirements
- Pull request process

### Quick Start for Contributors

```bash
# 1. Create feature branch
git checkout -b feature/your-feature-name

# 2. Make changes and test locally
./gradlew test

# 3. Commit with clear message
git commit -m "feat: Add your feature description"

# 4. Push and create pull request
git push origin feature/your-feature-name
```

---

## Issues & Support

### Reporting Bugs

Found a bug? Create an issue with:
- Clear description of the problem
- Steps to reproduce
- Expected vs actual behavior
- Device/emulator info
- Logcat output

### Feature Requests

Have a feature idea? Open an issue with:
- Use case description
- Why it's valuable
- Proposed implementation (if you have ideas)

### Getting Help

- **Development questions** → Check [DEVELOPER_SETUP.md](DEVELOPER_SETUP.md#troubleshooting)
- **User questions** → See [USER_GUIDE.md](USER_GUIDE.md#faq)
- **Architecture questions** → Read [ARCHITECTURE.md](ARCHITECTURE.md)

---

## Performance & Analytics

### Key Metrics

- **App Size**: ~15MB base + dynamic delivery
- **Startup Time**: < 2 seconds
- **Memory Usage**: ~100-150MB average
- **Battery Impact**: Minimal (optimized GPS usage)
- **Network Usage**: ~1-5MB per day (depending on photo count)

### Monitoring

- **Crash Reporting**: Firebase Crashlytics
- **Performance**: Play Console metrics
- **User Analytics**: Firebase Analytics (optional)

---

## Security

### Data Protection

- ✅ HTTPS/TLS for all network communication
- ✅ Encrypted token storage (EncryptedSharedPreferences)
- ✅ Permission validation (GPS, Camera, Storage)
- ✅ SQLite database (can be encrypted with SQLCipher - future)

### Best Practices

- Never log sensitive data (tokens, passwords)
- Validate all API responses
- Handle network errors gracefully
- Clear sensitive data on logout

**Security guide:** See [DEPLOYMENT.md](DEPLOYMENT.md#security)

---

## License

[Add your license here - MIT, Apache 2.0, etc.]

---

## Contact

- **Project Lead**: [Your name/email]
- **Tech Lead**: [Tech lead name/email]
- **Support Email**: [support@example.com]

---

## Acknowledgments

Built with:
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Hilt DI](https://dagger.dev/hilt/)
- [Retrofit](https://square.github.io/retrofit/)
- [Room Database](https://developer.android.com/training/data-storage/room)

---

**Version**: 1.0.3  
**Last Updated**: April 13, 2026  
**Status**: Production Ready
