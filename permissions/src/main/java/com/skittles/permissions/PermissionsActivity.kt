package com.skittles.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.skittles.permissions.ui.theme.AndroidcoresTheme

class PermissionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidcoresTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SinglePermissionSample()
                        Spacer(modifier = Modifier.size(20.dp))
                        MultiplePermissionsSample()
                    }
                }
            }
        }
    }
}

@Composable
fun SinglePermissionSample() {
    val context = LocalContext.current
    var showCameraRationaleDialog by remember { mutableStateOf(false) }

    var showPermanentDeniedDialog by remember { mutableStateOf(false) }
    val cameraPermission = Manifest.permission.CAMERA

    var cameraPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                cameraPermission
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Launcher for requesting permissions
    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            cameraPermissionGranted = isGranted

            if (cameraPermissionGranted.not()) {
                // Permission denied
                if (shouldShowRequestPermissionRationale(context, cameraPermission)) {
                    showCameraRationaleDialog = true
                } else {
                    showPermanentDeniedDialog = true
                }
            }
        }
    )

    if (cameraPermissionGranted.not()) {
        Button(
            onClick = { requestCameraPermissionLauncher.launch(cameraPermission) }
        ) {
            Text("Request Camera Permission")
        }
    } else {
        Text("Camera permission is granted!")
        // Your logic here when permission is granted
    }

    if (showCameraRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showCameraRationaleDialog = false },
            title = { Text("Permission Required") },
            text = { Text("This app requires camera permission to provide better user experiences. Please allow access.") },
            confirmButton = {
                Button(onClick = {
                    showCameraRationaleDialog = false
                    requestCameraPermissionLauncher.launch(cameraPermission)
                }) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                Button(onClick = { showCameraRationaleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Show permanent denied dialog
    if (showPermanentDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showPermanentDeniedDialog = false },
            title = { Text("Permission Denied") },
            text = { Text("permission has been permanently denied. You can enable it in app settings.") },
            confirmButton = {
                Button(onClick = {
                    showPermanentDeniedDialog = false
                    // Redirect to app settings
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }) {
                    Text("Go to Settings")
                }
            }
        )
    }
}

@Composable
fun MultiplePermissionsSample() {
    val context = LocalContext.current
    var showLocationsRationaleDialog by remember { mutableStateOf(false) }
    var showPermanentDeniedDialog by remember { mutableStateOf(false) }
    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    var locationPermissionsGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val requestLocationsPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->

            locationPermissionsGranted = permissions.values.reduce { acc, isPermissionGranted ->
                acc && isPermissionGranted
            }

            if (locationPermissionsGranted.not()) {
                // Permission denied
                if (shouldShowRequestPermissionRationale(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                ) {
                    showLocationsRationaleDialog = true
                } else {
                    showPermanentDeniedDialog = true
                }
            }
        }
    )

    if (locationPermissionsGranted.not()) {
        Button(
            onClick = { requestLocationsPermissionsLauncher.launch(locationPermissions) }
        ) {
            Text("Request Location Permissions")
        }
    } else {
        Text("Location permissions are granted!")
        // Your logic here when permission is granted
    }

    if (showLocationsRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showLocationsRationaleDialog = false },
            title = { Text("Permission Required") },
            text = { Text("This app requires locations permissions to provide better user experiences. Please allow access.") },
            confirmButton = {
                Button(onClick = {
                    showLocationsRationaleDialog = false
                    requestLocationsPermissionsLauncher.launch(locationPermissions)
                }) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                Button(onClick = { showLocationsRationaleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Show permanent denied dialog
    if (showPermanentDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showPermanentDeniedDialog = false },
            title = { Text("Permission Denied") },
            text = { Text("permission has been permanently denied. You can enable it in app settings.") },
            confirmButton = {
                Button(onClick = {
                    showPermanentDeniedDialog = false
                    // Redirect to app settings
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }) {
                    Text("Go to Settings")
                }
            }
        )
    }

}

private fun shouldShowRequestPermissionRationale(context: Context, permission: String): Boolean {
    return ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission)
}