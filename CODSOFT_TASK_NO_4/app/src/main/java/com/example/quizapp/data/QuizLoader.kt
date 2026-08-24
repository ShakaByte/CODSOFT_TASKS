package com.example.quizapp.data

import android.content.Context
import org.json.JSONObject

fun loadQuizData(context: Context): QuizData {
    val jsonString = context.assets.open("quizqeust.json")
        .bufferedReader()
        .use { it.readText() }

    val rootObject = JSONObject(jsonString)
    val categoriesArray = rootObject.getJSONArray("categories")
    val categories = mutableListOf<Category>()

    for (i in 0 until categoriesArray.length()) {
        val catObj = categoriesArray.getJSONObject(i)
        val name = catObj.getString("name")
        val questionsArray = catObj.getJSONArray("questions")
        val questions = mutableListOf<Question>()

        for (j in 0 until questionsArray.length()) {
            val qObj = questionsArray.getJSONObject(j)
            val optionsArray = qObj.getJSONArray("options")
            val options = mutableListOf<String>()
            for (k in 0 until optionsArray.length()) {
                options.add(optionsArray.getString(k))
            }

            questions.add(
                Question(
                    difficulty = qObj.getString("difficulty"),
                    question = qObj.getString("question"),
                    options = options,
                    correctAnswer = qObj.getString("correctAnswer")
                )
            )
        }

        categories.add(Category(name = name, questions = questions))
    }

    return QuizData(categories = categories)
}