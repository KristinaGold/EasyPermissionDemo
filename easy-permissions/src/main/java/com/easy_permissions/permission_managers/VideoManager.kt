package com.easy_permissions.permission_managers

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.easy_permissions.EasyPermission
import com.easy_permissions.permission_enums.PermissionStatus
import com.easy_permissions.permission_enums.PermissionType

private const val TAG = "VideoFragmentTag"

class EasyVideoManager(private val activity: FragmentActivity?, private val context: Context) {

    /**
     * Quick check if the developer has video permission
     */
    fun isVideoPermissionGranted(): Boolean {
        val cameraPermission =
            ContextCompat.checkSelfPermission(context, PermissionType.CAMERA.toManifestString())
        val micPermission =
            ContextCompat.checkSelfPermission(context, PermissionType.MICROPHONE.toManifestString())
        return cameraPermission == PackageManager.PERMISSION_GRANTED && micPermission == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Requests both camera and microphone permissions simultaneously.
     * Evaluates the results and returns TRUE if both of them are granted.
     */
    fun requestVideoPermission(onResult: (PermissionStatus) -> Unit) {
        if (!isVideoPermissionGranted()) {
            val videoPermissions = listOf(
                PermissionType.CAMERA.toManifestString(),
                PermissionType.MICROPHONE.toManifestString()
            )
            EasyPermission.requestMultiple(activity!!, videoPermissions) { results ->
                val cameraGranted =
                    results[PermissionType.CAMERA.toManifestString()] ?: false
                val micGranted =
                    results[PermissionType.MICROPHONE.toManifestString()] ?: false
                if (cameraGranted && micGranted) {
                    onResult(PermissionStatus.ACCESS_GRANTED)
                } else {
                    onResult(PermissionStatus.ACCESS_DENIED_BY_USER)
                }
            }
        } else {
            onResult(PermissionStatus.ACCESS_GRANTED)
        }
    }


    /**
     * Captures a video using the system camera application and returns its Uri
     */
    fun recordVideo(onSuccess: (Uri?) -> Unit, onFailure: (Exception) -> Unit) {
        val fragmentManager = activity!!.supportFragmentManager
        val videoFragment = VideoResultFragment()
        videoFragment.setCallback(onSuccess)

        fragmentManager.beginTransaction()
            .add(videoFragment, TAG)
            .commitNowAllowingStateLoss()
    }
}

/**
 * Internal transparent fragment to capture the activity result for video recording
 */
internal class VideoResultFragment : Fragment() {

    private var callback: ((Uri?) -> Unit)? = null

    // Using the built-in contract to record a video and save it to a transient Uri
    private val recordVideoLauncher = registerForActivityResult(
        ActivityResultContracts.CaptureVideo()
    ) { success: Boolean ->
        // Note: For simplicity in the demo, we rely on the OS provided video Uri or predefined temp files
        // In this implementation, we can wrap the intent or return the state back
        // To keep it clean and robust, we pass the generic handling back
    }

    // We can also use standard custom intent contract for direct video path generation
    private val recordVideoCustomLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val videoUri: Uri? = result.data?.data
        callback?.invoke(videoUri)
        parentFragmentManager.beginTransaction().remove(this).commitAllowingStateLoss()
    }

    fun setCallback(onSuccess: (Uri?) -> Unit) {
        this.callback = onSuccess
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        recordVideoCustomLauncher.launch(intent)
    }
}