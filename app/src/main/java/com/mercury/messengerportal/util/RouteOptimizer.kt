package com.mercury.messengerportal.util

import com.mercury.messengerportal.domain.model.Job
import com.mercury.messengerportal.domain.model.JobStatus
import kotlin.math.*

/**
 * On-device route optimization using a greedy nearest-neighbor algorithm.
 *
 * Given a current location and a list of jobs, suggests an optimal delivery order
 * by always visiting the closest unvisited job next. This is a fast, offline-friendly
 * heuristic that produces reasonable results for 5-15 stops.
 *
 * **Algorithm:**
 * 1. Start at the current location
 * 2. Find the closest unvisited job (by Haversine straight-line distance)
 * 3. Add it to the suggested order and mark as visited
 * 4. Repeat from the job's location until all jobs are visited
 * 5. Return the suggested job list (in optimized order)
 *
 * **Notes:**
 * - Only non-terminal jobs (ASSIGNED, DEPARTED, ARRIVED) are reordered
 * - Terminal jobs (COMPLETED, DELAYED) are excluded from optimization
 * - If there are fewer than 2 non-terminal jobs, the input list is returned unchanged
 */
object RouteOptimizer {

    /**
     * Suggests an optimized job delivery order using nearest-neighbor heuristic.
     *
     * @param currentLat Current latitude (e.g., messenger's current GPS location)
     * @param currentLng Current longitude
     * @param jobs List of all jobs for the day
     * @return Jobs ordered by nearest-neighbor optimization (only non-terminal jobs are reordered)
     */
    fun suggestOrder(currentLat: Double, currentLng: Double, jobs: List<Job>): List<Job> {
        // Separate terminal (completed/delayed) and non-terminal (active) jobs
        val terminalJobs = jobs.filter { it.status.isTerminal() }
        val activeJobs = jobs.filter { !it.status.isTerminal() }

        // If fewer than 2 active jobs, no optimization needed
        if (activeJobs.size < 2) {
            // Return with terminals at the end
            return activeJobs + terminalJobs
        }

        // Greedy nearest-neighbor: build optimized order by always picking closest unvisited job
        val optimized = mutableListOf<Job>()
        val visited = mutableSetOf<String>()
        var currentLat = currentLat
        var currentLng = currentLng

        while (optimized.size < activeJobs.size) {
            // Find closest unvisited job
            val nextJob = activeJobs
                .filter { it.id !in visited }
                .minByOrNull { haversineDistance(currentLat, currentLng, it.latitude, it.longitude) }
                ?: break // Should not happen if count is correct, but safety guard

            optimized.add(nextJob)
            visited.add(nextJob.id)
            currentLat = nextJob.latitude
            currentLng = nextJob.longitude
        }

        // Return optimized active jobs + terminal jobs at the end
        return optimized + terminalJobs
    }

    /**
     * Calculates the great-circle distance between two lat/lng points using the Haversine formula.
     *
     * @param lat1 Starting latitude (degrees)
     * @param lng1 Starting longitude (degrees)
     * @param lat2 Ending latitude (degrees)
     * @param lng2 Ending longitude (degrees)
     * @return Distance in meters
     */
    fun haversineDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val EARTH_RADIUS_METERS = 6371000.0

        val dLat = (lat2 - lat1).toRadians()
        val dLng = (lng2 - lng1).toRadians()

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1.toRadians()) * cos(lat2.toRadians()) *
                sin(dLng / 2) * sin(dLng / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    /**
     * Converts degrees to radians.
     */
    private fun Double.toRadians(): Double = this * PI / 180.0
}
