package com.easy_permissions.permission_managers

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.easy_permissions.EasyPermission
import com.easy_permissions.permission_enums.PermissionStatus
import com.easy_permissions.permission_enums.PermissionType

private const val TAG = "StorageFragmentTag"

class EasyStorageManager(private val context: Context, private val activity: FragmentActivity?) {

    /**
     * Checks whether the relevant media/storage permission is currently granted on the device.
     */
    fun isStoragePermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            PermissionType.IMAGES_AND_GALLERY.toManifestString()
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestStoragePermissions(onResult: (PermissionStatus) -> Unit) {
        val activity = activity ?: return
        val permissionNeeded = listOf(PermissionType.IMAGES_AND_GALLERY.toManifestString(), PermissionType.VIDEO_AND_GALLERY.toManifestString(), PermissionType.AUDIO_AND_GALLERY.toManifestString())
        EasyPermission.requestMultiple(activity, permissionNeeded) { results ->
            val imagesGranted =
                results[PermissionType.IMAGES_AND_GALLERY.toManifestString()] ?: false
            val videoGranted =
                results[PermissionType.VIDEO_AND_GALLERY.toManifestString()] ?: false
            val audioGranted = results[PermissionType.AUDIO_AND_GALLERY.toManifestString()] ?: false
            if (imagesGranted && videoGranted && audioGranted) {
                onResult(PermissionStatus.ACCESS_GRANTED)
            } else {
                onResult(PermissionStatus.ACCESS_DENIED_BY_USER)
            }

        }
    }

    /**
     * Opens the system file picker for ANY file type.
     * @param mimeType The type of files to filter (e.g., "application/pdf", "image/*", or "*/*" for everything)
     */
    fun pickFile(
        mimeType: String = "*/*",
        onSuccess: (Uri?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        // On Android 13+, the Document Picker doesn't even require runtime permissions!
        // We bypass the permission check for newer devices to provide a seamless experience.
       // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
       //     launchPickerFragment(mimeType, onSuccess)
       // } else {
            // Older devices still strictly require the legacy READ_EXTERNAL_STORAGE permission
            requestStoragePermissions { isGranted ->
                if (isGranted == PermissionStatus.ACCESS_GRANTED) {
                    launchPickerFragment(mimeType, onSuccess)
                } else {
                    onFailure(SecurityException("Storage permission was denied!"))
                }
            }
      //  }
    }

    private fun launchPickerFragment(mimeType: String, onSuccess: (Uri?) -> Unit) {

        val fragmentManager = activity!!.supportFragmentManager
        val storageFragment = GenericStorageResultFragment()
        storageFragment.setMimeType(mimeType)
        storageFragment.setCallback(onSuccess)

        fragmentManager.beginTransaction()
            .add(storageFragment, TAG)
            .commitNowAllowingStateLoss()
    }
}

/**
 * Internal transparent fragment managing the modern generic document picker launcher
 */
internal class GenericStorageResultFragment : Fragment() {

    private var callback: ((Uri?) -> Unit)? = null
    private var targetMimeType: String = "*/*"

    // Using GetContent contract which invokes the native System File Documents Provider
    private val pickFileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        callback?.invoke(uri)
        parentFragmentManager.beginTransaction().remove(this).commitAllowingStateLoss()
    }

    fun setCallback(onSuccess: (Uri?) -> Unit) {
        this.callback = onSuccess
    }

    fun setMimeType(mimeType: String) {
        this.targetMimeType = mimeType
    }

    override fun onStart() {
        super.onStart()
        // Launches the picker with the dynamic filter type
        pickFileLauncher.launch(targetMimeType)
    }
}

