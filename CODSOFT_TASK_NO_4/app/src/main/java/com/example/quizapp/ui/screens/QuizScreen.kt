package com.example.quizapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.activity.compose.BackHandler
import com.example.quizapp.data.Question
import com.example.quizapp.data.loadQuizData
import com.example.quizapp.data.QuizDatabase
import com.example.quizapp.data.QuizResult
import com.example.quizapp.data.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(navController: NavController, category: String, difficulty: String) {
    val context = LocalContext.current
    val db = remember { QuizDatabase.getDatabase(context) }
    val sessionManager = remember { SessionManager(context) }
    val userName by sessionManager.userName.collectAsState(initial = "")
    val scope = rememberCoroutineScope()

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

    var currentIndex by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var score by remember { mutableStateOf(0) }
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
        showExitDialog = true
    }

    val currentQuestion = questions[currentIndex]
    val useTwoPerRow = currentQuestion.options.all { it.length <= 12 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Question ${currentIndex + 1} of ${questions.size}") },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentQuestion.question,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (useTwoPerRow) {
                currentQuestion.options.chunked(2).forEach { rowOptions ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        rowOptions.forEach { option ->
                            OptionCard(
                                option = option,
                                selected = option == selectedOption,
                                onClick = { selectedOption = option },
                                modifier = Modifier.weight(1f).padding(4.dp)
                            )
                        }
                    }
                }
            } else {
                currentQuestion.options.forEach { option ->
                    OptionCard(
                        option = option,
                        selected = option == selectedOption,
                        onClick = { selectedOption = option },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = false, onClick = {
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
                    }),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = if (currentIndex < questions.size - 1) "Next" else "Finish Quiz",
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
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
                }) { Text("Yes") }
            },
            dismissButton = {
                Button(onClick = { showExitDialog = false }) { Text("No") }
            }
        )
    }
}

@Composable
fun OptionCard(option: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.selectable(selected = selected, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = option,
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            textAlign = TextAlign.Start
        )
    }
}