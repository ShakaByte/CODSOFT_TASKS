package com.example.quizapp.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "session")

object SessionKeys {
    val USER_NAME = stringPreferencesKey("user_name")
}

class SessionManager(private val context: Context) {

    val userName: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[SessionKeys.USER_NAME]
    }

    suspend fun saveUserName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[SessionKeys.USER_NAME] = name
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(SessionKeys.USER_NAME)
        }
    }
}