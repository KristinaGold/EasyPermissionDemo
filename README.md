# 🛡️ EasyPermission Toolbox

An all-in-one, production-ready Android permission management ecosystem. It abstracts the boilerplate code of Android permissions and provides **smart sub-managers** that fully handle the feature lifecycle (Camera capture, Video recording, Audio recording/playback, Document Pickers, and High-Priority Push Notifications).

Fully compatible with both **Jetpack Compose** and traditional **XML Views**.

---

## ✨ Features

* **⚡ Zero-Manifest Configuration:** All necessary permissions are automatically injected into the consumer application via Manifest Merger.
* **🧩 Clean Architecture:** Uses a centralized `PermissionType` Enum, hiding version-specific permission discrepancies (e.g., Android 13+ granular media splitting).
* **📸 Smart Camera Component:** Instantly capture high-quality images and receive a ready-to-use `Bitmap`.
* **📹 Audio & Video Studio:** Easily record videos (auto-managed with system intent and custom loop players) and capture audio with full playback configurations.
* **📁 Granular Storage Manager:** Modern document/file picker support allowing fully customizable MIME-type filtering (e.g., selecting *only* PDFs or generic files).
* **🔔 Heads-Up Push Notifications:** Automatically provisions channels and pushes high-priority overlay notifications out of the box.
* **📍 Location & Navigation Utilities:** Seamlessly fetch location context, calculate distances, and launch system maps with one line of code.

---

## 💻 Installation

Add the JitPack repository to your root `settings.gradle.kts` file:

```
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependency to your app-level `build.gradle.kts`:

```
dependencies {
	        implementation("com.github.KristinaGold:EasyPermissionDemo:1.0.8")
	}
```
---

## 🚀 Usage Guide
Initialize the manager once in your screen/activity context:

### Jetpack Compose Integration

Our library features built-in architecture-safe lifecycle guards (`DefaultLifecycleObserver`). This means you can safely instantiate and trigger permission sequences even during early lifecycle stages (like `onCreate`) inside your Compose components without causing race conditions or missing callbacks.

Since the underlying framework hooks into Android's `supportFragmentManager` using a headless setup, your host activity must extend `FragmentActivity` (or `AppCompatActivity`).

### 1. Update your Hosting Activity
Ensure your primary application window manager extends `FragmentActivity` instead of the standard naked `ComponentActivity`:

```kotlin
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyMainComposeApp()
        }
    }
}
```

### 2. Add the Composable Extension Utility
   Drop this standard, state-remembering extension helper into your project. It cleanly extracts the active context root and wraps your manager across UI recompositions:

```kotlin
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.fragment.app.FragmentActivity
import com.easy_permissions.EasyPermissionManager

/**
 * A highly optimized Composable constructor that safely instantiates and remembers 
 * the EasyPermissionManager instance across UI recompositions.
 */
@Composable
fun composePermissionManager(): EasyPermissionManager {
    val context = LocalContext.current
    val isInspectionMode = LocalInspectionMode.current

    // Safely extract the hosting FragmentActivity out of the Composable Context tree
    val activity = remember(context) {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is FragmentActivity) break
            currentContext = currentContext.baseContext
        }
        currentContext as? FragmentActivity
    }

    if (activity == null && !isInspectionMode) {
        throw IllegalStateException("EasyPermission requires your Activity to extend FragmentActivity")
    }

    return remember(context, activity) {
        EasyPermissionManager(context, activity)
    }
}
```

### 3. Usage Example inside a Composable Screen
   Now you can fire single or multiple permission gates smoothly within any click handler or side-effect block:
```kotlin
@Composable
fun CameraFeatureScreen() {
    // Instantiate the toolkit seamlessly
    val permissionManager = composePermissionManager()

    Button(onClick = {
        // Request specific hardware permissions with instant functional callbacks
        permissionManager.camera.requestCameraPermission { status ->
            when (status) {
                PermissionStatus.ACCESS_GRANTED -> {
                    // 📸 Camera available! Fire your viewfinders and secure capture pipelines
                }
                PermissionStatus.ACCESS_DENIED -> {
                    // Handle soft denial gracefully
                }
            }
        }
    }) {
        Text("Launch Secure Camera Channel")
    }
}
```


### Traditional XML Views Integration

For traditional Android View layouts (XML), utilizing the library is straightforward. Thanks to the built-in lifecycle-safety layers, you can initialize and invoke permission gates inside `onCreate()` or within UI listeners without tracking fragment attachments manually.

### Prerequisites
Your hosting Activity must extend `FragmentActivity` (or `AppCompatActivity`) to allow the underlying headless engine to safely bridge communication hooks.

### Usage Example
```kotlin
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.easy_permissions.EasyPermissionManager
import com.easy_permissions.permission_enums.PermissionStatus

class MainActivity : FragmentActivity() {

    private lateinit var permissionManager: EasyPermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Initialize the manager instance
        permissionManager = EasyPermissionManager(this, this)

        // 2. Safe execution directly inside onCreate()
        permissionManager.camera.requestCameraPermission { status ->
            if (status == PermissionStatus.ACCESS_GRANTED) {
                // 📸 Camera access cleared! Open your custom viewfinders smoothly
            } else {
                // Handle denial state gracefully
            }
        }
    }
}
```

## 🧩 Architectural Concepts & Results
When launching a request via this library, the developer receives a clean, comprehensive EasyPermissionResult callback containing one of three explicit states:

`ACCESS_GRANTED`: The feature is fully authorized and ready to execute.

`ACCESS_DENIED_BY_USER`: The user soft-denied the request (1st or 2nd prompt).

`ACCESS_DENIED_OPENING_PHONE_SETTINGS`: The user has permanently disabled this feature. The library automatically prompts a customized alert dialog and routes the user directly to the system settings screen.
## 🛠️ Specialized Sub-Managers API Reference

### 📸 Camera Component (`permissionManager.camera`)
Handles camera hardware access and delivers native Image Bitmaps instantly without dealing with manual intents.

- `isCameraPermissionGranted(): Boolean` Checks if the camera permission is granted.

- `requestCameraPermission(onResult: (PermissionStatus) -> Unit)` Launches the specific camera permission pipeline.

- `takePicture(onSuccess: (Bitmap?) -> Unit, onFailure: (Exception) -> Unit)` Launches the device camera capturing interface and returns a high-quality Bitmap.


### 📹 Video Studio Component (`permissionManager.video`)
Deals with system video hardware and handles local loops safely.

- `isVideoPermissionGranted(): Boolean` Checks if both camera and microphone permissions are granted.

- `requestVideoPermission(onResult: (PermissionStatus) -> Unit)` Requests necessary camera and hardware storage layers required for video recording.

- `recordVideo(onSuccess: (Uri?) -> Unit, onFailure: (Exception) -> Unit)` Triggers system intent video recorders and returns the local file pointer path (Uri).


### 🎙️ Audio Recorder Component (`permissionManager.audio`)
A specialized playground for microphone hardware interactions, supporting seamless background audio streaming and recordings.

- `isAudioPermissionGranted(): Boolean` Checks if the microphone permission is granted.

- `requestAudioPermission(onResult: (PermissionStatus) -> Unit)` Launches the specific microphone permission pipeline.

- `startRecording(outputFileUri: Uri, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)` Initiates media recorder components targeting a local destination path.

- `stopRecording(onSuccess: (Uri) -> Unit)` Gracefully tears down microphone locks and saves the target tracking output.


### 📁 Granular Storage Component (`permissionManager.storage`)
Modern file access abstraction supporting strict MIME-type isolation filters (bypassing Android 13 media split bugs).

- `isStoragePermissionGranted(): Boolean` Checks if the storage permission is granted.

- `requestStoragePermissions(onResult: (PermissionStatus) -> Unit)` Requests scoped media/storage permissions based on the active operating system level.

- `pickFile(mimeType: String, onSuccess: (Uri?) -> Unit, onFailure: (Exception) -> Unit)` Launches a modern visual document picker. Accepts specific filters (e.g., "application/pdf", "image/*").


### 📍 Location & Navigation Component (`permissionManager.location`)
Implements smart OR-logic natively behind the scenes. Access is considered granted if either Coarse (Approximate) or Fine (Exact) location layers are approved by the user.

- `isLocationPermissionGranted(): Boolean` Checks if the location permission is granted.

- `requestLocationPermission(onResult: (PermissionStatus) -> Unit)` Simultaneously executes safe batch requests for both exact and approximate coordinates.

- `getCurrentLocation(onSuccess: (Location?, Double, Double, String) -> Unit, onFailure: (Exception) -> Unit)` Pulls fresh spatial coordinates and actual address from system hardware nodes.

- `calculateDistanceInMeters(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): Float` Mathematical helper function: calculation of distance on a geographic plane between two points (in meters).

- `calculateDistanceInKm(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): Float` Distance calculation in kilometers (developer convenience).

- `getCoordinatesFromAddress(address: String): Pair<Double, Double>?` Returns coordinates from a human-readable address.

- `navigateToLocation(lat: Double, lng: Double)` Opens Google Maps navigation to a specific location.


### 🔔 Notification Component (`permissionManager.notifications`)
Manages channel provisionings and heads-up overlay constraints (crucial for Android 13+ devices).

- `isNotificationPermissionGranted(): Boolean` Checks if the notification permission is granted.

- `requestNotificationPermission(onResult: (PermissionStatus) -> Unit)` Launches runtime notification prompts on API 33+ devices.

- `showSimpleNotification(title: String, message: String, channelId: String)` Dispatches immediate, high-priority system overlay banners.

## ⚡ Generic Permission Pipeline
If you need to request standard or custom Android system permissions that do not belong to the specialized components above (e.g., READ_CONTACTS, BLUETOOTH), use the library's Sequential Generic Framework.

This component requests permissions individually in memory, which completely prevents Android's native bug where one blocked permission prematurely crashes a combined batch dialog.

### Single Generic Request

```kotlin
permissionManager.request(Manifest.permission.READ_CONTACTS) { result ->
    when (result) {
        EasyPermissionResult.ACCESS_GRANTED -> { /* Access contacts safely */ }
        EasyPermissionResult.ACCESS_DENIED_BY_USER -> { /* Handle soft decline */ }
        EasyPermissionResult.ACCESS_DENIED_OPENING_PHONE_SETTINGS -> { /* Dialog shown automatically */ }
    }
}
```
### Multiple Generic Request
Pass a list of raw manifest string constants. The library will prompt all active system dialogs sequentially, and render one single unified alert dialog at the very end summarizing any permanently blocked elements.

```kotlin
val genericList = listOf(
    Manifest.permission.READ_CONTACTS,
    Manifest.permission.BODY_SENSORS,
    Manifest.permission.CALL_PHONE
)

permissionManager.requestMultiple(genericList) { totalResultsMap ->
    totalResultsMap.forEach { (permissionString, isGranted) ->
        Log.d("EasyPermission", "$permissionString status: $isGranted")
    }
}
```

## 📜 License
MIT License

Copyright (c) 2025

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE. 







