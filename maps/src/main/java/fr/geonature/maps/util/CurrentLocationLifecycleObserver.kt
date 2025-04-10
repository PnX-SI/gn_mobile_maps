package fr.geonature.maps.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * A lifecycle observer that manages retrieving the device's current location.
 *
 * This class handles location permissions, requests single location update, and provides the location
 * result through `getCurrentLocation()`. It ties into the lifecycle of a [LifecycleOwner], automatically
 * registering and unregistering itself as an observer.
 *
 * **Example usage**
 * * Within a `Fragment`:
 * ```kotlin
 * class MyFragment : Fragment() {
 *   private var currentLocationLifecycleObserver: CurrentLocationLifecycleObserver? = null
 *   // ...
 *   override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *     super.onViewCreated(view, savedInstanceState)
 *     // ...
 *     activity?.also {
 *       currentLocationLifecycleObserver = CurrentLocationLifecycleObserver(
 *         it,
 *         this@MyFragment.viewLifecycleOwner,
 *         it.activityResultRegistry
 *       )
 *     }
 *     //...
 *     lifecycleScope.launch {
 *       val currentLocation = currentLocationLifecycleObserver?.getCurrentLocation()
 *       Logger.info { "current location: $currentLocation" }
 *     }
 *   }
 * }
 * ```
 *
 * @property context The application context.
 * @property lifecycleOwner The [LifecycleOwner] to observe.
 * @property registry The registry to register the permission request launcher.
 *
 * @author S. Grimault
 */
class CurrentLocationLifecycleObserver(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val registry: ActivityResultRegistry
) : DefaultLifecycleObserver {

    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var requestPermissionLauncher: ActivityResultLauncher<Array<String>>? = null
    private var locationContinuation: CancellableContinuation<Location?>? = null
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            locationManager.removeUpdates(this)
            locationContinuation?.resumeWith(Result.success(location))
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(
            provider: String?,
            status: Int,
            extras: Bundle?
        ) {
        }

        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }
    private val onReady = MutableLiveData<Boolean>()

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        requestPermissionLauncher = registry.register(
            REGISTRY_KEY,
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            handlePermissionResult(permissions)
        }
        onReady.postValue(true)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)

        locationManager.removeUpdates(locationListener)
        locationContinuation?.cancel()
        requestPermissionLauncher = null
    }

    /**
     * Retrieves the current device location.
     *
     * This function checks for location permissions. If permissions are granted, it initiates a
     * single location update request.
     * If permissions are not granted, it launches a permission request dialog. The result of the
     * permission request and subsequent location updates are handled asynchronously through
     * coroutines and continuations.
     *
     * @return the current device location as [Location]. The coroutine will suspend until the
     * location is available or an error occurs.
     * @throws SecurityException if location permissions are denied and the permission request fails.
     */
    suspend fun getCurrentLocation() = suspendCancellableCoroutine { continuation ->
        locationContinuation = continuation

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onReady.observeUntil(
                lifecycleOwner,
                { it == true }) {
                requestPermissionLauncher?.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        } else {
            requestLocationUpdate()
        }

        continuation.invokeOnCancellation {
            locationContinuation = null
        }
    }

    private fun handlePermissionResult(permissions: Map<String, Boolean>) {
        when {
            permissions.getOrElse(Manifest.permission.ACCESS_FINE_LOCATION) { false } || permissions.getOrElse(Manifest.permission.ACCESS_COARSE_LOCATION) { false } -> {
                // precise location access granted
                requestLocationUpdate()
            }

            else -> {
                // no location access granted
                locationContinuation?.resume(null)
            }
        }
    }

    private fun requestLocationUpdate() {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> {
                requestCurrentLocation(LocationManager.GPS_PROVIDER)
            }

            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> {
                requestCurrentLocation(LocationManager.NETWORK_PROVIDER)
            }

            else -> {
                // no provider available
                locationContinuation?.resume(null)
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun requestCurrentLocation(provider: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            locationManager.getCurrentLocation(
                provider,
                null,
                context.mainExecutor
            ) {
                locationContinuation?.resumeWith(Result.success(it))
            }
        } else {
            locationManager.requestSingleUpdate(
                provider,
                locationListener,
                null
            )
        }
    }

    companion object {
        private const val REGISTRY_KEY = "current_location_observer"
    }
}