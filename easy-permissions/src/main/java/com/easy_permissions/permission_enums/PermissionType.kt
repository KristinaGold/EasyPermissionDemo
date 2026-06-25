package com.easy_permissions.permission_enums

import android.Manifest
import android.os.Build

/**
 * A complete, developer-friendly wrapper for all major Android system permissions.
 * This enum hides Android's version-specific complexities and string constants.
 */
enum class PermissionType {
    CAMERA,
    MICROPHONE,
    NOTIFICATIONS,
    LOCATION,
    IMAGES_AND_GALLERY,
    CONTACTS,
    PHYSICAL_ACTIVITY,
    BLUETOOTH, PHONE_STATE,
    CALL_PHONE,
    CALENDAR,
    READ_SMS,
    VIBRATE,
    VIDEO_AND_GALLERY,
    AUDIO_AND_GALLERY,
    BODY_SENSORS;

    /**
     * Automatically maps the friendly enum type to the correct Android Manifest String,
     * taking care of version-specific API changes behind the scenes.
     */
    fun toManifestString(): String {
        return when (this) {
            CAMERA -> Manifest.permission.CAMERA

            MICROPHONE -> Manifest.permission.RECORD_AUDIO

            NOTIFICATIONS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.POST_NOTIFICATIONS
                } else {
                    "" // Automatically granted on Android 12 and below
                }
            }

            LOCATION -> Manifest.permission.ACCESS_FINE_LOCATION

            /**
             * For Android 13 and above: READ_MEDIA_IMAGES
             * For Android 12 and below: READ_EXTERNAL_STORAGE
             */
            IMAGES_AND_GALLERY -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
            }

            CONTACTS -> Manifest.permission.READ_CONTACTS

            PHYSICAL_ACTIVITY -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Manifest.permission.ACTIVITY_RECOGNITION
                } else {
                    "" // Not required as a dangerous permission on older versions
                }
            }

            BLUETOOTH -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // On Android 12+, scanning requires BLUETOOTH_SCAN (used as the primary flag here)
                    Manifest.permission.BLUETOOTH_SCAN
                } else {
                    // On older devices, the classic location permission or legacy bluetooth was enough
                    Manifest.permission.ACCESS_FINE_LOCATION
                }
            }

            VIBRATE -> Manifest.permission.VIBRATE

            PHONE_STATE -> Manifest.permission.READ_PHONE_STATE
            CALL_PHONE -> Manifest.permission.CALL_PHONE
            CALENDAR -> Manifest.permission.READ_CALENDAR
            READ_SMS -> Manifest.permission.READ_SMS
            BODY_SENSORS -> Manifest.permission.BODY_SENSORS

            VIDEO_AND_GALLERY -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_VIDEO
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }

            }
            AUDIO_AND_GALLERY -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_AUDIO
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }

            }
        }
    }
}