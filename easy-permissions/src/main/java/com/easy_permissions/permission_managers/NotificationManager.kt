package com.easy_permissions.permission_managers

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.easy_permissions.EasyPermission
import com.easy_permissions.R
import com.easy_permissions.permission_enums.PermissionStatus

class NotificationManager(private val context: Context, private val activity: FragmentActivity?) {

    private val CHANNEL_ID = "easy_permission_channel"
    private val CHANNEL_NAME = "EasyPermission Notifications"

    init {
        // Creating a Notification Channel - Mandatory from Android 8 (Oreo)
        createNotificationChannel()
    }

    /**
     * Checking if notification permission is granted.
     * On older devices (Android 12 and below), this will always return true because the permission is granted by default.
     */
    fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun requestNotificationPermission(onResult: (PermissionStatus) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            EasyPermission.request(activity!!, Manifest.permission.POST_NOTIFICATIONS){
                isGranted -> onResult(isGranted)
            }
        }
        else onResult(PermissionStatus.ACCESS_GRANTED)
    }

    /**
     * Sending an immediate notification to the screen!
     */
    @SuppressLint("MissingPermission")
    fun showSimpleNotification(title: String, message: String) {
        if (!isNotificationPermissionGranted()) return

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        with(NotificationManagerCompat.from(context)) {
            // Additional permission check for compiler compliance
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                notify(System.currentTimeMillis().toInt(), builder.build())
            }
        }
    }

    private fun createNotificationChannel() {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
            description = "Channel for EasyPermission Demo App"
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.createNotificationChannel(channel)
    }
}