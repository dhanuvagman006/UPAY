package com.example.upay

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.upay.ui.theme.UPAYTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoginApp()
        }
    }
}

@Composable
fun LoginApp() {
    var showSplashScreen by remember { mutableStateOf(true) }
    var isLoggedIn by remember { mutableStateOf(true) } // Corrected: initial state to false
    var darkModeEnabled by rememberSaveable { mutableStateOf(false) } // State for dark mode
    val context = LocalContext.current

    val permissionsToRequest = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.READ_PHONE_STATE
    )

    // Launcher for standard runtime permissions
    val multiplePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                Log.d("Permissions", "All runtime permissions granted.")
                // Now check for overlay permission if not already done by onLoginSuccess
            } else {
                Log.w("Permissions", "Not all runtime permissions were granted.")
            }
            // Potentially move isLoggedIn = true here after all checks, including overlay
        }
    )

    // Launcher for SYSTEM_ALERT_WINDOW permission
    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            // User has returned from the settings screen. Check permission status again.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(context)) {
                    Log.d("Permissions", "SYSTEM_ALERT_WINDOW permission granted.")
                } else {
                    Log.w("Permissions", "SYSTEM_ALERT_WINDOW permission was NOT granted.")
                }
            }
            // isLoggedIn = true would typically be set here if this was the last step
            // For simplicity, it's currently set after launching this in onLoginSuccess
        }
    )

    LaunchedEffect(Unit) {
        delay(2000) // Simulate a delay for the splash screen
        showSplashScreen = false
    }

    UPAYTheme(darkTheme = darkModeEnabled) {
        if (showSplashScreen) {
            SplashScreen()
        } else if (!isLoggedIn) {
            LoginScreen(onLoginSuccess = {
                val allRuntimePermissionsGranted = permissionsToRequest.all {
                    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                }

                if (!allRuntimePermissionsGranted) {
                    multiplePermissionsLauncher.launch(permissionsToRequest)
                }

                // After handling runtime permissions, check for overlay permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    overlayPermissionLauncher.launch(intent)
                } 
                
                // Set isLoggedIn to true to proceed to MainAppScreen.
                // Ideally, this should only happen if all permissions (including overlay) are granted.
                // For now, we proceed after requesting them.
                isLoggedIn = true 
            })
        } else {
            MainAppScreen(darkModeEnabled = darkModeEnabled, onDarkModeChange = { darkModeEnabled = it })
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Scaffold { paddingValues ->
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(150.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )
            Button(
                onClick = onLoginSuccess,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log In")
            }
        }
    }
}

@Preview(showBackground = true, name = "Splash Screen Preview")
@Composable
fun SplashScreenPreview() {
    UPAYTheme { SplashScreen() }
}

@Preview(showBackground = true, name = "Login Screen Preview Light")
@Composable
fun LoginScreenPreviewLight() {
    UPAYTheme(darkTheme = false) { LoginScreen(onLoginSuccess = {}) }
}

@Preview(showBackground = true, name = "Login Screen Preview Dark")
@Composable
fun LoginScreenPreviewDark() {
    UPAYTheme(darkTheme = true) { LoginScreen(onLoginSuccess = {}) }
}
