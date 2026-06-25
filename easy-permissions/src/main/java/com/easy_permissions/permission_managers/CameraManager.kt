package com.easy_permissions.permission_managers

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.easy_permissions.EasyPermission
import com.easy_permissions.permission_enums.PermissionStatus
import com.easy_permissions.permission_enums.PermissionType

private const val TAG = "CameraFragmentTag"
class EasyCameraManager(private val context: Context, private val activity: FragmentActivity?) {

    /**
     * Quick check if camera permission is granted
     */
    fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            PermissionType.CAMERA.toManifestString()
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestCameraPermission(onResult: (PermissionStatus) -> Unit){
        EasyPermission.request(activity!!, PermissionType.CAMERA.toManifestString()){ isGranted ->
            onResult(isGranted)
        }
    }

    /**
     * Opens the camera and returns a Bitmap in a callback
     */
    fun takePicture(onSuccess: (Bitmap?) -> Unit, onFailure: (Exception) -> Unit) {
        if (!isCameraPermissionGranted()) {
            onFailure(SecurityException("Camera permission is missing! Please request it first."))
            return
        }

        val activity = activity ?: run {
            onFailure(IllegalStateException("Activity is required for camera capture"))
            return
        }

        val fragmentManager = activity.supportFragmentManager

        // Creating a dedicated temporary fragment to manage image capture
        val cameraFragment = CameraResultFragment()
        cameraFragment.setCallback(onSuccess)

        fragmentManager.beginTransaction()
            .add(cameraFragment, TAG)
            .commitNowAllowingStateLoss()
    }
}

/**
 * Invisible internal fragment that manages the image capture Launcher
 */
internal class CameraResultFragment : Fragment() {

    private var callback: ((Bitmap?) -> Unit)? = null

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        callback?.invoke(bitmap)
        // Self-destruction of the fragment upon completion
        parentFragmentManager.beginTransaction().remove(this).commitAllowingStateLoss()
    }

    fun setCallback(onSuccess: (Bitmap?) -> Unit) {
        this.callback = onSuccess
    }

    override fun onStart() {
        super.onStart()
        // Launching the camera as soon as the fragment is ready
        takePictureLauncher.launch(null)
    }
}
