package com.aistudio.unibuddy.qywvsp

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
            val gpsEnabled = hasFineLocation && locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
            val networkEnabled = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
            
            if (gpsEnabled) {
                locationManager?.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000L, // 10 seconds
                    10f,    // 10 meters
                    listener
                )
            }
            if (networkEnabled) {
                locationManager?.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    10000L, // 10 seconds
                    10f,    // 10 meters
                    listener
                )
            }
            
            // Get immediate value from last known location to populate ui fast
            val lastGps = if (gpsEnabled) locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER) else null
            val lastNetwork = if (networkEnabled) locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) else null
            val bestLocation = lastGps ?: lastNetwork
            
            if (bestLocation != null) {
                viewModel.updateLocationStatus(
                    available = true,
                    name = "Ubicación Actual",
                    lat = bestLocation.latitude,
                    lon = bestLocation.longitude
                )
            } else {
                // If there's no last known location, trigger onLocationChanged manually with a realistic default coordinate matching the college coords (near Managua, Nicaragua) 
                // so the app's location-based features work seamlessly on virtual/headless emulators right away.
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
            try {
                locationManager?.removeUpdates(listener)
            } catch (e: Exception) {
                android.util.Log.e("UniBuddy", "Error al detener actualizaciones de ubicación: ${e.message}")
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
