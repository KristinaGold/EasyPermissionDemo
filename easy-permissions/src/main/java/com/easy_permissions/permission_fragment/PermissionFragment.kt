package com.easy_permissions.permission_fragment

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.DefaultLifecycleObserver

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
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            requestPermissionLauncher.launch(permission)
        } else {
            lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    requestPermissionLauncher.launch(permission)
                    lifecycle.removeObserver(this)
                }
            })
        }
    }

    // Launch multiple permissions
    fun launchMultiplePermissions(permissions: Array<String>, onResult: (Map<String, Boolean>) -> Unit) {
        this.multipleCallback = onResult
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            requestMultiplePermissionsLauncher.launch(permissions)
        } else {
            lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    requestMultiplePermissionsLauncher.launch(permissions)
                    lifecycle.removeObserver(this)
                }
            })
        }
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