package com.easy_permission_demo

import android.Manifest
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.easy_permissions.permission_enums.PermissionStatus
import com.easy_permissions.permission_enums.PermissionType
import com.easy_permissions.composablePermissionManager
import java.io.File
import kotlin.text.substringAfterLast


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }


    enum class Screen {
        DASHBOARD, CAMERA, LOCATION, STORAGE, AUDIO, NOTIFICATIONS, VIDEO
    }

    @Preview(showBackground = true)
    @Composable
    fun AppNavigation() {
        // Variable that manages which screen is currently displayed
        var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }

        when (currentScreen) {
            Screen.DASHBOARD -> DashboardScreen(onNavigate = { currentScreen = it })
            Screen.CAMERA -> CameraDemoScreen(onBack = { currentScreen = Screen.DASHBOARD })
            Screen.LOCATION -> LocationDemoScreen(onBack = { currentScreen = Screen.DASHBOARD })
            Screen.STORAGE -> StorageDemoScreen(onBack = { currentScreen = Screen.DASHBOARD })
            Screen.AUDIO -> AudioDemoScreen(onBack = { currentScreen = Screen.DASHBOARD })
            Screen.NOTIFICATIONS -> NotificationsDemoScreen(onBack = {
                currentScreen = Screen.DASHBOARD
            })

            Screen.VIDEO -> VideoDemoScreen(
                onBack = { currentScreen = Screen.DASHBOARD })
        }
    }


    @Composable
    fun DashboardScreen(onNavigate: (Screen) -> Unit) {
        val permissionManager = composablePermissionManager()
        val context = LocalContext.current

        // state to hold the log result of the mass request
        var massRequestResult by remember { mutableStateOf("") }
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🛡️ EasyPermission Toolbox", style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Select a scenario to demonstrate the library's capabilities",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { onNavigate(Screen.CAMERA) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(vertical = 4.dp)
            ) {
                Text(text = "📸 Smart Camera Component Demo")
            }

            Button(
                onClick = { onNavigate(Screen.LOCATION) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text(text = "📍 Location, Distance & Navigation Demo")
            }

            Button(
                onClick = { onNavigate(Screen.STORAGE) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text(text = "🖼️ Storage & Gallery Management Demo")
            }

            Button(
                onClick = { onNavigate(Screen.AUDIO) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) // Red color for recording
            ) {
                Text(text = "🎙️ Voice Recording Demo (Microphone)")
            }
            Button(
                onClick = { onNavigate(Screen.NOTIFICATIONS) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(text = "🔔 Notification Permission Demo (Push)")
            }
            Button(
                onClick = { onNavigate(Screen.VIDEO) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(text = "📹 Video Recording & Player Demo")
            }
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // THE MEGA BUTTON: Requests ALL Enum Permissions at the same time!
            Button(
                onClick = {
                    // Collect all defined enum values from our EasyPermissionType
                    val allPermissionsList = PermissionType.entries.map { it.toManifestString() }
                        .filter { it.isNotEmpty() }

                    massRequestResult = "Launching Mega Request..."

                    // Trigger the multi-permission core architecture of your library!
                    permissionManager.requestMultiple(allPermissionsList) { resultsMap ->
                        val approvedCount = resultsMap.values.count { it }
                        val totalCount = resultsMap.size

                        massRequestResult =
                            "📊 Result: $approvedCount / $totalCount Granted\n\n" + resultsMap.entries.joinToString(
                                "\n"
                            ) {
                                "${ it.key.substringAfterLast(".")}: ${if (it.value) "✅" else "❌"}"
                            }

                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.inverseSurface)
            ) {
                Text(
                    text = "⚡ REQUEST ALL PERMISSIONS", style = MaterialTheme.typography.titleMedium
                )
            }

            // Display results log dynamically on the screen
            if (massRequestResult.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = massRequestResult,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

// ==========================================
// 📸 Demo Screen: Smart Camera
// ==========================================

    @Preview(showBackground = true)
    @Composable
    fun CameraDemoScreen(onBack: () -> Unit = {}) {
        val permissionManager = composablePermissionManager()
        var cameraStatus by remember { mutableStateOf("Permission was not checked yet.") }
        var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
        val context = LocalContext.current
        val isCameraGranted = permissionManager.camera.isCameraPermissionGranted()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back"
                    )
                }
                Text(text = "Smart Camera Demo", style = MaterialTheme.typography.titleLarge)
            }

            Spacer(modifier = Modifier.height(32.dp))

            cameraStatus = if (isCameraGranted) "✅Approved camera access" else "No camera access"
            Text(
                text = "status: $cameraStatus", style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))


            capturedBitmap?.let { bitmap ->
                Text(
                    text = "The picture taken:", style = MaterialTheme.typography.bodyLarge
                )
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "The captured image",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            //Camera only
            Button(
                onClick = {
                    permissionManager.camera.requestCameraPermission { isGranted ->
                        if (isGranted == PermissionStatus.ACCESS_GRANTED) {
                            cameraStatus = "✅Approved camera access"
                            Toast.makeText(context, "Thanks for approving", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            cameraStatus = "❌Denied, no camera access"
                            Toast.makeText(context, "Access was denied by user", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            ) {
                Text(text = "Request camera permission")
            }
            if (isCameraGranted) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        permissionManager.camera.takePicture(onSuccess = { bitmap ->
                            if (bitmap != null) {
                                capturedBitmap = bitmap
                                cameraStatus = "✅ Image captured successfully!"
                            } else {
                                cameraStatus = "❌ Capture canceled by user."
                            }
                        }, onFailure = { exception ->
                            cameraStatus = "❌ Capture error: ${exception.message}"
                        })
                        Toast.makeText(context, "Thanks for approving", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(text = "Take Photo")
                }
            }
        }
    }


    // ==========================================
// 📍 Demo Screen: Smart Location & Navigation
// ==========================================
    @Preview(showBackground = true)
    @Composable
    fun LocationDemoScreen(onBack: () -> Unit = {}) {
        val context = LocalContext.current
        val permissionManager = composablePermissionManager()
        var statusText by remember { mutableStateOf("") }
        var locationText by remember { mutableStateOf("") }
        var distanceText by remember { mutableStateOf("") }
        var addressText by remember { mutableStateOf("") }
        var isPermissionGranted by remember { mutableStateOf(false) }
        isPermissionGranted = permissionManager.location.isLocationPermissionGranted()

        val eiffelTowerLat = 48.8584
        val eiffelTowerLng = 2.2945

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back"
                    )
                }
                Text(
                    text = "Smart Location & Navigation",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            statusText =
                if (isPermissionGranted) "✅Approved location access" else "No location access"
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Status: $statusText", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = locationText, style = MaterialTheme.typography.bodyLarge)
            if (distanceText.isNotEmpty()) Text(
                text = distanceText, color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = addressText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    permissionManager.location.requestLocationPermission {
                        if (it == PermissionStatus.ACCESS_GRANTED) {
                            locationText = "Getting your location.."
                            permissionManager.location.getCurrentLocation(onSuccess = { location, lat, lng, address ->
                                if (location != null) {
                                    locationText = "Latitude: $lat\nLongitude: $lng"

                                    // Using your library's mathematical calculation function!
                                    val distanceKm =
                                        permissionManager.location.calculateDistanceInKm(
                                            startLat = lat,
                                            startLng = lng,
                                            endLat = eiffelTowerLat,
                                            endLng = eiffelTowerLng
                                        )

                                    // Display the distance rounded to two decimal places
                                    distanceText = "🗼You are ${
                                        String.format(
                                            "%.2f", distanceKm
                                        )
                                    } km away from the Eifel Tower!"
                                    addressText = "Your address: $address"
                                } else {
                                    locationText = "❌ Location disabled"
                                }
                            }, onFailure = { exception ->
                                locationText = "❌ Problem to find location: ${exception.message}"
                            })
                        }
                    }
                }, modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Request location permission")
            }
            if (isPermissionGranted) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        permissionManager.location.navigateToLocation(
                            eiffelTowerLat, eiffelTowerLng
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) { Text(text = "🚀 Navigate to Eiffel Tower") }
            }
        }
    }

// ==========================================
// 🖼️ Demo Screen: Smart Storage & Gallery
// ==========================================

    @Composable
    fun StorageDemoScreen(onBack: () -> Unit) {
        val permissionManager = composablePermissionManager()

        // State to hold information about the selected file
        var fileInfoText by remember { mutableStateOf("No file selected yet") }
        var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back"
                    )
                }
                Text(
                    text = "Advanced File & Storage Manager",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Info Card displaying file metadata
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📊 Selected File Info:", style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = fileInfoText, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- BUTTON 1: Pick ANY File (*/*) ---
            Button(
                onClick = {
                    permissionManager.storage.pickFile(mimeType = "*/*", onSuccess = { uri ->
                        if (uri != null) {
                            selectedFileUri = uri
                            fileInfoText = "📁 Generic File Detected!\n\nURI: $uri"
                        } else {
                            fileInfoText = "❌ Selection canceled by user."
                        }
                    }, onFailure = {
                        fileInfoText = "❌ Error: ${it.message}"
                        //permissionManager.openSettings()
                    })
                }, modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(vertical = 4.dp)
            ) {
                Text(text = "📁 Choose Any File (PDF, Zip, Doc...)")
            }

            // --- BUTTON 2: Filter Only PDFs (application/pdf) ---
            Button(
                onClick = {
                    permissionManager.storage.pickFile(
                        mimeType = "application/pdf",
                        onSuccess = { uri ->
                            if (uri != null) {
                                selectedFileUri = uri
                                fileInfoText = "📄 PDF Document Successfully Loaded!\n\nURI: $uri"
                            } else {
                                fileInfoText = "❌ Selection canceled."
                            }
                        },
                        onFailure = {
                            fileInfoText = "❌ Error: ${it.message}"
                        })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text(text = "📄 Filter Only PDF Documents")
            }

            // --- BUTTON 3: Filter Only Images (image/*) ---
            Button(
                onClick = {
                    permissionManager.storage.pickFile(mimeType = "image/*", onSuccess = { uri ->
                        if (uri != null) {
                            selectedFileUri = uri
                            fileInfoText = "🖼️ Image Media Loaded!\n\nURI: $uri"
                        } else {
                            fileInfoText = "❌ Selection canceled."
                        }
                    }, onFailure = { fileInfoText = "❌ Error: ${it.message}" })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text(text = "🖼️ Filter Only Images")
            }

            // Preview rendering area if the chosen file happens to be an image
            selectedFileUri?.let { uri ->
                if (fileInfoText.contains("🖼️")) {
                    Spacer(modifier = Modifier.height(16.dp))
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { ctx ->
                        android.widget.ImageView(ctx).apply {
                            scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
                        }
                    }, update = { it.setImageURI(uri) }, modifier = Modifier.size(150.dp)
                    )
                }
            }
        }
    }

    // ==========================================
// 🎙️ Demo Screen: Smart Voice Recording
// ==========================================
    @Composable
    fun AudioDemoScreen(onBack: () -> Unit) {
        val context = LocalContext.current
        val permissionManager = composablePermissionManager()

        var recordingStatus by remember { mutableStateOf("Status: Waiting") }
        var isRecording by remember { mutableStateOf(false) }

        // We'll keep the File object itself so we can pass it to the player
        var recordedFile by remember { mutableStateOf<File?>(null) }
        var isPlaying by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back"
                    )
                }
                Text(
                    text = "Smart Recording & Playback", style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
            Text(text = recordingStatus, style = MaterialTheme.typography.bodyLarge)

            if (recordedFile != null && recordedFile!!.exists()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📍 File ready for playback!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Record / Stop button (your existing code with a slight adjustment)
            Button(
                onClick = {
                    if (!isRecording) {
                        permissionManager.request(Manifest.permission.RECORD_AUDIO) { isGranted ->
                            if (isGranted == PermissionStatus.ACCESS_GRANTED) {
                                recordedFile = null // Reset previous recording
                                permissionManager.audio.startRecording(onSuccess = {
                                    isRecording = true
                                    recordingStatus = "🔴 Recording sound... speak now!"
                                }, onFailure = { exception ->
                                    recordingStatus = "❌ Error: ${exception.message}"
                                })
                            } else {
//                                if (!permissionManager.shouldShowRationale(Manifest.permission.RECORD_AUDIO)) {
//                                    permissionManager.openSettings()
//                                } else {
                                recordingStatus = "❌ Microphone permission denied."
                                //  }
                            }
                        }
                    } else {
                        val audioFile = permissionManager.audio.stopRecording()
                        isRecording = false
                        if (audioFile != null && audioFile.exists()) {
                            recordedFile = audioFile
                            recordingStatus = "✅ Recording stopped successfully!"
                        } else {
                            recordingStatus = "❌ Error saving file."
                        }
                    }
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(text = if (isRecording) "⏹️ Stop Recording" else "🎙️ Start Voice Recording")
            }

            // Appears only if there's a ready recording and not currently recording
            if (recordedFile != null && !isRecording) {
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (!isPlaying) {
                            try {
                                isPlaying = true
                                recordingStatus = "🔊 Playing recording now..."

                                // Initializing Android MediaPlayer
                                val mediaPlayer = MediaPlayer().apply {
                                    setDataSource(recordedFile!!.absolutePath) // Loading the recorded file
                                    prepare()
                                    start() // Starting playback on speaker!

                                    // Listener that detects when the song/recording ends naturally
                                    setOnCompletionListener {
                                        isPlaying = false
                                        recordingStatus = "✅ Playback finished."
                                        release() // Releasing MediaPlayer from memory
                                    }
                                }
                            } catch (e: Exception) {
                                isPlaying = false
                                recordingStatus = "❌ Playback error: ${e.message}"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isPlaying, // Prevents double clicks while playing
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text(text = if (isPlaying) "🎵 Playing..." else "▶️ Play Sound")
                }
            }
        }
    }

    // ==========================================
// 🔔 Demo Screen: Smart Notifications
// ==========================================
    @Preview(showBackground = true)
    @Composable
    fun NotificationsDemoScreen(onBack: () -> Unit = {}) {
        val permissionManager = composablePermissionManager()
        val isPermissionGranted = permissionManager.notifications.isNotificationPermissionGranted()
        var notificationStatusText by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back"
                    )
                }
                Text(text = "Smart Push Notifications", style = MaterialTheme.typography.titleLarge)
            }

            notificationStatusText =
                if (isPermissionGranted) "✅ Permission granted!" else "Permission denied"
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Status: $notificationStatusText", style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {permissionManager.notifications.requestNotificationPermission { isGranted ->
                        notificationStatusText = if (isGranted == PermissionStatus.ACCESS_GRANTED) {
                            "✅ Permission granted! Sending notification..."
                        } else {
                            "❌ Notification permission denied."
                        }
                    }
                }, modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Request notification permission")
            }
            Button(
                onClick = {
                    permissionManager.notifications.showSimpleNotification(
                        title = "It works! 🎉",
                        message = "Notification sent successfully from your new library."
                    )
                }, modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Send test notification")
            }
        }
    }


    // ==========================================
// 📹 Demo Screen: Recording & Built-in Video Player
// ==========================================
    @Composable
    fun VideoDemoScreen(onBack: () -> Unit = {}) {
        val permissionManager = composablePermissionManager()
        var videoStatus by remember { mutableStateOf("Status: Waiting") }
        var capturedVideoUri by remember { mutableStateOf<Uri?>(null) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back"
                    )
                }
                Text(text = "Video Recording & Player", style = MaterialTheme.typography.titleLarge)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(text = videoStatus, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))

            // Built-in video player: If there's a recorded video, we'll display and play it here in a loop!
            capturedVideoUri?.let { uri ->
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { ctx ->
                        android.widget.VideoView(ctx).apply {
                            setVideoURI(uri)
                            // Adds control buttons (Play, Pause, Timeline) to the video
                            setMediaController(android.widget.MediaController(ctx))

                            // Listener that restarts the video when it ends (Loop)
                            setOnPreparedListener { mediaPlayer ->
                                mediaPlayer.isLooping = true
                                start()
                            }
                        }
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    permissionManager.video.requestVideoPermission { isGranted ->
                        if (isGranted == PermissionStatus.ACCESS_GRANTED) {
                            videoStatus = "✅ Permissions approved! Opening video..."

                            // Activate your library's smart video component!
                            permissionManager.video.recordVideo(onSuccess = { uri ->
                                if (uri != null) {
                                    capturedVideoUri = uri
                                    videoStatus = "✅ Video recorded and loaded into the player!"
                                } else {
                                    videoStatus = "❌ Video recording canceled."
                                }
                            }, onFailure = { exception ->
                                videoStatus = "❌ Error: ${exception.message}"
                            })
                        } else {
                            videoStatus =
                                "❌ Camera and microphone must be approved to record video."
                        }
                    }
                }, modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(text = "📹 Record New Video")
            }
        }
    }
}





