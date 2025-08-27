package com.example.practicaltrainingproject.data.model

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.practicaltrainingproject.data.local.WeeklyImpactData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "weekly_impact")

class WeeklyImpactStore(private val context: Context) {

    private val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    private val keys = days.associateWith { day -> intPreferencesKey(day) }

    val weeklyData: Flow<WeeklyImpactData> = context.dataStore.data.map { prefs ->
        WeeklyImpactData(
            counts = days.associateWith { day -> prefs[keys[day]!!] ?: 0 }
        )
    }

    suspend fun updateDayCount(day: String, newValue: Int) {
        context.dataStore.edit { prefs ->
            prefs[keys[day]!!] = newValue
        }
    }
}
