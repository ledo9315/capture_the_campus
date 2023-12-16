package com.hsfl.leo_nelly.capturethecampus

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private var locationServiceBinder: LocationService? = null
    private var isBound = false

    // Verbindung zum LocationService
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d("MainActivity", "Service connected")
            val binder = service as LocationService.LocalBinder
            locationServiceBinder = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d("MainActivity", "Service disconnected")
            isBound = false
        }
    }

    //Abwicklung der Berechtigungsanfrage für den Standortzugriff
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startLocation()
        } else {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bindLocationService()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindLocationService()
    }

    private fun bindLocationService() {
        Log.d("MainActivity", "Binding LocationService")
        Intent(this, LocationService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun unbindLocationService() {
        Log.d("MainActivity", "Unbinding LocationService")
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    // Startet die Standortaktualisierungen, wenn die Berechtigungen vorhanden sind.
    fun startLocation() {
        if (checkLocationPermission(this)) {
            if (isLocationEnabled(this)) {
                requestLocationUpdates(locationServiceBinder)
            } else {
                showLocationServicesSnackbar()
            }
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationUpdates(serviceBinder: LocationService?) {
        serviceBinder?.startLocationUpdates { location ->
            Log.d("MainActivity", "Location: $location")
            mainViewModel.setLocation(location)
            mainViewModel.updateMapPointsStatus()
        }
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun requestLocationPermission() {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun stopLocation() {
        locationServiceBinder?.stopLocationUpdates()
    }

    private fun checkLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Zeigt eine Snackbar an, wenn die Standortdienste deaktiviert sind
    private fun showLocationServicesSnackbar() {
        Snackbar.make(
            findViewById(android.R.id.content),
            "Location services are deactivated",
            Snackbar.LENGTH_LONG
        )
            .setAction("Activate") {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }.show()
    }
}
