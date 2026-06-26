package com.easy_permissions

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.easy_permissions.permission_enums.PermissionStatus
import com.easy_permissions.permission_managers.EasyAudioRecorder
import com.easy_permissions.permission_managers.EasyCameraManager
import com.easy_permissions.permission_managers.EasyStorageManager
import com.easy_permissions.permission_managers.EasyVideoManager
import com.easy_permissions.permission_managers.LocationManager
import com.easy_permissions.permission_managers.NotificationManager


/**
 * FOR BOTH COMPOSE & XML:
 * Core Wrapper Class that exposes all smart functions.
 */
class EasyPermissionManager(
    private val context: Context,
    private val activity: FragmentActivity?
) {
    // Secondary constructor specifically for Compose Preview (Inspection Mode)
    val location by lazy { LocationManager(activity, context) }
    val camera by lazy { EasyCameraManager(context, activity) }
    val storage by lazy { EasyStorageManager(context, activity) }
    val audio by lazy { EasyAudioRecorder(context, activity) }
    val notifications by lazy { NotificationManager(context, activity) }
    val video by lazy { EasyVideoManager(activity , context) }

    fun request(permission: String, onResult: (PermissionStatus) -> Unit) {
        activity?.let {
            EasyPermission.request(it, permission, onResult)
        } ?: onResult(PermissionStatus.ACCESS_DENIED_BY_USER)
    }

    fun requestMultiple(permissions: List<String>, onResult: (Map<String, Boolean>) -> Unit) {
        activity?.let {
            EasyPermission.requestMultiple(it, permissions, onResult)
        } ?: onResult(permissions.associateWith { false })
    }
}
