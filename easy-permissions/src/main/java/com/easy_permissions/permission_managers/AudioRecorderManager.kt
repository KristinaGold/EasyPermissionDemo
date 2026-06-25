package com.easy_permissions.permission_managers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.easy_permissions.EasyPermission
import com.easy_permissions.permission_enums.PermissionStatus
import com.easy_permissions.permission_enums.PermissionType
import java.io.File
import java.io.IOException

class EasyAudioRecorder(private val context: Context, private val activity: FragmentActivity?) {

    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null

    /**
     * Quick check if microphone permission is granted
     */
    fun isAudioPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            PermissionType.MICROPHONE.toManifestString()
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestAudioPermission(onResult: (PermissionStatus) -> Unit) {
        EasyPermission.request(
            activity!!,
            PermissionType.MICROPHONE.toManifestString()
        ) { isGranted ->
            onResult(isGranted)

        }
    }

    /**
     * Start voice recording
     * The function creates an internal temporary file and records directly to it
     */
    fun startRecording(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        if (!isAudioPermissionGranted()) {
            onFailure(SecurityException("Audio recording permission is missing! Please request RECORD_AUDIO first."))
            return
        }

        try {
            // Creating a temporary file in .mp3 (or .3gp depending on encoding) format in the app's internal cache directory
            currentOutputFile = File.createTempFile("easy_audio_", ".3gp", context.cacheDir)

            // Initializing MediaRecorder based on the Android version
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(currentOutputFile?.absolutePath)
                prepare()
                start()
            }
            onSuccess()
        } catch (e: IOException) {
            onFailure(e)
        } catch (e: IllegalStateException) {
            onFailure(e)
        }
    }

    /**
     * Stop recording and return the ready file
     * @return The File containing the recorded sound
     */
    fun stopRecording(): File? {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaRecorder = null
        }
        return currentOutputFile
    }
}