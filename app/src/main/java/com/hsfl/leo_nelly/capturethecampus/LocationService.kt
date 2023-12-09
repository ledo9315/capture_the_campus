package com.hsfl.leo_nelly.capturethecampus

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

class LocationService : Service() {
    private val binder = LocalBinder()
    private lateinit var locationClient: FusedLocationProviderClient
    private var updateLocationCallback: ((Location) -> Unit)? = null
    private lateinit var locationCallback: LocationCallback
    private val notificationChannelId = "de.hsfl.leo_nelly.capturethecampus.notification"
    private val notificationChannelName = "Location Notification"
    private lateinit var notificationManager: NotificationManager
    private var isForegroundStarted = false

    companion object {
        private const val NOTIFICATION_ID = 1
    }

    inner class LocalBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NotificationManager::class.java) as NotificationManager
        createNotificationChannel()
        locationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            notificationChannelId,
            notificationChannelName,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun startForegroundIfNeeded() {
            startForeground(NOTIFICATION_ID, buildNotification())
            isForegroundStarted = true
    }

    private fun stopForegroundIfNeeded() {
            stopForeground(STOP_FOREGROUND_REMOVE)
            isForegroundStarted = false
    }

    fun startLocationUpdates(updateCallback: (Location) -> Unit) {
        if (!checkLocationPermission()) {
            Log.d("LocationService", "Location permission not granted")
            return
        }
        startForegroundIfNeeded()
        updateLocationCallback = updateCallback
        val locationRequest = LocationRequest.Builder(1000L)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                result.lastLocation?.let { location ->
                    updateLocationCallback?.invoke(location)
                }
            }
        }
        locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun stopLocationUpdates() {
        if (::locationClient.isInitialized) {
            locationClient.removeLocationUpdates(locationCallback)
        }
        stopForegroundIfNeeded()
    }

    private fun buildNotification() = NotificationCompat.Builder(this, notificationChannelId)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("Location Service")
        .setContentText("Running...")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
