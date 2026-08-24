package com.example.quizapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.quizapp.data.SessionManager
import com.example.quizapp.ui.screens.HistoryScreen
import com.example.quizapp.ui.screens.LoginScreen
import com.example.quizapp.ui.screens.HomeScreen
import com.example.quizapp.ui.screens.QuizScreen
import com.example.quizapp.ui.screens.ResultScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController, sessionManager)
        }
        composable("home") {
            HomeScreen(navController, sessionManager)
        }
        composable(
            "quiz/{category}/{difficulty}",
            arguments = listOf(
                navArgument("category") { type = NavType.StringType },
                navArgument("difficulty") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            val difficulty = backStackEntry.arguments?.getString("difficulty") ?: ""
            QuizScreen(navController, category, difficulty)
        }
        composable(
            "result/{score}/{total}/{category}/{difficulty}",
            arguments = listOf(
                navArgument("score") { type = NavType.IntType },
                navArgument("total") { type = NavType.IntType },
                navArgument("category") { type = NavType.StringType },
                navArgument("difficulty") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val score = backStackEntry.arguments?.getInt("score") ?: 0
            val total = backStackEntry.arguments?.getInt("total") ?: 0
            val category = backStackEntry.arguments?.getString("category") ?: ""
            val difficulty = backStackEntry.arguments?.getString("difficulty") ?: ""
            ResultScreen(navController, score, total, category, difficulty)
        }
        composable("history") {
            HistoryScreen(navController, sessionManager)
        }
    }
}