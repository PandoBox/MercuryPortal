package com.mercury.messengerportal.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mercury.messengerportal.ui.camera.CameraScreen
import com.mercury.messengerportal.ui.dashboard.PerformanceDashboardScreen
import com.mercury.messengerportal.ui.dayclosing.DayClosingScreen
import com.mercury.messengerportal.ui.home.HomeScreen
import com.mercury.messengerportal.ui.jobdetail.JobDetailScreen
import com.mercury.messengerportal.ui.joblist.JobListScreen
import com.mercury.messengerportal.ui.login.LoginScreen

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
                onDayClosing = { navController.navigate(Screen.DayClosing.route) },
                onDepartJob = { jobId -> navController.navigate(Screen.JobDetail.createRoute(jobId)) },
                onOpenDashboard = { navController.navigate(Screen.Dashboard.route) }
            )
        }

        composable(Screen.JobList.route) {
            JobListScreen(
                onJobClick = { jobId ->
                    navController.navigate(Screen.JobDetail.createRoute(jobId))
                },
                onBack = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route) { inclusive = true } } }
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
                },
                onBack = { navController.navigate(Screen.JobList.route) { popUpTo(Screen.JobList.route) { inclusive = true } } }
            )
        }

        composable(
            route = Screen.Camera.route,
            arguments = listOf(
                navArgument("jobId") { type = NavType.StringType },
                navArgument("logId") { type = NavType.StringType },
                navArgument("requirePhoto") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: return@composable
            val logId = backStackEntry.arguments?.getString("logId") ?: return@composable
            CameraScreen(
                jobId = jobId,
                logId = logId,
                onPhotoCaptured = { photoUrl ->
                    // Pass the photoUrl (or null if skipped) back to JobDetailScreen
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("photoUrl", photoUrl)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(Screen.DayClosing.route) {
            DayClosingScreen(
                onDayClosed = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Dashboard.route) {
            PerformanceDashboardScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
