package com.example.practicaltrainingproject.presentation.ui.mainScreen.components

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.json.JSONArray

class SensorChartsViewModel(private val context: Context) : ViewModel() {
    val impactSensitivityHistory = mutableStateListOf<Float>()
    private val prefs: SharedPreferences = context.getSharedPreferences("impact_chart_prefs", Context.MODE_PRIVATE)
    private val key = "impactSensitivityHistory"

    init {
        loadHistory()
    }

    private fun loadHistory() {
        val json = prefs.getString(key, null)
        if (json != null) {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                impactSensitivityHistory.add(arr.getDouble(i).toFloat())
            }
        }
    }

    private fun saveHistory() {
        val arr = JSONArray()
        impactSensitivityHistory.forEach { arr.put(it) }
        prefs.edit().putString(key, arr.toString()).apply()
    }

    fun addImpactValue(value: Float) {
        impactSensitivityHistory.add(value)
        saveHistory()
    }

    fun clearHistory() {
        impactSensitivityHistory.clear()
        saveHistory()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SensorChartsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SensorChartsViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
