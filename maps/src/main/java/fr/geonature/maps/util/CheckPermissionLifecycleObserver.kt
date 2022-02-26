package fr.geonature.maps.util

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import org.tinylog.kotlin.Logger
import kotlin.coroutines.resume

/**
 * Checks if the requested permission has been granted to this application.
 * If not, ask this permission.
 *
 * @author S. Grimault
 */
class CheckPermissionLifecycleObserver(
    activity: ComponentActivity,
    private val permission: String
) :
    DefaultLifecycleObserver {

    private val registry: ActivityResultRegistry = activity.activityResultRegistry

    init {
        activity.lifecycle.addObserver(this)
    }

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var permissionContinuation: CancellableContinuation<Boolean>? = null

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        requestPermissionLauncher = registry.register(
            "read_ask_permission",
            owner,
            ActivityResultContracts.RequestPermission()
        ) { result ->
            Logger.info { if (result) "request permission '${permission}' granted" else "request permission '${permission}' not allowed" }

            permissionContinuation?.resumeWith(Result.success(result))
        }
    }

    /**
     * Checks if the requested permission has been granted to this application.
     * If not, ask this permission.
     *
     * @return `true` if the permission has been granted
     */
    suspend operator fun invoke(context: Context) =
        suspendCancellableCoroutine<Boolean> { continuation ->
            // already granted
            if (PermissionUtils.checkSelfPermissions(
                    context,
                    permission
                )
            ) {
                Logger.info { "permission '$permission' already granted" }

                continuation.resume(true)
                return@suspendCancellableCoroutine
            }

            Logger.info { "ask permission '$permission'..." }

            permissionContinuation = continuation
            requestPermissionLauncher.launch(permission)

            continuation.invokeOnCancellation {
                permissionContinuation = null
            }
        }
}