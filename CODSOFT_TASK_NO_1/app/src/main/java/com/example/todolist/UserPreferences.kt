package com.example.todolist

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

object UserPreferences {
    val NAME_KEY = stringPreferencesKey("user_name")
    val ONBOARDING_DONE_KEY = booleanPreferencesKey("onboarding_done")

    suspend fun saveName(context: Context, name: String) {
        context.dataStore.edit { prefs ->
            prefs[NAME_KEY] = name
        }
    }

    suspend fun setOnboardingDone(context: Context, done: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_DONE_KEY] = done
        }
    }

    fun getName(context: Context): Flow<String> {
        return context.dataStore.data.map { prefs -> prefs[NAME_KEY] ?: "" }
    }

    fun isOnboardingDone(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { prefs -> prefs[ONBOARDING_DONE_KEY] ?: false }
    }

    suspend fun clearSession(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(NAME_KEY)
        }
    }
}

