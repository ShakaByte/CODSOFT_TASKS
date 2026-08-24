package com.example.quizapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.quizapp.data.SessionManager
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import kotlinx.coroutines.launch


@Composable
fun HomeScreen(navController: NavController, sessionManager: SessionManager) {
    val categories = listOf("Random", "Electronics", "Computer Science", "Sports", "Video Games", "Traditional Indian Food")
    val difficulties = listOf("Easy", "Medium", "Hard")

    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var selectedDifficulty by remember { mutableStateOf(difficulties[0]) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        IconButton(onClick = {
            scope.launch {
                sessionManager.clearSession()
            }
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }) {
            Icon(Icons.Filled.ExitToApp, contentDescription = "Logout")
        }
        Text("Choose Category")
        LazyColumn(modifier = Modifier.height(150.dp)) {
            items(categories) { category ->
                Text(
                    text = if (category == selectedCategory) "$category ✓" else category,
                    color = if (category == selectedCategory) Color.Blue else Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedCategory = category }
                        .padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Choose Difficulty")
        Row {
            difficulties.forEach { level ->
                Text(
                    text = if (level == selectedDifficulty) "$level ✓" else level,
                    color = if (level == selectedDifficulty) Color.Blue else Color.Black,
                    modifier = Modifier
                        .clickable { selectedDifficulty = level }
                        .padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                navController.navigate("quiz/$selectedCategory/$selectedDifficulty")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start New Game")
        }
    }
}