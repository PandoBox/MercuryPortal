package com.mercury.messengerportal.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Home : Screen("home")
    data object JobList : Screen("job_list")
    data object JobDetail : Screen("job_detail/{jobId}") {
        fun createRoute(jobId: String) = "job_detail/$jobId"
    }
    data object Camera : Screen("camera/{jobId}/{logId}/{requirePhoto}") {
        fun createRoute(jobId: String, logId: String, requirePhoto: Boolean) =
            "camera/$jobId/$logId/$requirePhoto"
    }
    data object DayClosing : Screen("day_closing")
}
