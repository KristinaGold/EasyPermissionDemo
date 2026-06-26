package com.easy_permissions

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.easy_permissions.permission_enums.PermissionStatus
import com.easy_permissions.permission_fragment.PermissionFragment

object EasyPermission {

    private const val TAG = "EasyPermissionFragment"

    /**
     * Requests a single permission. If denied permanently, automatically
     * redirects the user to the system app settings screen.
     */
    internal fun request(
        activity: FragmentActivity,
        permission: String,
        onResult: (PermissionStatus) -> Unit
    ) {
        val fragment = getOrCreateFragment(activity)

        fragment.launchPermission(permission) { isGranted ->
            if (isGranted) {
                onResult(PermissionStatus.ACCESS_GRANTED)
            } else {
                // Check if the user blocked the permission permanently ("Don't ask again")
                val isPermanentlyDenied = !shouldShowRationale(activity, permission)

                if (isPermanentlyDenied) {
                    onResult(PermissionStatus.ACCESS_DENIED_OPENING_PHONE_SETTINGS)
                    showSettingsChoiceDialog(activity, onResult)
                } else {
                    onResult(PermissionStatus.ACCESS_DENIED_BY_USER)
                }
            }
            fragment.cleanUp()
        }
    }


    /**
     * Requests all permissions first.
     * Only AFTER the user interacts with all available system dialogs,
     * it evaluates and displays ONE combined dialog for permanently denied ones.
     */
    internal fun requestMultiple(
        activity: FragmentActivity,
        permissions: List<String>,
        onResult: (Map<String, Boolean>) -> Unit
    ) {
        val finalResults = mutableMapOf<String, Boolean>()
        val fragment = getOrCreateFragment(activity) // Create once only!

        // Run the loop and pass the same original Fragment all the way
        executeSequentialRequests(
            activity,
            fragment,
            permissions,
            0,
            finalResults
        ) { completedMap ->

            // 1. Final cleanup of the Fragment from the screen - only now when everything is finished!
            fragment.cleanUp()

            // 2. Return the full result to the developer
            onResult(completedMap)

            // 3. Collecting everything that is permanently blocked
            val permanentlyDeniedList = completedMap.entries
                .filter { (permission, isGranted) ->
                    !isGranted && !shouldShowRationale(activity, permission)
                }
                .map { (permission, _) -> permission }

            // 4. Displaying a centralized dialog
            if (permanentlyDeniedList.isNotEmpty()) {
                val cleanPermissionNames = permanentlyDeniedList.map { it.substringAfterLast(".") }
                showSettingsChoiceDialog(activity, cleanPermissionNames) {
                    openAppSettings(activity)
                }
            }
        }
    }

    /**
     * Lifecycle-safe sequential loop
     */
    private fun executeSequentialRequests(
        activity: FragmentActivity,
        fragment: PermissionFragment,
        permissions: List<String>,
        index: Int,
        currentResults: MutableMap<String, Boolean>,
        onSequenceFinished: (Map<String, Boolean>) -> Unit
    ) {
        // Stopping condition: We reached the end of the list
        if (index >= permissions.size) {
            onSequenceFinished(currentResults)
            return
        }

        val currentPermission = permissions[index]

        // Sending through the existing Fragment without recreating fragments
        fragment.launchPermission(currentPermission) { isGranted ->
            currentResults[currentPermission] = isGranted

            // Immediate transition to the next permission
            executeSequentialRequests(
                activity,
                fragment,
                permissions,
                index + 1,
                currentResults,
                onSequenceFinished
            )
        }
    }

// Requesting multiple permissions simultaneously

    internal fun requestMultipleOnce(
        activity: FragmentActivity,
        permissions: List<String>,
        onResult: (Map<String, Boolean>) -> Unit

    ) {
        val fragment = getOrCreateFragment(activity)
        fragment.launchMultiplePermissions(permissions.toTypedArray()) {
            onResult(it)
            fragment.cleanUp()
        }
    }

    /**
     * Dynamic Alert Dialog that builds a custom bullet-point list
     * of all missing permissions for the user.
     */
    internal fun showSettingsChoiceDialog(
        activity: FragmentActivity,
        missingPermissions: List<String>,
        onConfirmSettings: () -> Unit
    ) {
        // Build a clean string list (e.g., "\n• CAMERA\n• RECORD_AUDIO")
        val permissionsListString = missingPermissions.joinToString(separator = "\n") { "• $it" }

        AlertDialog.Builder(activity)
            .setTitle("Permissions Required")
            .setMessage("The following required permissions have been permanently denied:\n\n$permissionsListString\n\nTo use this feature, please enable them in your phone settings.")
            .setPositiveButton("Go to Settings") { dialog, _ ->
                dialog.dismiss()
                onConfirmSettings()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    internal fun shouldShowRationale(activity: FragmentActivity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    /**
     * Internal helper to display a clean choice alert dialog to the user
     */
    private fun showSettingsChoiceDialog(
        activity: FragmentActivity,
        onResult: (PermissionStatus) -> Unit
    ) {
        AlertDialog.Builder(activity)
            .setTitle("Permission Required")
            .setMessage("This permission has been permanently denied. To use this feature, please enable it in your phone settings.")
            .setPositiveButton("Go to Settings") { dialog, _ ->
                dialog.dismiss()
                onResult(PermissionStatus.ACCESS_DENIED_OPENING_PHONE_SETTINGS)
                openAppSettings(activity) // Go to settings
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                onResult(PermissionStatus.ACCESS_DENIED_BY_USER) // Stay in app
            }
            .setCancelable(false) // Prevents clearing the dialog by clicking outside
            .show()
    }

    /**
     * Directs the user directly to the settings screen of the current app
     */
    internal fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    private fun getOrCreateFragment(activity: FragmentActivity): PermissionFragment {
        val fragmentManager = activity.supportFragmentManager
        var fragment = fragmentManager.findFragmentByTag(TAG) as? PermissionFragment

        if (fragment == null) {
            fragment = PermissionFragment()
            fragmentManager.beginTransaction()
                .add(fragment, TAG)
                .commitAllowingStateLoss()
        }
        return fragment
    }
}