/**
 * Service to get current location for emergency alerts
 * Provides GPS coordinates to include in emergency notifications
 * Singleton pattern ensures location data is shared across the app
 */
package com.example.practicaltrainingproject.data.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocationService private constructor(private val context: Context) {

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation

    private var isUpdating = false

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            _currentLocation.value = location
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    fun startLocationUpdates() {
        if (isUpdating) return // Already updating

        if (hasLocationPermission()) {
            try {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000L, // 5 seconds
                    10f,   // 10 meters
                    locationListener
                )

                isUpdating = true

                // Get last known location immediately
                val lastLocation =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                lastLocation?.let { _currentLocation.value = it }
            } catch (e: SecurityException) {
                // Permission revoked at runtime
                isUpdating = false
            }
        }
    }

    fun stopLocationUpdates() {
        if (!isUpdating) return // Not updating

        try {
            locationManager.removeUpdates(locationListener)
            isUpdating = false
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun getCurrentLocationString(): String {
        val location = _currentLocation.value
        return if (location != null) {
            "Lat: ${"%.6f".format(location.latitude)}, Lng: ${"%.6f".format(location.longitude)}"
        } else {
            "Location unavailable"
        }
    }

    fun getLocationForEmergency(): String {
        val location = _currentLocation.value
        return if (location != null) {
            "https://maps.google.com/?q=${location.latitude},${location.longitude}"
        } else {
            "Location not available"
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001

        @Volatile
        private var INSTANCE: LocationService? = null

        fun getInstance(context: Context): LocationService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocationService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}