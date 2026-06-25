package com.easy_permissions.permission_fragment

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

internal class PermissionFragment : Fragment() {

    // Callback for a single permission
    private var singleCallback: ((Boolean) -> Unit)? = null

    // Callback for multiple permissions - returns a map of permissions and their status
    private var multipleCallback: ((Map<String, Boolean>) -> Unit)? = null

    // Request single permission
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        singleCallback?.invoke(isGranted)
    }

    // Request multiple permissions
    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultsMap ->
        multipleCallback?.invoke(resultsMap)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    // Launch single permission
    fun launchPermission(permission: String, onResult: (Boolean) -> Unit) {
        this.singleCallback = onResult
        requestPermissionLauncher.launch(permission)
    }

    // Launch multiple permissions
    fun launchMultiplePermissions(permissions: Array<String>, onResult: (Map<String, Boolean>) -> Unit) {
        this.multipleCallback = onResult
        requestMultiplePermissionsLauncher.launch(permissions)
    }


    /**
     * 🔥 Will be triggered manually only when the library finishes the entire request cycle
     */
    fun cleanUp() {
        if (isAdded && !parentFragmentManager.isStateSaved) {
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commitAllowingStateLoss()
        }
    }
}