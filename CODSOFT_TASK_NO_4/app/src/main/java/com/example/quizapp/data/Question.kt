package com.example.quizapp.data

data class QuizData(
    val categories: List<Category>
)

data class Category(
    val name: String,
    val questions: List<Question>
)

data class Question(
    val difficulty: String,
    val question: String,
    val options: List<String>,
    val correctAnswer: String
)