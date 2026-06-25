package com.easy_permissions.permission_managers

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.easy_permissions.EasyPermission
import com.easy_permissions.EasyPermission.openAppSettings
import com.easy_permissions.EasyPermission.shouldShowRationale
import com.easy_permissions.EasyPermission.showSettingsChoiceDialog
import com.easy_permissions.permission_enums.PermissionStatus
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale

class LocationManager(private val activity: FragmentActivity?, private val context: Context) {

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(
            context
        )
    }

    /**
     * Quick check if the developer has location permission (network or GPS)
     */
    fun isLocationPermissionGranted(): Boolean {
        val fineLocation =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocation =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fineLocation == PackageManager.PERMISSION_GRANTED || coarseLocation == PackageManager.PERMISSION_GRANTED
    }
    /**
     * Requests both Fine and Coarse location permissions simultaneously.
     * Evaluates the results and returns TRUE if at least one of them is granted.
     */
    fun requestLocationPermission(onResult: (PermissionStatus) -> Unit) {
        val locationPermissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        // 2. Use your multiple request function
        EasyPermission.requestMultipleOnce(activity!!, locationPermissions) { resultsMap ->
            val fineGranted = resultsMap[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseGranted = resultsMap[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            // 3. OR logic: If at least one of them is granted (Fine or Coarse) - Access is granted!
            if (fineGranted || coarseGranted) {
                onResult(PermissionStatus.ACCESS_GRANTED)
            } else {
                // 4. If the user denied both, we check if it's a permanent block to trigger the dialog
                val fineRationale = shouldShowRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                val coarseRationale = shouldShowRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION)

                // If both are permanently blocked (both Rationales return false)
                if (!fineRationale && !coarseRationale) {
                    // Display the dynamic dialog built in the previous step and redirect to settings
                    showSettingsChoiceDialog(activity, locationPermissions) {
                        openAppSettings(activity)
                    }
                    onResult(PermissionStatus.ACCESS_DENIED_OPENING_PHONE_SETTINGS)
                } else {
                    // The user simply clicked Deny normally for the first or second time
                    onResult(PermissionStatus.ACCESS_DENIED_BY_USER)
                }
            }
        }
    }

    /**
     * Fetch the device's current location (Latitude & Longitude)
     * Uses Priority.PRIORITY_HIGH_ACCURACY to get the most accurate location from GPS
     */
    @SuppressLint("MissingPermission") // We handle this with our check
    fun getCurrentLocation(
        onSuccess: (Location?, Double, Double, String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (!isLocationPermissionGranted()) {
            onFailure(SecurityException("Location permission is missing! Please request it first."))
            return
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location: Location? ->
                val address = getAddressFromCoordinates(location!!.latitude, location.longitude)
                onSuccess(location, location.latitude, location.longitude, address)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    /**
     * Mathematical helper function: calculation of distance on a geographic plane between two points (in meters)
     * Uses the Haversine formula built into Android
     */
    fun calculateDistanceInMeters(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): Float {
        val startLocation = Location("start").apply {
            latitude = startLat
            longitude = startLng
        }
        val endLocation = Location("end").apply {
            latitude = endLat
            longitude = endLng
        }
        return startLocation.distanceTo(endLocation) // Returns distance in meters
    }

    /**
     * Distance calculation in kilometers (developer convenience)
     */
    fun calculateDistanceInKm(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): Float {
        return calculateDistanceInMeters(startLat, startLng, endLat, endLng) / 1000
    }

    private fun getAddressFromCoordinates(lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            addresses?.get(0)?.getAddressLine(0) ?: "Unknown Location"
        } catch (e: Throwable) {
            "Unknown Location"
        }
    }

    fun getCoordinatesFromAddress(address: String): Pair<Double, Double>? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(address, 1)
            if (!addresses.isNullOrEmpty()) {
                Pair(addresses[0].latitude, addresses[0].longitude)
            } else null
        } catch (e: Throwable) {
            null
        }
    }

    fun navigateToLocation(lat: Double, lng: Double) {
        val gmmIntentUri = Uri.parse("google.navigation:q=$lat,$lng")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            setPackage("com.google.android.apps.maps")
        }

        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            val genericIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$lat,$lng")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(genericIntent)
        }
    }
}