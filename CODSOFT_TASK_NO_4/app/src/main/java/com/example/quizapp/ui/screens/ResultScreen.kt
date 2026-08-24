package com.example.quizapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun ResultScreen(navController: NavController, score: Int, total: Int, category: String, difficulty: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Your Score: $score / $total")

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            navController.navigate("quiz/$category/$difficulty")
        }) {
            Text("Play Again")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { navController.navigate("home") }) {
            Text("Back to Home")
        }
    }
}