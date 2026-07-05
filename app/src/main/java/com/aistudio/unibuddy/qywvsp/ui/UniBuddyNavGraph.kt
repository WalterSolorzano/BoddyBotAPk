package com.aistudio.unibuddy.qywvsp.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aistudio.unibuddy.qywvsp.ui.screens.*

@Composable
fun UniBuddyNavGraph(
    navController: NavHostController,
    viewModel: UniBuddyViewModel,
    modifier: Modifier = Modifier,
    startDestination: String = "onboarding"
) {
    NavHost(
        navController = navController,
        startDestination = if (viewModel.currentUser != null) "dashboard" else "login",
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable("login") {
            AuthScreen(
                viewModel = viewModel,
                onAuthSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("onboarding") {
            OnboardingScreen(
                viewModel = viewModel,
                onFinished = {
                    navController.navigate("dashboard") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToDetails = { subjectId ->
                    navController.navigate("subject_detail/$subjectId")
                },
                onNavigateToGrades = { subjectId ->
                    navController.navigate("subject_grades/$subjectId")
                },
                onConfigureRoute = {
                    navController.navigate("settings")
                },
                onNavigateToFocus = {
                    navController.navigate("focus")
                },
                onNavigateToStats = {
                    navController.navigate("stats")
                }
            )
        }

        composable(
            route = "subject_detail/{subjectId}",
            arguments = listOf(navArgument("subjectId") { type = NavType.IntType })
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getInt("subjectId") ?: return@composable
            // SubjectDetailsScreen(
            //     viewModel = viewModel,
            //     subjectId = subjectId,
            //     onBack = { navController.popBackStack() },
            //     onNavigateToGrades = { id -> navController.navigate("subject_grades/$id") }
            // )
        }

        composable(
            route = "subject_grades/{subjectId}",
            arguments = listOf(navArgument("subjectId") { type = NavType.IntType })
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getInt("subjectId") ?: return@composable
            // SubjectGradesScreen(
            //     viewModel = viewModel,
            //     subjectId = subjectId,
            //     onBack = { navController.popBackStack() }
            // )
        }

        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateToPensum = { navController.navigate("pensum") }
            )
        }

        composable("focus") {
            // FocusModeScreen(
            //     viewModel = viewModel,
            //     onBack = { navController.popBackStack() }
            // )
        }
    }
}
