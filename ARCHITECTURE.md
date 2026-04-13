# Architecture Document

**Mercury Messenger Portal - System Design & Architecture**

---

## Table of Contents

1. [Overview](#overview)
2. [Technology Stack](#technology-stack)
3. [High-Level Architecture](#high-level-architecture)
4. [Project Structure](#project-structure)
5. [Architectural Layers](#architectural-layers)
6. [Data Flow](#data-flow)
7. [Dependency Injection](#dependency-injection)
8. [Database Schema](#database-schema)
9. [State Management](#state-management)
10. [Navigation Architecture](#navigation-architecture)
11. [Design Patterns](#design-patterns)
12. [Key Decisions & Rationale](#key-decisions--rationale)

---

## Overview

Mercury Messenger Portal is a native Android application designed for field messengers to manage job deliveries, track locations, and capture completion photos. The app follows **clean architecture principles** with clear separation of concerns across three main layers: Presentation, Domain, and Data.

### Architecture Principles

- **Single Responsibility**: Each component has one reason to change
- **Dependency Inversion**: High-level modules don't depend on low-level modules
- **Testability**: Each layer can be tested independently with mocks
- **Offline-First**: Local database is the source of truth
- **Reactive**: UI automatically updates when data changes via Kotlin Flows
- **Type Safety**: Strong typing throughout, no stringly-typed data

### Target Users

- **Messengers**: Field workers completing delivery jobs
- **Dispatchers**: Supervisors monitoring job status (backend only)
- **Admins**: System administrators (backend only)

---

## Technology Stack

### Android Framework & UI

| Technology | Purpose | Version |
|------------|---------|---------|
| **Kotlin** | Primary language | 1.9+ |
| **Jetpack Compose** | Modern declarative UI | Latest stable |
| **Material Design 3** | UI components & theming | Latest stable |
| **AndroidX** | Core Android libraries | Latest stable |

### Architecture & State Management

| Technology | Purpose |
|------------|---------|
| **MVVM** | Presentation layer pattern |
| **Kotlin Flow** | Reactive data streams |
| **StateFlow** | UI state management |
| **Coroutines** | Async/background operations |
| **ViewModel** | Lifecycle-aware state holder |

### Local Storage

| Technology | Purpose |
|------------|---------|
| **Room Database** | Local SQLite wrapper with type safety |
| **Migrations** | Schema versioning and updates |

### Remote Communication

| Technology | Purpose |
|------------|---------|
| **Retrofit2** | HTTP REST client |
| **OkHttp3** | HTTP client with interceptors |
| **Gson** | JSON serialization/deserialization |

### Dependency Injection

| Technology | Purpose |
|------------|---------|
| **Hilt** | Android-specific DI framework |
| **Dagger 2** | Compile-time dependency resolution |

### Device Features

| Technology | Purpose |
|------------|---------|
| **GPS/Location** | Real-time location tracking |
| **Camera** | Photo capture for job completion |
| **File System** | Photo storage and access |

### Background Tasks

| Technology | Purpose |
|------------|---------|
| **WorkManager** | Scheduled background sync |
| **Service** | Long-running operations |

---

## High-Level Architecture

### System Components Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  (Compose UI + ViewModels + State Management)               │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ HomeScreen   │  │ JobList      │  │ JobDetail    │ ...  │
│  │ + ViewModel  │  │ + ViewModel  │  │ + ViewModel  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│         ↓                ↓                   ↓               │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│                    Domain Layer                              │
│         (Business Logic & Data Models)                       │
│                                                              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │ Job Model   │  │ JobStatus   │  │ DayLog      │ ...     │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
│                                                              │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│                    Data Layer                                │
│  (Repository Pattern + API + Database)                       │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ JobRepository                                        │   │
│  │  ├─ Remote: ApiService (Retrofit)                   │   │
│  │  └─ Local: JobDao (Room)                            │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ AuthRepository                                       │   │
│  │  ├─ Remote: ApiService                              │   │
│  │  └─ Local: SharedPreferences (encrypted)            │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
└─────────────────────────────────────────────────────────────┘
         ↓                        ↓                  ↓
   ┌──────────┐          ┌──────────────┐    ┌──────────┐
   │ REST API │          │ Room Database│    │ SharedPr.│
   │(Backend) │          │ (SQLite)     │    │(Tokens)  │
   └──────────┘          └──────────────┘    └──────────┘
```

---

## Project Structure

```
app/src/main/java/com/mercury/messengerportal/
│
├── MainActivity.kt                          # Entry point
├── MercuryApp.kt                           # App class (Hilt setup)
│
├── di/                                      # Dependency Injection
│   ├── DatabaseModule.kt                   # Room database provision
│   ├── NetworkModule.kt                    # Retrofit provision
│   └── RepositoryModule.kt                 # Repository provision
│
├── domain/                                  # Business Logic Layer
│   └── model/
│       ├── Job.kt                          # Job domain model
│       ├── JobStatus.kt                    # Job status enum
│       ├── JobStatusLog.kt                 # Status change log
│       ├── DayLog.kt                       # Day work log
│       ├── Messenger.kt                    # Messenger info
│       └── JobType.kt                      # Job type enum
│
├── data/                                    # Data Access Layer
│   ├── remote/
│   │   ├── ApiService.kt                   # Retrofit REST interface
│   │   └── dto/
│   │       └── Dtos.kt                     # API data transfer objects
│   │
│   ├── local/
│   │   ├── AppDatabase.kt                  # Room database setup
│   │   ├── entity/
│   │   │   ├── JobEntity.kt                # Job table entity
│   │   │   ├── JobStatusLogEntity.kt       # Status log table
│   │   │   └── DayLogEntity.kt             # Day log table
│   │   └── dao/
│   │       ├── JobDao.kt                   # Job data access
│   │       ├── JobStatusLogDao.kt          # Status log access
│   │       └── DayLogDao.kt                # Day log access
│   │
│   └── repository/
│       ├── JobRepository.kt                # Job data orchestration
│       └── AuthRepository.kt               # Auth data orchestration
│
├── ui/                                      # Presentation Layer
│   ├── navigation/
│   │   ├── NavGraph.kt                     # Navigation setup
│   │   └── Screen.kt                       # Screen definitions
│   │
│   ├── login/
│   │   ├── LoginScreen.kt                  # Login UI
│   │   └── LoginViewModel.kt               # Login logic & state
│   │
│   ├── home/
│   │   ├── HomeScreen.kt                   # Home/dashboard UI
│   │   └── HomeViewModel.kt                # Home logic & state
│   │
│   ├── joblist/
│   │   ├── JobListScreen.kt                # Job list UI
│   │   └── JobListViewModel.kt             # Job list logic
│   │
│   ├── jobdetail/
│   │   ├── JobDetailScreen.kt              # Job detail UI
│   │   └── JobDetailViewModel.kt           # Job detail logic
│   │
│   ├── camera/
│   │   └── CameraScreen.kt                 # Photo capture UI
│   │
│   ├── dayclosing/
│   │   ├── DayClosingScreen.kt             # Day summary UI
│   │   └── DayClosingViewModel.kt          # Day closing logic
│   │
│   ├── components/
│   │   ├── JobCard.kt                      # Reusable job card
│   │   └── StatusChip.kt                   # Status display
│   │
│   └── theme/
│       ├── Color.kt                        # Color palette
│       ├── Theme.kt                        # Material 3 theme
│       └── Type.kt                         # Typography
│
├── util/                                    # Utilities
│   ├── Extensions.kt                       # Kotlin extensions
│   ├── LocationHelper.kt                   # GPS & geocoding
│   ├── PhotoHelper.kt                      # Photo compression
│   ├── PilotConfig.kt                      # Feature flags
│   └── Constants.kt                        # App constants
│
├── worker/                                  # Background Tasks
│   └── SyncWorker.kt                       # Data sync worker
│
└── service/
    └── MercuryMessagingService.kt          # Firebase messaging
```

---

## Architectural Layers

### 1. Presentation Layer

**Responsibility**: Display UI and handle user interactions

**Components**:
- **Screens** (Compose): Declarative UI compositions
- **ViewModels**: State holders that survive configuration changes
- **State Objects**: Data classes representing UI state

**Technologies**:
- Jetpack Compose for UI
- ViewModel for lifecycle management
- StateFlow/Flow for reactive updates
- Material Design 3 for components

**Example - JobDetailViewModel**:
```kotlin
@HiltViewModel
class JobDetailViewModel @Inject constructor(
    private val jobRepository: JobRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(JobDetailUiState())
    val uiState: StateFlow<JobDetailUiState> = _uiState.asStateFlow()
    
    fun loadJob(jobId: String) {
        viewModelScope.launch {
            jobRepository.observeJob(jobId).collect { job ->
                _uiState.update { state -> 
                    state.copy(job = job, isLoading = false) 
                }
            }
        }
    }
}
```

**Key Characteristics**:
- No direct database or API calls
- All data comes from repositories
- Communicates exclusively through StateFlow/Flow
- No Android context dependencies (except for view)

---

### 2. Domain Layer

**Responsibility**: Business logic and domain models

**Components**:
- **Domain Models**: Pure Kotlin data classes (no Android dependencies)
- **Enums**: JobStatus, JobType, DeliverySession, etc.
- **Use Cases**: Business rules (optional, not heavily used here)

**Technologies**:
- Plain Kotlin data classes
- No external dependencies
- Fully testable without Android framework

**Domain Models**:

```kotlin
// Job domain model - source of truth for job entity
data class Job(
    val id: String,
    val title: String,
    val type: JobType,
    val senderName: String,
    val senderPhone: String,
    val receiverName: String,
    val receiverPhone: String,
    val locationName: String,
    val locationAddress: String,
    val latitude: Double,
    val longitude: Double,
    val sequenceOrder: Int,
    val status: JobStatus,
    val assignedAt: Long,
    val notes: String,
    val messengerRemark: String,
    val messengerId: String
)

enum class JobStatus {
    ASSIGNED, DEPARTED, ARRIVED, COMPLETED, DELAYED
}

data class DayLog(
    val id: String,
    val messengerId: String,
    val date: String,
    val clockInTime: Long?,
    val clockOutTime: Long?,
    val clockInLat: Double?,
    val clockInLng: Double?,
    val clockOutLat: Double?,
    val clockOutLng: Double?
)

data class JobStatusLog(
    val id: String,
    val jobId: String,
    val fromStatus: JobStatus,
    val toStatus: JobStatus,
    val timestamp: Long,
    val latitude: Double?,
    val longitude: Double?,
    val photoUrl: String?,
    val note: String?
)
```

**Key Characteristics**:
- Pure Kotlin, no Android dependencies
- Immutable data classes
- Clear domain boundaries
- Business logic validation

---

### 3. Data Layer

**Responsibility**: Data acquisition and persistence

**Sub-components**:

#### 3a. Remote Data Source (API)

```kotlin
interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @GET("jobs")
    suspend fun getAssignedJobs(
        @Query("messengerId") messengerId: String,
        @Query("date") date: String
    ): Response<List<JobDto>>
    
    @PATCH("jobs/{jobId}/status")
    suspend fun updateJobStatus(
        @Path("jobId") jobId: String,
        @Body request: StatusUpdateRequest
    ): Response<StatusUpdateResponse>
}
```

**Key Characteristics**:
- Retrofit REST interface
- Type-safe API contracts
- Request/response DTOs
- No business logic

#### 3b. Local Data Source (Database)

**Database Schema**:

```
┌─ jobs ────────────────────────┐
│ id (PK)                        │
│ title                          │
│ type                           │
│ senderName                     │
│ senderPhone                    │
│ receiverName                   │
│ receiverPhone                  │
│ locationName                   │
│ locationAddress                │
│ latitude                       │
│ longitude                      │
│ sequenceOrder                  │
│ status                         │
│ assignedAt                     │
│ notes                          │
│ messengerRemark                │
│ messengerId (FK → messengers)  │
└────────────────────────────────┘

┌─ job_status_logs ──────────────┐
│ id (PK)                         │
│ jobId (FK → jobs)              │
│ fromStatus                      │
│ toStatus                        │
│ timestamp                       │
│ latitude                        │
│ longitude                       │
│ photoUrl                        │
│ note                            │
└─────────────────────────────────┘

┌─ day_logs ─────────────────────┐
│ id (PK)                         │
│ messengerId                     │
│ date                            │
│ clockInTime                     │
│ clockOutTime                    │
│ clockInLat                      │
│ clockInLng                      │
│ clockOutLat                     │
│ clockOutLng                     │
└─────────────────────────────────┘
```

**DAOs (Data Access Objects)**:

```kotlin
@Dao
interface JobDao {
    @Query("SELECT * FROM jobs WHERE messengerId = :messengerId ORDER BY sequenceOrder")
    fun observeJobsByMessenger(messengerId: String): Flow<List<JobEntity>>
    
    @Query("SELECT * FROM jobs WHERE id = :jobId")
    fun observeJob(jobId: String): Flow<JobEntity>
    
    @Update
    suspend fun updateJob(job: JobEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobs: List<JobEntity>)
}
```

**Key Characteristics**:
- Room database with SQLite
- Type-safe DAOs
- Automatic schema migration
- Returns Flow for reactive updates

#### 3c. Repository Pattern

```kotlin
@Singleton
class JobRepository @Inject constructor(
    private val jobDao: JobDao,
    private val apiService: ApiService,
    private val locationHelper: LocationHelper
) {
    fun observeJob(jobId: String): Flow<Job> = 
        jobDao.observeJob(jobId).map { it.toDomain() }
    
    suspend fun updateJobStatus(
        job: Job,
        newStatus: JobStatus,
        latitude: Double?,
        longitude: Double?,
        locationAddress: String?,
        photoUrl: String?,
        isNetworkAvailable: Boolean
    ) {
        try {
            val logId = UUID.randomUUID().toString()
            val request = StatusUpdateRequest(
                logId = logId,
                status = newStatus.name,
                latitude = latitude,
                longitude = longitude,
                deviceTimestamp = System.currentTimeMillis(),
                photoUrl = photoUrl
            )
            
            // Call API
            val response = apiService.updateJobStatus(job.id, request)
            
            if (response.isSuccessful) {
                // Update local cache
                val jobEntity = job.toEntity().copy(status = newStatus.name)
                jobDao.updateJob(jobEntity)
                
                // Log status change
                val logEntity = JobStatusLogEntity(
                    id = logId,
                    jobId = job.id,
                    fromStatus = job.status.name,
                    toStatus = newStatus.name,
                    timestamp = System.currentTimeMillis(),
                    latitude = latitude,
                    longitude = longitude,
                    photoUrl = photoUrl,
                    note = null
                )
                jobStatusLogDao.insertLog(logEntity)
            }
        } catch (e: Exception) {
            // Handle error
            throw ApiException("Failed to update job status", e)
        }
    }
}
```

**Repository Responsibilities**:
1. **Data Abstraction**: Hide API and database complexity
2. **Caching Strategy**: Use database as cache
3. **Orchestration**: Coordinate between multiple sources
4. **Error Handling**: Convert exceptions to domain exceptions
5. **Transformation**: Map DTOs ↔ Domain Models

**Key Characteristics**:
- Single point of data access
- Offline-first (local DB is source of truth)
- Transparent to ViewModels
- Handles network errors gracefully

---

## Data Flow

### Complete User Flow: Complete a Job

```
1. User on JobDetailScreen clicks "Complete" button
   ↓
2. JobDetailViewModel.onPrimaryAction() called
   ├─ Checks if photo required
   └─ If yes: navigate to CameraScreen, else continue
   ↓
3. Capture location (GPS) + optional photo
   ↓
4. commitStatusUpdate(job, JobStatus.COMPLETED)
   ├─ Set justStatusChanged = true in UI state
   ├─ Call jobRepository.updateJobStatus()
   │  ├─ Call apiService.updateJobStatus() [Remote]
   │  ├─ Save response with serverTimestamp
   │  ├─ Insert JobStatusLogEntity [Local]
   │  └─ Update JobEntity status [Local]
   │
   └─ StateFlow emits new state → UI updates
   ↓
5. JobDetailScreen observes state change
   ├─ isLoading = false
   ├─ job.status = COMPLETED
   └─ justStatusChanged = true
   ↓
6. UI renders "Depart Next Job" button (visible because justStatusChanged = true)
   ↓
7. User clicks "Depart Next Job"
   ↓
8. onDepartNextJob() called
   ├─ Capture location again
   ├─ Find next job with status ASSIGNED
   ├─ Call jobRepository.updateJobStatus() on next job
   │  └─ Update next job to DEPARTED
   │
   └─ Set navigateToJobId in state
   ↓
9. LaunchedEffect observes navigateToJobId change
   ├─ Navigate to JobDetail screen with new jobId
   └─ Clear navigateToJobId flag
   ↓
10. New JobDetailScreen loads new job and repeats
```

### Data Persistence Flow

```
API Response (JSON)
       ↓
Retrofit deserializes → JobDto (Data Transfer Object)
       ↓
Repository converts → Job (Domain Model)
       ↓
Emitted via StateFlow → ViewModel
       ↓
ViewModel updates state → JobDetailUiState
       ↓
Compose observes state → UI renders
       ↓
[Local Cache]
Repository simultaneously saves:
JobDto → JobEntity → JobDao.insertJob() → Room Database
       ↓
Next app start:
Room queries database → JobEntity → Job domain model → StateFlow
(No network call needed - offline-first)
```

---

## Dependency Injection

### Hilt Setup

```kotlin
// MercuryApp.kt
@HiltAndroidApp
class MercuryApp : Application()

// DatabaseModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "mercury_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideJobDao(db: AppDatabase): JobDao = db.jobDao()
    // ... other DAOs
}

// NetworkModule.kt
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(httpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}
```

### Injection Points

```kotlin
@HiltViewModel
class JobDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val jobRepository: JobRepository  // ← Provided by Hilt
) : ViewModel() { ... }

@Composable
fun JobDetailScreen(
    viewModel: JobDetailViewModel = hiltViewModel()  // ← Hilt creates
) { ... }
```

### Dependency Graph

```
Application
├─ AppDatabase (Singleton)
│  ├─ JobDao
│  ├─ JobStatusLogDao
│  └─ DayLogDao
│
├─ OkHttpClient (Singleton)
│
├─ Retrofit (Singleton)
│  └─ ApiService
│
├─ JobRepository (Singleton)
│  ├─ JobDao
│  └─ ApiService
│
├─ AuthRepository (Singleton)
│  └─ ApiService
│
└─ ViewModel Factories (Per-screen)
   ├─ JobDetailViewModel
   │  └─ JobRepository
   ├─ HomeViewModel
   │  ├─ JobRepository
   │  └─ AuthRepository
   └─ ...
```

**Benefits**:
- Compile-time dependency validation
- Easy to test (swap implementations)
- Lifecycle-aware scoping (Singleton vs ViewModel)
- No boilerplate service locator pattern

---

## Database Schema

### Tables

#### jobs
```sql
CREATE TABLE jobs (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    type TEXT NOT NULL,
    senderName TEXT NOT NULL,
    senderPhone TEXT NOT NULL,
    receiverName TEXT NOT NULL,
    receiverPhone TEXT NOT NULL,
    locationName TEXT NOT NULL,
    locationAddress TEXT NOT NULL,
    latitude REAL NOT NULL,
    longitude REAL NOT NULL,
    sequenceOrder INTEGER NOT NULL,
    status TEXT NOT NULL,
    assignedAt INTEGER NOT NULL,
    notes TEXT,
    messengerRemark TEXT,
    messengerId TEXT NOT NULL,
    serviceRequest TEXT,
    deliverySession TEXT,
    specifyTime TEXT,
    refNo TEXT,
    requesterDepartment TEXT
);

CREATE INDEX idx_jobs_messengerId ON jobs(messengerId);
CREATE INDEX idx_jobs_status ON jobs(status);
CREATE INDEX idx_jobs_sequenceOrder ON jobs(sequenceOrder);
```

#### job_status_logs
```sql
CREATE TABLE job_status_logs (
    id TEXT PRIMARY KEY,
    jobId TEXT NOT NULL,
    fromStatus TEXT NOT NULL,
    toStatus TEXT NOT NULL,
    timestamp INTEGER NOT NULL,
    latitude REAL,
    longitude REAL,
    photoUrl TEXT,
    note TEXT,
    FOREIGN KEY(jobId) REFERENCES jobs(id)
);

CREATE INDEX idx_job_status_logs_jobId ON job_status_logs(jobId);
CREATE INDEX idx_job_status_logs_timestamp ON job_status_logs(timestamp);
```

#### day_logs
```sql
CREATE TABLE day_logs (
    id TEXT PRIMARY KEY,
    messengerId TEXT NOT NULL,
    date TEXT NOT NULL,
    clockInTime INTEGER,
    clockOutTime INTEGER,
    clockInLat REAL,
    clockInLng REAL,
    clockOutLat REAL,
    clockOutLng REAL
);

CREATE INDEX idx_day_logs_messengerId_date ON day_logs(messengerId, date);
```

### Schema Versioning

```kotlin
@Database(
    entities = [JobEntity::class, JobStatusLogEntity::class, DayLogEntity::class],
    version = 6,  // Current version
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() { ... }
```

**Migration Strategy**:
- `version = 6`: Current production version
- New fields added with default values
- `fallbackToDestructiveMigration()` for development (clears on upgrade)
- For production: write explicit migrations

**Data Retention**:
- Jobs kept indefinitely (historical data)
- Status logs kept for audit trail (1+ years)
- Day logs kept for payroll/analytics (1+ years)

---

## State Management

### ViewModel State Pattern

```kotlin
// State data class (immutable)
data class JobDetailUiState(
    val job: Job? = null,
    val statusLogs: List<JobStatusLog> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val pendingCameraLogId: String? = null,
    val pendingNextStatus: JobStatus? = null,
    val pendingLatitude: Double? = null,
    val pendingLongitude: Double? = null,
    val pendingLocationAddress: String? = null,
    val pendingCommit: Boolean = false,
    val showArrivalLocationPicker: Boolean = false,
    val showDelayDialog: Boolean = false,
    val delayReason: String = "",
    val justStatusChanged: Boolean = false
)

// ViewModel manages state
@HiltViewModel
class JobDetailViewModel @Inject constructor(...) : ViewModel() {
    private val _uiState = MutableStateFlow(JobDetailUiState())
    val uiState: StateFlow<JobDetailUiState> = _uiState.asStateFlow()
    
    fun updateState(reducer: (JobDetailUiState) -> JobDetailUiState) {
        _uiState.update(reducer)
    }
    
    fun commitStatusUpdate(job: Job, nextStatus: JobStatus) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            try {
                // Perform API call
                jobRepository.updateJobStatus(...)
                updateState { it.copy(
                    isLoading = false,
                    justStatusChanged = true
                ) }
            } catch (e: Exception) {
                updateState { it.copy(
                    isLoading = false,
                    error = e.message
                ) }
            }
        }
    }
}

// UI observes state
@Composable
fun JobDetailScreen(...) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    
    when {
        state.isLoading -> CircularProgressIndicator()
        state.error != null -> ErrorMessage(state.error)
        state.job != null -> JobContent(state.job)
    }
    
    if (state.justStatusChanged) {
        Button(onClick = { viewModel.onDepartNextJob() }) {
            Text("Depart Next Job")
        }
    }
}
```

### State Flow Composition

```
UI Event (user click)
   ↓
ViewModel Function (onPrimaryAction)
   ↓
Update UI State (_uiState.update)
   ↓
Repository Call (jobRepository.updateJobStatus)
   ↓
Database Update (jobDao.updateJob)
   ↓
Flow emission to ViewModel
   ↓
State preserved (justStatusChanged not overwritten)
   ↓
UI observes new state
   ↓
Compose recomposition
   ↓
Screen updates (button appears, loading stops)
```

**Key Pattern**:
- Unidirectional data flow (one direction only)
- State updates immutable (always copy())
- UI reactive (automatic recomposition)
- No direct UI ↔ Database access

---

## Navigation Architecture

### Navigation Graph

```kotlin
@Composable
fun MercuryNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                onViewJobs = { navController.navigate(Screen.JobList.route) },
                onDayClosing = { navController.navigate(Screen.DayClosing.route) }
            )
        }
        
        composable(
            route = Screen.JobDetail.route,
            arguments = listOf(navArgument("jobId") { type = NavType.StringType })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: return@composable
            JobDetailScreen(
                jobId = jobId,
                navController = navController,
                onNavigateToCamera = { jId, lId, requirePhoto ->
                    navController.navigate(Screen.Camera.createRoute(jId, lId, requirePhoto))
                }
            )
        }
        
        composable(Screen.Camera.route, ...) {
            CameraScreen(
                onPhotoCaptured = { photoUrl ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("photoUrl", photoUrl)
                    navController.popBackStack()
                }
            )
        }
    }
}
```

### Screen Definitions

```kotlin
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object JobList : Screen("joblist")
    object JobDetail : Screen("jobdetail/{jobId}") {
        fun createRoute(jobId: String) = "jobdetail/$jobId"
    }
    object Camera : Screen("camera/{jobId}/{logId}/{requirePhoto}") {
        fun createRoute(jobId: String, logId: String, requirePhoto: Boolean) =
            "camera/$jobId/$logId/$requirePhoto"
    }
    object DayClosing : Screen("dayclosing")
}
```

**Navigation Flow**:

```
Login
  ↓ (on success)
Home
  ├─→ JobList
  │    ↓ (click job)
  │   JobDetail
  │    ├─→ Camera (on photo required)
  │    │    ↓ (back)
  │    │   JobDetail (continues)
  │    │
  │    └─→ (depart next job)
  │        JobDetail (new job)
  │
  └─→ DayClosing
```

**Key Patterns**:
- Type-safe route construction
- Back stack management with `popUpTo`
- Saved state handle for photo transfer
- Deep linking support (future)

---

## Design Patterns

### 1. MVVM (Model-View-ViewModel)

- **Model**: Domain + Data layers
- **View**: Compose screens
- **ViewModel**: State holder (ViewModel class)

**Benefits**: Clear separation, testable, lifecycle-aware

### 2. Repository Pattern

- Abstraction over data sources
- Single source of truth (local DB)
- Offline-first strategy

### 3. Dependency Injection (DI)

- Hilt framework
- Constructor injection
- Compile-time validation

### 4. Unidirectional Data Flow (UDF)

```
Event → Action → State Update → UI Render
```

### 5. Single Responsibility

Each component has one reason to change:
- DAO: DB queries only
- Repository: API/DB orchestration only
- ViewModel: State management only
- Screen: UI rendering only

### 6. Flow/StateFlow for Reactivity

- Automatic UI updates
- No manual observers
- Type-safe state emissions

### 7. Sealed Classes for Navigation

- Type-safe route definitions
- Exhaustive when expressions

### 8. Entity → DTO → Domain Model Transformation

```
JobDto (API) → JobEntity (DB) → Job (Domain) → JobDetailUiState (UI)
```

---

## Key Decisions & Rationale

### 1. Room Database as Source of Truth

**Decision**: Local database is authoritative, API calls update the cache

**Rationale**:
- Offline-first capability
- Reduced network calls
- Consistent UI state
- Better UX during poor connectivity

**Trade-off**: Potential data staleness (mitigated by cache timeout)

---

### 2. StateFlow for UI State

**Decision**: Use `MutableStateFlow` instead of LiveData

**Rationale**:
- Kotlin-first API
- Coroutine-compatible
- Better composition with other Flows
- More powerful operators

**Trade-off**: Requires coroutine knowledge

---

### 3. Suspend Functions + Coroutines

**Decision**: Use `suspend` functions instead of callbacks

**Rationale**:
- Sequential code easier to read
- Exception handling with try-catch
- Lifecycle-aware with `viewModelScope`
- Cancellation support

**Trade-off**: Kotlin-only (no Java interop needed here)

---

### 4. DTOs Separate from Domain Models

**Decision**: Keep API contracts (DTOs) separate from domain logic (Models)

**Rationale**:
- API can change without affecting domain
- Clear layering
- Testable transformations
- Flexibility in backend changes

**Trade-off**: Extra mapping code (small cost)

---

### 5. Hilt for DI

**Decision**: Use Hilt over manual Dagger or service locators

**Rationale**:
- Android-specific DSL
- Automatic lifecycle management
- Less boilerplate
- Compile-time safety

**Trade-off**: Learning curve for Hilt syntax

---

### 6. Offline-First GPS + Location Picker

**Decision**: Capture GPS immediately when action triggered, show picker later

**Rationale**:
- More accurate location (less time delay)
- Better UX (quick capture vs. delayed dialog)
- Matches user expectations

**Implementation**: Pending state in ViewModel preserves captured coordinates

---

### 7. Photo URL in Status Log

**Decision**: Store photo URL returned from server, not client-managed

**Rationale**:
- Server controls storage/CDN
- Decouples client from storage
- Easier to migrate storage provider
- Reduced local storage

**Trade-off**: Depends on reliable server implementation

---

### 8. Justification Flag for Button Visibility

**Decision**: Track "just completed" with `justStatusChanged` flag

**Rationale**:
- Distinguishes "just completed" from "viewing completed"
- Prevents showing button when user revisits already-completed job
- Matches user expectation

**Implementation Challenge**: Preserving flag through Flow emissions (solved by checking jobId change)

---

## Performance Considerations

### Memory

- **StateFlow subscriptions**: Auto-cleanup when ViewModel destroyed
- **Database queries**: Room handles connection pooling
- **Large lists**: Lazy loading with pagination (future improvement)

### Network

- **Timeout**: 30 seconds (configurable)
- **Retry**: Exponential backoff for transient errors
- **Caching**: 1-hour TTL (configurable per endpoint)
- **Image compression**: Photos compressed before upload (PhotoHelper)

### Database

- **Indexes**: On frequently queried columns (messengerId, jobId, date, status)
- **Query optimization**: Minimal JOIN queries
- **Transaction batching**: Multiple updates in single transaction

---

## Security

### Data Protection

- **Token storage**: EncryptedSharedPreferences (AES-256)
- **Database**: Encrypted SQLite (SQLCipher - future)
- **Network**: HTTPS/TLS required
- **Certificate pinning**: Optional (future)

### Permission Handling

- **GPS**: Runtime permission request with user explanation
- **Camera**: Runtime permission request
- **Photo storage**: Scoped storage (Android 10+)

### Input Validation

- **API responses**: Validate all received data
- **Location bounds**: Ensure realistic coordinates
- **Photo size**: Limit to 5MB

---

## Testing Strategy

### Unit Tests
- ViewModel logic with mock Repository
- Repository with mock API and Database
- Domain models (plain Kotlin)

### Integration Tests
- ViewModel + real Database
- Repository + real Database
- Mock API responses

### UI Tests
- Screen composition
- User interaction flows
- Navigation

### Manual Tests
- Complete job workflows
- Network error scenarios
- Offline mode
- Photo capture

---

## Scalability & Future Improvements

### Short Term
- [ ] Implement proper database migrations
- [ ] Add request logging interceptor
- [ ] Implement token refresh mechanism
- [ ] Add pagination for large job lists

### Medium Term
- [ ] Add WorkManager for background sync
- [ ] Implement SQLCipher for database encryption
- [ ] Add error tracking (Crashlytics)
- [ ] Multi-language support

### Long Term
- [ ] WebSocket for real-time updates
- [ ] Video capture (in addition to photos)
- [ ] Offline job reassignment
- [ ] Advanced analytics

---

## Related Documents

- **API_INTEGRATION.md** - API contract and integration details
- **DEPLOYMENT.md** - Build and release procedures (future)
- **DEVELOPER_GUIDE.md** - Setup and development guide (future)

---

**Document Version**: 1.0  
**Last Updated**: 2026-04-13  
**Status**: Complete and Ready for Team Review
