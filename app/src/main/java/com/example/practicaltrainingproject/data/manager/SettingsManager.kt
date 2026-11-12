package com.example.practicaltrainingproject.data.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    companion object {
        val BACKGROUND_MONITORING = booleanPreferencesKey("background_monitoring")
        val LOCATION_ENABLED = booleanPreferencesKey("location_enabled")
        val HIGH_ACCURACY_MODE = booleanPreferencesKey("high_accuracy_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val SOUND_ALERTS = booleanPreferencesKey("sound_alerts")
        val AUTO_EMERGENCY_CALL = booleanPreferencesKey("auto_emergency_call")
        val AUTO_EMERGENCY_SMS = booleanPreferencesKey("auto_emergency_sms")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val SHARE_DATA = booleanPreferencesKey("share_data")
    }

    // Flows to observe settings
    val backgroundMonitoring: Flow<Boolean> = context.settingsDataStore.data
        .map { it[BACKGROUND_MONITORING] ?: false }

    val locationEnabled: Flow<Boolean> = context.settingsDataStore.data
        .map { it[LOCATION_ENABLED] ?: false }

    val highAccuracyMode: Flow<Boolean> = context.settingsDataStore.data
        .map { it[HIGH_ACCURACY_MODE] ?: false }

    val notificationsEnabled: Flow<Boolean> = context.settingsDataStore.data
        .map { it[NOTIFICATIONS_ENABLED] ?: true }

    val soundAlerts: Flow<Boolean> = context.settingsDataStore.data
        .map { it[SOUND_ALERTS] ?: true }

    val autoEmergencyCall: Flow<Boolean> = context.settingsDataStore.data
        .map { it[AUTO_EMERGENCY_CALL] ?: true }

    val autoEmergencySms: Flow<Boolean> = context.settingsDataStore.data
        .map { it[AUTO_EMERGENCY_SMS] ?: true }

    val darkTheme: Flow<Boolean> = context.settingsDataStore.data
        .map { it[DARK_THEME] ?: false }

    val shareData: Flow<Boolean> = context.settingsDataStore.data
        .map { it[SHARE_DATA] ?: false }

    // Save functions
    suspend fun setBackgroundMonitoring(enabled: Boolean) {
        context.settingsDataStore.edit { it[BACKGROUND_MONITORING] = enabled }
    }

    suspend fun setLocationEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[LOCATION_ENABLED] = enabled }
    }

    suspend fun setHighAccuracyMode(enabled: Boolean) {
        context.settingsDataStore.edit { it[HIGH_ACCURACY_MODE] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setSoundAlerts(enabled: Boolean) {
        context.settingsDataStore.edit { it[SOUND_ALERTS] = enabled }
    }

    suspend fun setAutoEmergencyCall(enabled: Boolean) {
        context.settingsDataStore.edit { it[AUTO_EMERGENCY_CALL] = enabled }
    }

    suspend fun setAutoEmergencySms(enabled: Boolean) {
        context.settingsDataStore.edit { it[AUTO_EMERGENCY_SMS] = enabled }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.settingsDataStore.edit { it[DARK_THEME] = enabled }
    }

    suspend fun setShareData(enabled: Boolean) {
        context.settingsDataStore.edit { it[SHARE_DATA] = enabled }
    }
}

