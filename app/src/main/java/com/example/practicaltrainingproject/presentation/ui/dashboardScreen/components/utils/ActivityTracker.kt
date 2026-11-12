package com.example.practicaltrainingproject.presentation.ui.dashboardScreen.components.utils

/**
 * Tracks user activity and monitoring consistency
 * Calculates active days and recent activity status
 */

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

val Context.dataStore by preferencesDataStore(name = "activity_tracker")

class ActivityTracker(private val context: Context) {

    private companion object {
        val LAST_ACTIVE_DAY = intPreferencesKey("last_active_day")
        val ACTIVE_DAYS_COUNT = intPreferencesKey("active_days_count")
        val CURRENT_STREAK = intPreferencesKey("current_streak")
    }

    suspend fun recordDailyActivity() {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

        context.dataStore.edit { preferences ->
            val lastActiveDay = preferences[LAST_ACTIVE_DAY] ?: 0
            val currentStreak = preferences[CURRENT_STREAK] ?: 0
            val activeDaysCount = preferences[ACTIVE_DAYS_COUNT] ?: 0

            if (lastActiveDay != today) {
                // New day - update streak
                val newStreak = if (lastActiveDay == today - 1) currentStreak + 1 else 1

                preferences[LAST_ACTIVE_DAY] = today
                preferences[CURRENT_STREAK] = newStreak
                preferences[ACTIVE_DAYS_COUNT] = activeDaysCount + 1
            }
        }
    }

    val activeDays: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[ACTIVE_DAYS_COUNT] ?: 0
        }

    val currentStreak: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[CURRENT_STREAK] ?: 0
        }

    fun hasRecentActivity(lastFallTimestamp: String): Boolean {
        return lastFallTimestamp != "Never" // Simple check - can be enhanced
    }
}