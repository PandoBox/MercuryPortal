package com.mercury.messengerportal.data.remote

import com.mercury.messengerportal.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * REST API contract. All endpoints are stubbed — replace BASE_URL in BuildConfig
 * and add real server implementation when ready.
 */
interface ApiService {

    // ── Auth ─────────────────────────────────────────────────────────────────

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // ── Jobs ──────────────────────────────────────────────────────────────────

    @GET("jobs")
    suspend fun getAssignedJobs(
        @Query("messengerId") messengerId: String,
        @Query("date") date: String          // yyyy-MM-dd
    ): Response<List<JobDto>>

    @PATCH("jobs/{jobId}/status")
    suspend fun updateJobStatus(
        @Path("jobId") jobId: String,
        @Body request: StatusUpdateRequest
    ): Response<StatusUpdateResponse>

    @POST("jobs/{jobId}/reassign-request")
    suspend fun requestReassign(
        @Path("jobId") jobId: String,
        @Body request: ReassignRequest
    ): Response<Unit>

    @PATCH("jobs/reorder")
    suspend fun reorderJobs(@Body request: ReorderRequest): Response<Unit>

    // ── Day Log ───────────────────────────────────────────────────────────────

    @POST("day-log/clock-in")
    suspend fun clockIn(@Body request: ClockEventRequest): Response<ClockEventResponse>

    @POST("day-log/clock-out")
    suspend fun clockOut(@Body request: ClockEventRequest): Response<ClockEventResponse>

    // ── Photo Upload ──────────────────────────────────────────────────────────

    @Multipart
    @POST("photos/upload")
    suspend fun uploadPhoto(
        @retrofit2.http.Part photo: okhttp3.MultipartBody.Part,
        @retrofit2.http.Part("jobId") jobId: okhttp3.RequestBody,
        @retrofit2.http.Part("logId") logId: okhttp3.RequestBody
    ): Response<PhotoUploadResponse>
}
