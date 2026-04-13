# Developer Setup Guide

**Mercury Messenger Portal - Local Development Environment**

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Repository Setup](#repository-setup)
3. [Environment Configuration](#environment-configuration)
4. [Build & Run](#build--run)
5. [Project Structure](#project-structure)
6. [Development Tools](#development-tools)
7. [Common Tasks](#common-tasks)
8. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### System Requirements

- **Operating System**: macOS, Linux, or Windows (with WSL2)
- **RAM**: 8GB minimum (16GB recommended)
- **Disk Space**: 10GB free (for SDK and builds)
- **Network**: Internet connection (for Gradle and dependency downloads)

### Required Software

#### Android Studio
- **Version**: Flamingo 2022.2.1 or later
- **Download**: https://developer.android.com/studio
- **Installation**: Standard installation with Android SDK

#### Java Development Kit (JDK)
- **Version**: JDK 17 (bundled with Android Studio Flamingo+)
- **Verify**: `java -version` should show version 17.x

#### Git
- **Version**: 2.30+
- **Install**: 
  - macOS: `brew install git`
  - Linux: `sudo apt-get install git`
  - Windows: https://git-scm.com/download/win
- **Verify**: `git --version`

#### Android SDK
- **API Level**: 34 (target) / 21 (minimum)
- **Components**:
  - Android SDK Platform 34
  - Android SDK Platform-Tools (latest)
  - Android Emulator
  - System Image (arm64-v8a or x86_64)

**Verify SDK installation:**
```bash
echo $ANDROID_HOME
# Should output something like: /Users/username/Library/Android/sdk
```

---

## Repository Setup

### 1. Clone the Repository

```bash
cd ~/projects  # or your preferred directory
git clone https://github.com/PandoBox/MercuryPortal.git
cd MercuryPortal
```

### 2. Verify Repository Structure

```bash
# Should show the following files in root:
ls -la

# Output should include:
# - app/                 (source code)
# - gradle/              (gradle wrapper)
# - build.gradle.kts     (root build config)
# - settings.gradle.kts  (project settings)
# - ARCHITECTURE.md
# - API_INTEGRATION.md
# - .git/                (git repository)
# - .gitignore
```

### 3. Set Git Configuration (Optional but Recommended)

```bash
# Configure Git for Mercury project
git config user.name "Your Name"
git config user.email "your.email@example.com"

# Verify
git config user.name
git config user.email
```

---

## Environment Configuration

### 1. Local Properties (Sensitive Data)

Create `local.properties` file in project root:

```bash
cd /path/to/MercuryPortal
touch local.properties
```

**Add to local.properties:**
```properties
# Android SDK path
sdk.dir=/Users/username/Library/Android/sdk

# API configuration (for development)
API_BASE_URL=https://dev-api.mercury-backend.com/v1/

# Optional: Build signing (see section below)
# RELEASE_STORE_FILE=/path/to/keystore.jks
# RELEASE_STORE_PASSWORD=your_password
# RELEASE_KEY_ALIAS=your_alias
# RELEASE_KEY_PASSWORD=your_password
```

**Never commit local.properties to Git** - it's in `.gitignore`

### 2. Build Variants

The app has multiple build types:

```
buildTypes:
├── debug      (Development)
│   └─ API_BASE_URL = dev-api.mercury-backend.com
│
└── release    (Production)
    └─ API_BASE_URL = api.mercury-backend.com
```

**Configure environment in `build.gradle.kts`:**

```kotlin
buildTypes {
    debug {
        buildConfigField("String", "API_BASE_URL", "\"https://dev-api.mercury-backend.com/v1/\"")
    }
    release {
        buildConfigField("String", "API_BASE_URL", "\"https://api.mercury-backend.com/v1/\"")
    }
}
```

Access in code:
```kotlin
val apiBaseUrl = BuildConfig.API_BASE_URL
```

### 3. Gradle Properties (Optional)

Create or update `gradle.properties`:

```properties
# Gradle performance
org.gradle.jvmargs=-Xmx2048m
org.gradle.parallel=true
org.gradle.caching=true

# Kotlin
kotlin.code.style=official
```

---

## Build & Run

### 1. Open in Android Studio

```bash
# Option 1: Command line
open -a "Android Studio" .

# Option 2: Manual
# - Open Android Studio
# - File → Open → Select MercuryPortal directory
```

**Wait for initial sync** (Gradle downloads ~2GB of dependencies)

### 2. Run Debug Build on Emulator

**Create/Start Android Emulator:**

```bash
# List available AVDs (Android Virtual Devices)
$ANDROID_HOME/emulator/emulator -list-avds

# Start emulator
$ANDROID_HOME/emulator/emulator -avd Pixel_6_API_34 &
```

**Or use Android Studio GUI:**
- Click "Device Manager" (phone icon)
- Select or create device
- Click play button to start

**Build and Run:**

```bash
# Terminal
./gradlew installDebug
adb shell am start -n com.mercury.messengerportal/.MainActivity

# Or use Android Studio
# Click green play button (Run) or press Shift+F10
```

### 3. Run on Physical Device

**Enable USB Debugging:**
1. Settings → About Phone → Tap Build Number 7 times
2. Back to Settings → Developer Options → Enable USB Debugging
3. Connect device via USB

**Build and Run:**
```bash
# List connected devices
adb devices

# Install app
./gradlew installDebug

# App launches automatically
```

### 4. Build Release APK

```bash
# Build unsigned APK
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release-unsigned.apk
```

**Sign APK (see Deployment guide)**

---

## Project Structure

### Key Directories

```
app/src/main/
├── java/com/mercury/messengerportal/
│   ├── ui/                          # UI screens (Compose)
│   │   ├── login/
│   │   ├── home/
│   │   ├── joblist/
│   │   ├── jobdetail/
│   │   ├── camera/
│   │   ├── dayclosing/
│   │   ├── navigation/              # Navigation setup
│   │   ├── components/              # Reusable UI components
│   │   └── theme/
│   │
│   ├── domain/model/                # Domain models
│   │   ├── Job.kt
│   │   ├── JobStatus.kt
│   │   ├── DayLog.kt
│   │   └── ...
│   │
│   ├── data/                        # Data layer
│   │   ├── repository/              # Data orchestration
│   │   ├── remote/                  # API (Retrofit)
│   │   │   ├── ApiService.kt
│   │   │   └── dto/
│   │   └── local/                   # Database (Room)
│   │       ├── AppDatabase.kt
│   │       ├── entity/
│   │       └── dao/
│   │
│   ├── di/                          # Dependency Injection (Hilt)
│   │   ├── DatabaseModule.kt
│   │   └── NetworkModule.kt
│   │
│   ├── util/                        # Utilities
│   │   ├── LocationHelper.kt
│   │   ├── PhotoHelper.kt
│   │   └── ...
│   │
│   └── MainActivity.kt              # App entry point
│
└── res/
    ├── values/
    ├── drawable/
    └── xml/
```

### Important Files

| File | Purpose |
|------|---------|
| `build.gradle.kts` | Root build config |
| `app/build.gradle.kts` | App-level build config |
| `settings.gradle.kts` | Project settings |
| `local.properties` | Local SDK path (not committed) |
| `gradle.properties` | Gradle tuning (optional) |

---

## Development Tools

### 1. Android Studio Features

#### Layout Inspector
Debug UI layouts in real-time:
- Tools → Layout Inspector
- Select running device/emulator
- Inspect view hierarchy

#### Logcat (Logs)
View app and system logs:
- View → Tool Windows → Logcat
- Filter by package: `com.mercury.messengerportal`
- Search for specific tags

#### Database Inspector
Inspect Room database:
- View → Tool Windows → App Inspection
- Select app
- View → Databases
- Browse tables, query data

#### Profiler
Monitor performance:
- View → Tool Windows → Profiler
- Monitor CPU, Memory, Network, Energy

### 2. Command-Line Tools

```bash
# Build variants
./gradlew build                   # All variants
./gradlew assembleDebug           # Debug APK
./gradlew assembleRelease         # Release APK

# Test
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # UI tests (device required)

# Code quality
./gradlew lint                    # Android Lint
./gradlew ktlint                  # Kotlin Lint (if configured)

# Clean
./gradlew clean                   # Remove build artifacts
```

### 3. ADB (Android Debug Bridge)

```bash
# List devices
adb devices

# View logs
adb logcat

# Clear logs
adb logcat -c

# Install APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Push file to device
adb push photo.jpg /sdcard/DCIM/

# Pull file from device
adb pull /sdcard/DCIM/photo.jpg ./

# Open app
adb shell am start -n com.mercury.messengerportal/.MainActivity

# Clear app data
adb shell pm clear com.mercury.messengerportal
```

### 4. Emulator Configuration

```bash
# Create new AVD (Android Virtual Device)
$ANDROID_HOME/cmdline-tools/latest/bin/avdmanager create avd \
  -n Pixel_6_API_34 \
  -k "system-images;android-34;google_apis;arm64-v8a" \
  -d "Pixel 6"

# Start emulator
$ANDROID_HOME/emulator/emulator -avd Pixel_6_API_34

# Emulator with GPS mock
# Settings → Developer Options → Mock Location App → Mercury Portal
```

---

## Common Tasks

### 1. Add a New Dependency

**In `app/build.gradle.kts`:**

```kotlin
dependencies {
    // Example: Add a new library
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
}
```

**Sync Gradle:**
```bash
./gradlew build
# Or click "Sync Now" in Android Studio
```

### 2. Create a New Screen

**Files to create:**

1. **Screen UI** (`ui/myfeature/MyScreen.kt`):
```kotlin
@Composable
fun MyScreen(
    onBack: () -> Unit,
    viewModel: MyViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // UI here
}
```

2. **ViewModel** (`ui/myfeature/MyViewModel.kt`):
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: MyRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyUiState())
    val uiState: StateFlow<MyUiState> = _uiState.asStateFlow()
}
```

3. **Add to Navigation** (`ui/navigation/NavGraph.kt`):
```kotlin
composable(Screen.MyScreen.route) {
    MyScreen(onBack = { navController.popBackStack() })
}
```

### 3. Modify Database Schema

**Update entity:**
```kotlin
// data/local/entity/MyEntity.kt
@Entity(tableName = "my_table")
data class MyEntity(
    @PrimaryKey val id: String,
    val newField: String  // Add new field
)
```

**Increment version in AppDatabase.kt:**
```kotlin
@Database(
    entities = [...],
    version = 7  // Increment from 6
)
```

**For development** (destructive migration):
```kotlin
Room.databaseBuilder(context, AppDatabase::class.java, "mercury_db")
    .fallbackToDestructiveMigration()  // Clears old data
    .build()
```

**For production** (write migration):
```kotlin
val migration_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE my_table ADD COLUMN newField TEXT")
    }
}

Room.databaseBuilder(context, AppDatabase::class.java, "mercury_db")
    .addMigrations(migration_6_7)
    .build()
```

### 4. Add API Endpoint

**1. Update DTO:**
```kotlin
// data/remote/dto/Dtos.kt
data class MyRequest(val field: String)
data class MyResponse(val result: String)
```

**2. Add to ApiService:**
```kotlin
// data/remote/ApiService.kt
@POST("my-endpoint")
suspend fun myEndpoint(@Body request: MyRequest): Response<MyResponse>
```

**3. Add to Repository:**
```kotlin
// data/repository/MyRepository.kt
suspend fun myOperation() {
    try {
        val response = apiService.myEndpoint(MyRequest(...))
        if (response.isSuccessful) {
            // Update local cache
        }
    } catch (e: Exception) {
        // Handle error
    }
}
```

**4. Use in ViewModel:**
```kotlin
// ui/myscreen/MyViewModel.kt
fun performOperation() {
    viewModelScope.launch {
        try {
            repository.myOperation()
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message) }
        }
    }
}
```

### 5. Test a Feature

**Unit Test:**
```kotlin
// In src/test/kotlin/
class MyViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    @Test
    fun testMyFunction() = runTest {
        // Mock dependencies
        val mockRepository = mockk<MyRepository>()
        val viewModel = MyViewModel(mockRepository)
        
        // Execute
        viewModel.performOperation()
        
        // Assert
        val state = viewModel.uiState.value
        assertEquals("expected", state.result)
    }
}
```

**Run tests:**
```bash
./gradlew test
# or in Android Studio: Run → Run Tests
```

### 6. Debug the App

**Set Breakpoints:**
- Click line number in code
- Green dot appears
- Run app in debug mode (green bug icon)

**Debug controls (when breakpoint hit):**
- Step Over (F6)
- Step Into (F7)
- Resume (Cmd+Option+R)
- Evaluate Expression (Cmd+Option+E)

### 7. View Database

**In Android Studio:**
1. View → Tool Windows → App Inspection
2. Select running device
3. Select app
4. View → Databases
5. Browse tables and data

**Or via ADB:**
```bash
adb shell
cd /data/data/com.mercury.messengerportal/databases/
sqlite3 mercury_db
> .tables
> SELECT * FROM jobs LIMIT 5;
```

---

## Troubleshooting

### Build Issues

#### Gradle Sync Fails
```bash
# Clear Gradle cache
./gradlew clean

# Rebuild
./gradlew build

# Or invalidate Android Studio cache:
# File → Invalidate Caches → Invalidate and Restart
```

#### Java Version Error
```bash
# Verify Java version
java -version

# Should show 17.x or later
# If not, set JAVA_HOME:
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

#### Kotlin Compilation Error
- Check Kotlin plugin version in `build.gradle.kts`
- Update: `kotlin("android") version "1.9.x"`

### Runtime Issues

#### App Crashes on Startup
```bash
# Check logcat for error
adb logcat | grep "com.mercury.messengerportal"

# Common issues:
# - Missing permissions in AndroidManifest.xml
# - Database migration failed
# - Hilt dependency not injected
```

#### Emulator Too Slow
```bash
# Use x86_64 system image (faster than arm64)
# Enable hardware acceleration in Android Studio:
# Settings → System Settings → Emulator → Use system QEMU
```

#### GPS Not Working
```bash
# In emulator, send mock location:
adb emu geo fix 13.7247 100.5317  # Bangkok coordinates

# Or in emulator Extended Controls:
# Hamburger menu → Extended controls → Location → Manual
```

#### Camera Permission Denied
```bash
# Grant permission via adb
adb shell pm grant com.mercury.messengerportal android.permission.CAMERA

# Or in emulator Settings → Apps → Mercury Portal → Permissions
```

### Network Issues

#### API Calls Fail in Debug
- Check `API_BASE_URL` in `build.gradle.kts`
- Verify backend is running
- Check emulator network: `adb shell ping google.com`

#### HTTPS Certificate Error
```bash
# For development, use non-HTTPS or add cert to network_security_config.xml
# Never do this in production!
```

---

## Best Practices

### Code Style

- Follow Kotlin conventions
- Use `.editorconfig` for consistency
- Run `./gradlew ktlint` before commit

### Git Workflow

```bash
# Create feature branch
git checkout -b feature/job-reordering

# Make changes, test locally
# ...

# Commit with clear message
git commit -m "feat: Add drag-and-drop job reordering"

# Push to remote
git push origin feature/job-reordering

# Create pull request on GitHub
```

### Testing

- Write tests for business logic (ViewModel, Repository)
- Use mocks for external dependencies
- Run tests before pushing: `./gradlew test`

### Database Migrations

- Always write migrations for production database changes
- Test migrations on both fresh install and upgrade
- Keep migration files for audit trail

### Logging

```kotlin
// Good: Use tags
private val TAG = "JobDetailViewModel"
Log.d(TAG, "Job loaded: ${job.id}")

// Avoid logging sensitive data
// ❌ Bad: Log.d("API", "Token: $token")
// ✅ Good: Log.d("API", "Login successful")
```

---

## Resources

- **Android Developer Docs**: https://developer.android.com/
- **Jetpack Compose**: https://developer.android.com/jetpack/compose
- **Room Database**: https://developer.android.com/training/data-storage/room
- **Hilt DI**: https://dagger.dev/hilt/
- **Coroutines**: https://kotlinlang.org/docs/coroutines-overview.html
- **Retrofit**: https://square.github.io/retrofit/

---

## Getting Help

### Debug Checklist

When something doesn't work:
1. [ ] Check logcat for error messages
2. [ ] Verify Gradle sync successful
3. [ ] Run `./gradlew clean && ./gradlew build`
4. [ ] Invalidate Android Studio cache
5. [ ] Restart emulator/device
6. [ ] Check API Base URL configuration
7. [ ] Verify permissions in AndroidManifest.xml
8. [ ] Test with physical device (not just emulator)

### Asking for Help

Include:
- Error logs (from logcat or build output)
- Steps to reproduce
- Android Studio version
- Device/emulator info
- Recent changes made

---

**Document Version**: 1.0  
**Last Updated**: 2026-04-13  
**Status**: Ready for Development Team
