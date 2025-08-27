package com.example.practicaltrainingproject.data.model

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.practicaltrainingproject.data.local.HourlyImpactData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.hourlyDataStore by preferencesDataStore(name = "hourly_impact")

class HourlyImpactStore(private val context: Context) {
    private val hours = (0..23).map { it.toString() }
    private val keys = hours.associateWith { hour -> intPreferencesKey(hour) }

    val hourlyData: Flow<HourlyImpactData> = context.hourlyDataStore.data.map { prefs ->
        HourlyImpactData(
            counts = hours.associateWith { hour -> prefs[keys[hour]!!] ?: 0 }
        )
    }

    suspend fun updateHourCount(hour: String, newValue: Int) {
        context.hourlyDataStore.edit { prefs ->
            prefs[keys[hour]!!] = newValue
        }
    }

    suspend fun saveHourlyData(data: HourlyImpactData) {
        context.hourlyDataStore.edit { prefs ->
            data.counts.forEach { (hour, value) ->
                prefs[keys[hour]!!] = value
            }
        }
    }
}

