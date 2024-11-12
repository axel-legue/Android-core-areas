package com.skittles.services

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.skittles.services.ui.theme.AndroidcoresTheme

class MainActivity : ComponentActivity() {

    private var boundService: BoundService? = null
    private var isServiceBound = false
    private var serviceStatus by mutableStateOf("Service Status: Idle")

    // Permission request launcher
    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            startForegroundServiceWithPermissions()
        } else {
            Toast.makeText(
                this,
                "Location permissions are required for the foreground service",
                Toast.LENGTH_LONG
            ).show()
            serviceStatus = "Foreground Service - Permission Denied"
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as BoundService.LocalBinder
            boundService = binder.getService()
            isServiceBound = true
            serviceStatus = "Bound to Service"
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isServiceBound = false
            boundService = null
            serviceStatus = "Unbound from Service"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            AndroidcoresTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var randomNumber by remember { mutableIntStateOf(0) }
                    
                    ServiceDemoScreen(
                        modifier = Modifier.padding(innerPadding),
                        onStartForegroundService = { startForegroundService() },
                        onStopForegroundService = { stopForegroundService() },
                        onStartBackgroundService = { startBackgroundService() },
                        onStopBackgroundService = { stopBackgroundService() },
                        onBindService = { bindToService() },
                        onUnbindService = { unbindFromService() },
                        onStartIntentService = { startIntentService() },
                        isServiceBound = isServiceBound,
                        serviceStatus = serviceStatus,
                        getRandomNumber = { randomNumber = boundService?.getRandomNumber() ?: 0 },
                        randomNumber = randomNumber.toString()
                    )
                }
            }
        }
    }

    private fun startForegroundService() {
        if (checkAndRequestLocationPermissions()) {
            startForegroundServiceWithPermissions()
        } else {
            Toast.makeText(this, "Requesting location permissions", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startForegroundServiceWithPermissions() {
        val intent = Intent(this, ForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        serviceStatus = "Foreground Service Started"
    }

    private fun checkAndRequestLocationPermissions(): Boolean {
        val permissions = mutableListOf(
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.FOREGROUND_SERVICE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Check if we need coarse or fine location
        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasFineLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasCoarseLocation && !hasFineLocation) {
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        return if (permissionsToRequest.isNotEmpty()) {
            // Launch the permission request
            requestPermissionsLauncher.launch(permissionsToRequest)
            false
        } else {
            true
        }
    }

    private fun stopForegroundService() {
        val intent = Intent(this, ForegroundService::class.java)
        stopService(intent)
        serviceStatus = "Foreground Service Stopped"
    }

    private fun startBackgroundService() {
        val intent = Intent(this, BackgroundService::class.java)
        startService(intent)
        serviceStatus = "Background Service Started"
    }

    private fun stopBackgroundService() {
        val intent = Intent(this, BackgroundService::class.java)
        stopService(intent)
        serviceStatus = "Background Service Stopped"
    }

    private fun bindToService() {
        Intent(this, BoundService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun unbindFromService() {
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
            serviceStatus = "Service Unbound"
        }
    }

    private fun startIntentService() {
        val intent = Intent(this, SimpleIntentService::class.java)
        startService(intent)
        serviceStatus = "Intent Service Started"
    }
}

@Composable
fun ServiceDemoScreen(
    modifier: Modifier = Modifier,
    onStartForegroundService: () -> Unit,
    onStopForegroundService: () -> Unit,
    onStartBackgroundService: () -> Unit,
    onStopBackgroundService: () -> Unit,
    onBindService: () -> Unit,
    onUnbindService: () -> Unit,
    onStartIntentService: () -> Unit,
    isServiceBound: Boolean,
    getRandomNumber: () -> Unit,
    randomNumber: String,
    serviceStatus: String
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = onStartForegroundService) {
            Text("Start Foreground Service")
        }
        Button(onClick = onStopForegroundService) {
            Text("Stop Foreground Service")
        }
        Button(onClick = onStartBackgroundService) {
            Text("Start Background Service")
        }
        Button(onClick = onStopBackgroundService) {
            Text("Stop Background Service")
        }
        Button(onClick = onBindService) {
            Text("Bind to Service")
        }
        Button(onClick = onUnbindService) {
            Text("Unbind from Service")
        }
        Button(onClick = onStartIntentService) {
            Text("Start Intent Service")
        }
        if (isServiceBound) {
            Text(text = "Random Number: $randomNumber")
            Button(onClick = {
                getRandomNumber()
            }) {
                Text("Get Random Number")
            }
        }

        Text(
            text = serviceStatus,
            modifier = Modifier.padding(top = 16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
