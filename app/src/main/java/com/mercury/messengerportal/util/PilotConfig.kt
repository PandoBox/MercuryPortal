package com.mercury.messengerportal.util

import android.location.Location

/**
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║                     PILOT CONFIG — REMOVE BEFORE PROD               ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * Temporary overrides used during the pilot/prototype phase.
 * All flags are gated by [PILOT_MODE_ENABLED] so they can be disabled with
 * a single toggle. Delete this file entirely before going to production.
 */
object PilotConfig {

    /** Master switch — set to false to run without any pilot overrides. */
    const val PILOT_MODE_ENABLED = true

    // ── [Pilot-config] Fake Location ─────────────────────────────────────────
    // Spoofs GPS to "BTS Visionary Park" so the emulator/test device behaves
    // as if the messenger is physically at that location.
    // Remove when real device testing is possible.

    /** If true, all LocationHelper.getCurrentLocation() calls return [FAKE_LOCATION]. */
    val FAKE_LOCATION_ENABLED = PILOT_MODE_ENABLED && true

    /** BTS Visionary Park, Bangkok (lat, lng) */
    private const val FAKE_LAT = 13.7650
    private const val FAKE_LNG = 100.6381

    val FAKE_LOCATION: Location = Location("pilot-fake").apply {
        latitude = FAKE_LAT
        longitude = FAKE_LNG
        accuracy = 5f
        time = System.currentTimeMillis()
    }

    // ── [Pilot-config] Arrival Location Picker ────────────────────────────────
    // When the messenger taps "Mark Arrived", show a dialog to choose between
    // (a) their current GPS position or (b) the job's expected destination.
    // In production the GPS stamp should be automatic — this picker is a workaround
    // for prototype testing where the device isn't physically at the job site.

    /** If true, show a location-picker dialog on ARRIVED transitions. */
    val ARRIVAL_LOCATION_PICKER_ENABLED = PILOT_MODE_ENABLED && true
}
