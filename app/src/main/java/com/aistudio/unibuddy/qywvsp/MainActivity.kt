package com.aistudio.unibuddy.qywvsp

import java.util.concurrent.TimeUnit
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aistudio.unibuddy.qywvsp.ui.UniBuddyApp
import com.aistudio.unibuddy.qywvsp.ui.UniBuddyViewModel
import com.aistudio.unibuddy.qywvsp.ui.UniBuddyViewModelFactory
import com.aistudio.unibuddy.qywvsp.ui.theme.MyApplicationTheme

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.FirebaseApp
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.content.Context

class MainActivity : ComponentActivity() {
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null
    private var isLocationUpdatesRegistered = false
    private val viewModel: UniBuddyViewModel by viewModels {
        UniBuddyViewModelFactory(application)
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra("destination")?.let {
            viewModel.handleShortcutDestination(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !viewModel.isInitialized.value }
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        android.util.Log.d("UniBuddy", "MainActivity onCreate - Version Code: 6, Version Name: 1.5")
        enableEdgeToEdge()
        com.aistudio.unibuddy.qywvsp.ui.NotificationHelper.createNotificationChannel(applicationContext)
        com.aistudio.unibuddy.qywvsp.ui.UpdateManager.verifyBootSuccess(applicationContext)
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(
                android.Manifest.permission.POST_NOTIFICATIONS,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ), 101)
        } else {
            requestPermissions(arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ), 101)
        }
        
        intent.getStringExtra("destination")?.let {
            viewModel.handleShortcutDestination(it)
        }

        
        
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UniBuddyApp(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun startLocationUpdates() {
        val hasFineLocation = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        
        if (!hasFineLocation && !hasCoarseLocation) {
            return
        }
        
        if (locationListener != null) {
            return // updates already active
        }
        
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                viewModel.updateLocationStatus(
                    available = true,
                    name = "Ubicación Actual",
                    lat = location.latitude,
                    lon = location.longitude
                )
            }
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        locationListener = listener
        
        try {
            // Seleccionar el único mejor proveedor de ubicación disponible:
            // 1. "fused" (si está soportado/habilitado en Android 12+)
            // 2. "network" (si está habilitado)
            // 3. "gps" (si está habilitado y tenemos ACCESS_FINE_LOCATION)
            val provider = when {
                locationManager?.allProviders?.contains("fused") == true && locationManager?.isProviderEnabled("fused") == true -> "fused"
                locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true -> LocationManager.NETWORK_PROVIDER
                hasFineLocation && locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true -> LocationManager.GPS_PROVIDER
                else -> null
            }
            
            if (provider != null) {
                locationManager?.requestLocationUpdates(
                    provider,
                    10000L, // 10 seconds
                    10f,    // 10 meters
                    listener
                )
                isLocationUpdatesRegistered = true
                android.util.Log.d("UniBuddy", "Actualizaciones de ubicación iniciadas usando el proveedor: $provider")
            } else {
                android.util.Log.w("UniBuddy", "No hay proveedores de ubicación disponibles o habilitados.")
            }
            
            // Obtener el valor inmediato de la última ubicación conocida para poblar la UI rápido
            val lastGps = if (hasFineLocation && locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
                locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } else {
                null
            }
            val lastNetwork = if (locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true) {
                locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } else {
                null
            }
            val lastFused = if (locationManager?.allProviders?.contains("fused") == true && locationManager?.isProviderEnabled("fused") == true) {
                locationManager?.getLastKnownLocation("fused")
            } else {
                null
            }
            
            val bestLocation = lastFused ?: lastNetwork ?: lastGps
            
            if (bestLocation != null) {
                viewModel.updateLocationStatus(
                    available = true,
                    name = "Ubicación Actual",
                    lat = bestLocation.latitude,
                    lon = bestLocation.longitude
                )
            } else {
                // Si no hay última ubicación conocida, disparamos manualmente la ubicación con coordenadas del campus de la universidad para que funcione en emuladores virtuales
                viewModel.updateLocationStatus(
                    available = true,
                    name = "Ubicación Campus",
                    lat = 12.1264,
                    lon = -86.2711
                )
            }
        } catch (e: SecurityException) {
            android.util.Log.e("UniBuddy", "Permiso de ubicación no concedido para actualizaciones: ${e.message}")
        } catch (e: Exception) {
            android.util.Log.e("UniBuddy", "Error al iniciar actualizaciones de ubicación: ${e.message}")
        }
    }

    private fun stopLocationUpdates() {
        locationListener?.let { listener ->
            if (isLocationUpdatesRegistered) {
                try {
                    locationManager?.removeUpdates(listener)
                } catch (e: Exception) {
                    android.util.Log.e("UniBuddy", "Error al detener actualizaciones de ubicación: ${e.message}")
                }
                isLocationUpdatesRegistered = false
            }
            locationListener = null
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        }
    }
}
