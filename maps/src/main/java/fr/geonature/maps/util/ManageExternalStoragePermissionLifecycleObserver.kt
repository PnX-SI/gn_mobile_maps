package fr.geonature.maps.util

import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Checks if this app is allowed to manage all files.
 * If not, launch automatically Intent action `Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION`.
 *
 * @author S. Grimault
 */
@RequiresApi(Build.VERSION_CODES.R)
class ManageExternalStoragePermissionLifecycleObserver(activity: ComponentActivity) :
    DefaultLifecycleObserver {

    private val registry: ActivityResultRegistry = activity.activityResultRegistry

    init {
        activity.lifecycle.addObserver(this)
    }

    private lateinit var startManageExternalStorageResultLauncher: ActivityResultLauncher<Intent>
    private var manageExternalStorageContinuation: CancellableContinuation<Boolean>? = null

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        startManageExternalStorageResultLauncher = registry.register(
            "manage_external_storage",
            owner,
            ActivityResultContracts.StartActivityForResult()
        ) {
            val granted = Environment.isExternalStorageManager()

            Log.i(
                TAG,
                if (granted) "request manage external storage permission granted" else "request manage external storage permission cancelled"
            )

            manageExternalStorageContinuation?.resumeWith(Result.success(granted))
        }
    }

    /**
     * Checks if this app is allowed to manage all files.
     * If not, launch automatically Intent action `Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION`.
     *
     * @return `true` if this app has All Files Access on the primary shared/external storage media.
     */
    suspend operator fun invoke() = suspendCancellableCoroutine<Boolean> { continuation ->
        // already granted to manage all files
        if (Environment.isExternalStorageManager()) {
            continuation.resume(true)
            return@suspendCancellableCoroutine
        }

        Log.i(
            TAG,
            "ask permission to have access to manage all files..."
        )

        manageExternalStorageContinuation = continuation
        startManageExternalStorageResultLauncher.launch(
            Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
        )

        continuation.invokeOnCancellation {
            manageExternalStorageContinuation = null
        }
    }

    companion object {
        private val TAG = ManageExternalStoragePermissionLifecycleObserver::class.java.name
    }
}