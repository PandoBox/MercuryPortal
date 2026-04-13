# API Integration Layer Documentation

**Mercury Messenger Portal - Backend Integration Guide**

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Authentication](#authentication)
4. [API Endpoints](#api-endpoints)
5. [Data Models](#data-models)
6. [Error Handling](#error-handling)
7. [Implementation Checklist](#implementation-checklist)
8. [Testing & Validation](#testing--validation)

---

## Overview

The Mercury Messenger Portal communicates with the backend via REST API using Retrofit2. The current implementation includes stub endpoints that simulate real backend responses. This document outlines the contract and integration requirements for connecting to a real backend system.

### Current State
- **Base Retrofit interface**: `ApiService.kt`
- **Data Transfer Objects**: `Dtos.kt`
- **Repository layer**: `JobRepository.kt`, `AuthRepository.kt`
- **HTTP client**: Configured in `NetworkModule.kt` with Hilt dependency injection

### Integration Scope
This document covers:
- All API endpoints required
- Request/response payloads
- Error handling strategies
- Authentication flow
- Data synchronization
- Photo upload handling

---

## Architecture

### Layered Architecture

```
UI Layer (Compose Screens)
    ↓
ViewModel (State Management)
    ↓
Repository (Data Abstraction)
    ├── Local (Room Database)
    └── Remote (ApiService/Retrofit)
```

### Data Flow

1. **UI Action** → ViewModel function call
2. **ViewModel** → calls Repository method
3. **Repository** → decides: cache hit vs. API call
4. **API Call** → Retrofit + ApiService
5. **Response** → parsed into DTO → mapped to Domain model
6. **Local Cache** → saved to Room database (single source of truth)
7. **StateFlow** → emitted to UI for reactive updates

### Key Files

| File | Purpose |
|------|---------|
| `ApiService.kt` | Retrofit interface defining all endpoints |
| `Dtos.kt` | Data Transfer Objects for API serialization |
| `JobRepository.kt` | Repository pattern: API + Database abstraction |
| `AuthRepository.kt` | Authentication token and session management |
| `NetworkModule.kt` | Hilt configuration for HTTP client |
| `AppDatabase.kt` | Room database setup |

---

## Authentication

### Login Flow

1. User enters `employeeId` and `password` on LoginScreen
2. `AuthRepository.login()` calls `apiService.login()`
3. Backend validates credentials and returns JWT token
4. Token stored securely in local storage (encrypted SharedPreferences)
5. All subsequent requests include token in `Authorization: Bearer <token>` header

### Token Management

**Current Implementation:**
```kotlin
// In NetworkModule.kt - configure Retrofit with interceptor
val httpClient = OkHttpClient.Builder()
    .addInterceptor(AuthInterceptor(tokenRepository))  // Add this
    .build()
```

**AuthInterceptor should:**
- Extract token from secure storage
- Add `Authorization: Bearer <token>` header to all requests
- Catch 401 responses and trigger re-login flow

### Implementation Requirements

- [ ] Implement `AuthInterceptor` to add token to headers
- [ ] Store token in encrypted SharedPreferences (EncryptedSharedPreferences)
- [ ] Implement token refresh mechanism (if backend uses refresh tokens)
- [ ] Handle token expiration and redirect to login
- [ ] Clear token on logout

---

## API Endpoints

### Base URL Configuration

```kotlin
// In build.gradle.kts or BuildConfig
const val API_BASE_URL = "https://api.mercury-backend.com/v1/"
```

**Environment-specific URLs:**
```
Development: https://dev-api.mercury-backend.com/v1/
Staging:     https://staging-api.mercury-backend.com/v1/
Production:  https://api.mercury-backend.com/v1/
```

### Endpoint Details

#### 1. Authentication

##### POST `/auth/login`

**Request:**
```json
{
  "employeeId": "EMP001",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "messengerId": "MSGR001",
  "messengerName": "Luffy D. Monkey",
  "employeeId": "EMP001",
  "phone": "+66812345678"
}
```

**Error Responses:**
- `401 Unauthorized`: Invalid credentials
- `400 Bad Request`: Missing required fields

---

#### 2. Jobs

##### GET `/jobs`

Fetch assigned jobs for a messenger on a specific date.

**Query Parameters:**
```
messengerId: String  (required) - Messenger ID
date: String        (required) - Format: yyyy-MM-dd
```

**Request Example:**
```
GET /jobs?messengerId=MSGR001&date=2026-04-13
```

**Response (200 OK):**
```json
[
  {
    "id": "JOB001",
    "title": "Deliver Package to Bangkok",
    "type": "DELIVERY",
    "senderName": "Company A",
    "senderPhone": "+66812345678",
    "receiverName": "John Doe",
    "receiverPhone": "+66987654321",
    "locationName": "Silom, Bangkok",
    "locationAddress": "123 Silom Rd, Bangkok, Thailand",
    "latitude": 13.7247,
    "longitude": 100.5317,
    "sequenceOrder": 1,
    "status": "ASSIGNED",
    "assignedAt": 1681382400000,
    "notes": "Deliver to reception, signature required",
    "messengerId": "MSGR001"
  }
]
```

**Caching Strategy:**
- Response cached in Room database
- Refresh on app startup and manual pull-to-refresh
- Cache timeout: 1 hour (configurable)

---

##### PATCH `/jobs/{jobId}/status`

Update job status with location and timestamp.

**Path Parameters:**
```
jobId: String - Job ID to update
```

**Request Body:**
```json
{
  "logId": "LOG_UUID_001",
  "status": "COMPLETED",
  "latitude": 13.7247,
  "longitude": 100.5317,
  "deviceTimestamp": 1681382400000,
  "photoUrl": "https://storage.mercury.com/photos/JOB001_LOG001.jpg"
}
```

**Status Values:**
```
ASSIGNED   → Initial state when assigned
DEPARTED   → Messenger left to job location
ARRIVED    → Messenger reached location
COMPLETED  → Job finished
DELAYED    → Job delayed with reason
```

**Response (200 OK):**
```json
{
  "logId": "LOG_UUID_001",
  "serverTimestamp": 1681382400500
}
```

**Implementation Notes:**
- `logId` should be a unique UUID per status change (generated client-side)
- `deviceTimestamp` is milliseconds since epoch
- `photoUrl` is nullable (photo not required for all transitions)
- Server returns `serverTimestamp` for synchronization

---

##### PATCH `/jobs/reorder`

Batch update job sequence order for drag-and-drop reordering.

**Request Body:**
```json
{
  "jobs": [
    {
      "jobId": "JOB001",
      "sequenceOrder": 2
    },
    {
      "jobId": "JOB002",
      "sequenceOrder": 1
    }
  ]
}
```

**Response (200 OK):**
```json
{}
```

**Implementation Notes:**
- Called when user drags jobs in JobListScreen
- Updates sequence order for active jobs only (not COMPLETED/DELAYED)
- Should be debounced to avoid excessive API calls

---

##### POST `/jobs/{jobId}/reassign-request`

Request to reassign job back to HQ.

**Path Parameters:**
```
jobId: String - Job ID to reassign
```

**Request Body:**
```json
{
  "note": "Unable to find receiver at location"
}
```

**Response (200 OK):**
```json
{}
```

**Implementation Notes:**
- Called from Job Detail screen
- Note is optional but recommended
- Backend should notify supervisor/dispatcher

---

#### 3. Day Log / Clock Events

##### POST `/day-log/clock-in`

Log messenger clock-in with location.

**Request Body:**
```json
{
  "messengerId": "MSGR001",
  "latitude": 13.7247,
  "longitude": 100.5317,
  "deviceTimestamp": 1681382400000
}
```

**Response (200 OK):**
```json
{
  "serverTimestamp": 1681382400500
}
```

**Implementation Notes:**
- Called from Home screen "Clock In" button
- Location captured at time of click
- Creates new DayLog record for the messenger

---

##### POST `/day-log/clock-out`

Log messenger clock-out with location.

**Request Body:**
```json
{
  "messengerId": "MSGR001",
  "latitude": 13.7247,
  "longitude": 100.5317,
  "deviceTimestamp": 1681382400000
}
```

**Response (200 OK):**
```json
{
  "serverTimestamp": 1681382400500
}
```

**Implementation Notes:**
- Called from Day Summary screen after location picker
- Marks end of work day
- All jobs not COMPLETED/DELAYED by clock-out should be flagged as issues

---

#### 4. Photo Upload

##### POST `/photos/upload` (Multipart)

Upload job completion photo.

**Request Parameters:**
```
photo: File       - Image file (JPEG, PNG)
jobId: String     - Job ID
logId: String     - Status log ID
```

**Response (200 OK):**
```json
{
  "photoUrl": "https://storage.mercury.com/photos/JOB001_LOG001_photo.jpg"
}
```

**Implementation Notes:**
- Called before status update if photo required
- Max file size: 5MB (configurable)
- Image should be compressed before upload (handled in PhotoHelper.kt)
- URL returned is used in subsequent status update request

---

## Data Models

### Domain Models (App Layer)

Located in `domain/model/` - these are the canonical data structures used throughout the app.

```kotlin
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
    val messengerId: String,
    val messengerRemark: String = ""
)

enum class JobStatus {
    ASSIGNED, DEPARTED, ARRIVED, COMPLETED, DELAYED
}

data class DayLog(
    val id: String,
    val messengerId: String,
    val date: String,          // yyyy-MM-dd
    val clockInTime: Long?,    // milliseconds
    val clockOutTime: Long?,   // milliseconds
    val clockInLat: Double?,
    val clockInLng: Double?,
    val clockOutLat: Double?,
    val clockOutLng: Double?
)

data class JobStatusLog(
    val id: String,            // UUID
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

### DTO to Domain Mapping

**In Repository layer, convert DTOs to Domain models:**

```kotlin
// JobRepository.kt
private fun JobDto.toDomain(): Job {
    return Job(
        id = id,
        title = title,
        type = JobType.valueOf(type),
        senderName = senderName,
        // ... other fields
        status = JobStatus.valueOf(status)
    )
}
```

---

## Error Handling

### HTTP Error Codes

| Code | Meaning | Action |
|------|---------|--------|
| 200 | Success | Process response normally |
| 400 | Bad Request | Log error, show user-friendly message |
| 401 | Unauthorized | Clear token, redirect to login |
| 403 | Forbidden | Show "permission denied" error |
| 404 | Not Found | Show "resource not found" error |
| 408 | Request Timeout | Retry with exponential backoff |
| 500 | Server Error | Log, show "server error" message, retry |
| 503 | Service Unavailable | Show "service unavailable" message |

### Implementation in Repository

```kotlin
suspend fun updateJobStatus(job: Job, newStatus: JobStatus, ...): Result<Unit> {
    return try {
        val response = apiService.updateJobStatus(job.id, request)
        when {
            response.isSuccessful -> {
                // Update local cache
                jobDao.updateJob(jobEntity.copy(status = newStatus.name))
                Result.success(Unit)
            }
            response.code() == 401 -> {
                // Token expired
                authRepository.logout()
                Result.failure(AuthException("Session expired"))
            }
            response.code() == 400 -> {
                Result.failure(BadRequestException(response.errorBody()?.string()))
            }
            else -> {
                Result.failure(ApiException("Server error: ${response.code()}"))
            }
        }
    } catch (e: IOException) {
        // Network error - use offline-first approach
        Result.failure(NetworkException("No internet connection"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### Retry Strategy

**Implement exponential backoff for transient failures:**

```kotlin
private suspend fun <T> retryOnNetworkError(
    maxRetries: Int = 3,
    delay: Long = 1000,
    block: suspend () -> Response<T>
): Response<T> {
    var currentDelay = delay
    repeat(maxRetries) { attempt ->
        try {
            return block()
        } catch (e: IOException) {
            if (attempt == maxRetries - 1) throw e
            delay(currentDelay)
            currentDelay *= 2
        }
    }
    throw IOException("Max retries exceeded")
}
```

### Offline-First Approach

The app uses Room database as single source of truth:

1. All operations read from local database first
2. API calls update local cache
3. If offline, operations queue in local database
4. When online, sync pending changes to server
5. No forced network call on every operation

---

## Implementation Checklist

### Phase 1: Setup & Configuration

- [ ] Configure API base URL in `BuildConfig` or environment config
- [ ] Set up SSL certificate pinning (if required)
- [ ] Create `AuthInterceptor` for token management
- [ ] Implement token storage in EncryptedSharedPreferences
- [ ] Configure Retrofit timeout values (connect: 30s, read: 30s, write: 30s)
- [ ] Add logging interceptor (OkHttp logging) for development
- [ ] Create environment-specific configuration (dev/staging/prod)

### Phase 2: Authentication

- [ ] Implement real login endpoint
- [ ] Add token refresh mechanism
- [ ] Test 401 handling and re-login flow
- [ ] Implement logout clearing token
- [ ] Add session timeout handling

### Phase 3: Core Endpoints

- [ ] Implement GET `/jobs` endpoint
- [ ] Implement PATCH `/jobs/{jobId}/status` endpoint
- [ ] Add photo upload with multipart
- [ ] Test job fetching and caching
- [ ] Test status updates with various scenarios

### Phase 4: Additional Endpoints

- [ ] Implement PATCH `/jobs/reorder`
- [ ] Implement POST `/jobs/{jobId}/reassign-request`
- [ ] Implement clock in/out endpoints
- [ ] Test reassign and reorder flows

### Phase 5: Error Handling & Resilience

- [ ] Add retry logic with exponential backoff
- [ ] Implement offline-first queuing system
- [ ] Add network state monitoring
- [ ] Test error scenarios (401, 500, timeout, no network)
- [ ] Implement user-friendly error messages

### Phase 6: Optimization & Monitoring

- [ ] Add request/response logging
- [ ] Implement API call metrics
- [ ] Test under poor network conditions
- [ ] Optimize image upload (compression)
- [ ] Set up error tracking (Crashlytics, Sentry)

---

## Testing & Validation

### Unit Tests

Test repository methods with mock API responses:

```kotlin
@Test
fun testUpdateJobStatusSuccess() = runTest {
    // Mock API response
    val mockResponse = mockk<Response<StatusUpdateResponse>>()
    every { mockResponse.isSuccessful } returns true
    every { mockResponse.body() } returns StatusUpdateResponse("LOG_001", 1681382400500)
    
    coEvery { apiService.updateJobStatus(any(), any()) } returns mockResponse
    
    val result = jobRepository.updateJobStatus(job, JobStatus.COMPLETED, lat, lng, null, null, true)
    
    assertTrue(result.isSuccess)
    // Verify database was updated
    coVerify { jobDao.updateJob(any()) }
}
```

### Integration Tests

Test with real database and mock API:

```kotlin
@Test
fun testJobListLoadingAndCaching() = runTest {
    // Mock API return
    coEvery { apiService.getAssignedJobs(any(), any()) } returns Response.success(jobDtos)
    
    // First load
    val jobs1 = jobRepository.observeJobs(messengerId).first()
    assertEquals(3, jobs1.size)
    
    // Second load (should use cache)
    val jobs2 = jobRepository.observeJobs(messengerId).first()
    assertEquals(3, jobs2.size)
    
    // Verify API called once (not twice)
    coVerify(exactly = 1) { apiService.getAssignedJobs(any(), any()) }
}
```

### End-to-End Tests

Test complete user workflows:

1. **Clock In Flow**: Clock in → Jobs loaded → show job list
2. **Complete Job Flow**: Open job → Click complete → Select location → Confirm → Update status
3. **Clock Out Flow**: All jobs completed → Click clock out → Select location → Confirm
4. **Error Recovery**: Network error → Retry → Success

### Manual Testing Checklist

- [ ] Login with valid/invalid credentials
- [ ] Load jobs for a day
- [ ] Complete a job without photo
- [ ] Complete a job with photo
- [ ] Delay a job with reason
- [ ] Reassign a job
- [ ] Reorder jobs
- [ ] Clock in/out with location
- [ ] Test offline mode (disable network)
- [ ] Test slow network (throttle connection)
- [ ] Test token expiration (simulate 401)
- [ ] Test server error (500)

---

## Notes for Backend Team

### Expected Response Times

- **Jobs fetch**: < 2 seconds
- **Status update**: < 3 seconds
- **Photo upload** (5MB): < 10 seconds
- **Clock in/out**: < 2 seconds

### Data Consistency

- Server should validate all location coordinates are within reasonable bounds
- Status transitions should follow: ASSIGNED → DEPARTED → ARRIVED → COMPLETED
- DELAYED jobs bypass DEPARTED/ARRIVED
- Sequence order should be unique within a messenger's job list for a day

### Timestamps

- All timestamps in request/response are **milliseconds since epoch** (UTC)
- Client generates `deviceTimestamp`, server returns `serverTimestamp` for drift correction
- Assume up to 5 minutes difference between client and server clocks

### Photo URLs

- Returned photo URLs must be accessible without authentication
- URLs should be permanent and not expire
- Include photo creation date for analytics

---

## Related Files

- **API Configuration**: `di/NetworkModule.kt`
- **API Interface**: `data/remote/ApiService.kt`
- **DTOs**: `data/remote/dto/Dtos.kt`
- **Repository**: `data/repository/JobRepository.kt`, `data/repository/AuthRepository.kt`
- **Database**: `data/local/AppDatabase.kt`, `data/local/dao/*.kt`
- **Models**: `domain/model/*.kt`

---

**Document Version**: 1.0  
**Last Updated**: 2026-04-13  
**Status**: Ready for Backend Integration
