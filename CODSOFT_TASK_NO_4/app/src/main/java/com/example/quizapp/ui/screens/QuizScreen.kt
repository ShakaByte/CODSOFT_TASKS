package com.example.quizapp.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.quizapp.data.Question
import com.example.quizapp.data.loadQuizData
import androidx.compose.material3.AlertDialog
import com.example.quizapp.data.QuizDatabase
import com.example.quizapp.data.QuizResult
import kotlinx.coroutines.launch
import com.example.quizapp.data.SessionManager
import androidx.compose.runtime.collectAsState
@Composable
fun QuizScreen(navController: NavController, category: String, difficulty: String) {
    val context = LocalContext.current

    val quizData = remember { loadQuizData(context) }

    val actualCategory = remember {
        if (category == "Random") quizData.categories.random().name else category
    }

    val questions = remember {
        quizData.categories
            .first { it.name == actualCategory }
            .questions
            .filter { it.difficulty.equals(difficulty, ignoreCase = true) }
    }
    val sessionManager = remember { SessionManager(context) }
    val userName by sessionManager.userName.collectAsState(initial = "")
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var score by remember { mutableIntStateOf(0) }
    val db = remember { QuizDatabase.getDatabase(context) }
    val scope = rememberCoroutineScope()
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit Quiz?") },
            text = { Text("Your progress will be lost and score marked as zero.") },
            confirmButton = {
                Button(onClick = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { showExitDialog = false }) {
                    Text("No")
                }
            }
        )
    }
    val currentQuestion = questions[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Question ${currentIndex + 1} of ${questions.size}")

        Spacer(modifier = Modifier.height(16.dp))

        Text(currentQuestion.question)

        Spacer(modifier = Modifier.height(16.dp))

        currentQuestion.options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (option == selectedOption),
                        onClick = { selectedOption = option }
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (option == selectedOption),
                    onClick = { selectedOption = option }
                )
                Text(option)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (selectedOption == currentQuestion.correctAnswer) {
                    score++
                }

                if (currentIndex < questions.size - 1) {
                    currentIndex++
                    selectedOption = null
                } else {
                    val finalScore = if (selectedOption == currentQuestion.correctAnswer) score else score
                    scope.launch {
                        db.quizResultDao().insertResult(
                            QuizResult(
                                userName = userName ?: "",
                                category = actualCategory,
                                difficulty = difficulty,
                                score = score,
                                total = questions.size,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                    navController.navigate("result/$score/${questions.size}/$actualCategory/$difficulty")
                }
            }
        ) {
            Text(if (currentIndex < questions.size - 1) "Next" else "Finish Quiz")
        }
    }
}