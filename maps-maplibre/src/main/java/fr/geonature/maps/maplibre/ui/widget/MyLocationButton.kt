package fr.geonature.maps.maplibre.ui.widget

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.BundleCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fr.geonature.maps.maplibre.R
import fr.geonature.maps.maplibre.ui.overlay.MyLocationListener
import fr.geonature.maps.maplibre.ui.overlay.MyLocationOverlay
import fr.geonature.maps.maplibre.util.ThemeUtils
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style

/**
 * Shows the current user location on the map and centers the map on it.
 *
 * Handled states (see [MyLocationState]):
 * - [MyLocationState.DISABLED]: `ACCESS_FINE_LOCATION` is not granted, the button is disabled;
 * - [MyLocationState.STOPPED]: no location marker;
 * - [MyLocationState.ONCE]: the last known location is resolved once and shown;
 * - [MyLocationState.CONTINUOUS]: the location is resolved continuously and the map follows it
 * (the icon is blinking).
 *
 * Each click cycles through [MyLocationState.STOPPED], [MyLocationState.ONCE] and
 * [MyLocationState.CONTINUOUS]. Moving the map by hand stops following the location.
 *
 * The missing permission is requested through [OnMyLocationButtonListener.checkPermissions]
 * when the button is bound to a map, the result being given back to [onPermissionResult].
 *
 * @author S. Grimault
 */
class MyLocationButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FloatingActionButton(
    context,
    attrs
) {

    private var listener: OnMyLocationButtonListener? = null
    private var map: MapLibreMap? = null
    private var overlay: MyLocationOverlay? = null
    private var restored = false
    private var initialCenterDone = false

    internal var myLocationState: MyLocationState = MyLocationState.DISABLED
        private set

    private val cameraMoveStartedListener =
        MapLibreMap.OnCameraMoveStartedListener { reason ->
            if (reason == MapLibreMap.OnCameraMoveStartedListener.REASON_API_GESTURE && myLocationState == MyLocationState.CONTINUOUS) {
                // the user takes the control of the map: stop following the location
                overlay?.stopUpdates()
                setState(MyLocationState.ONCE)
            }
        }

    init {
        contentDescription = context.getString(R.string.button_my_location_hint)
        isEnabled = false
        applyState()

        setOnClickListener { onClick() }
    }

    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable(
                STATE_SUPER,
                super.onSaveInstanceState()
            )
            putString(
                STATE_LOCATION,
                myLocationState.name
            )
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            restored = true
            state.getString(STATE_LOCATION)
                ?.let { name -> MyLocationState.entries.firstOrNull { it.name == name } }
                ?.also { myLocationState = it }

            super.onRestoreInstanceState(
                BundleCompat.getParcelable(
                    state,
                    STATE_SUPER,
                    Parcelable::class.java
                )
            )

            return
        }

        super.onRestoreInstanceState(state)
    }

    /**
     * Binds this button to the given map: the location overlay is created and the missing
     * `ACCESS_FINE_LOCATION` permission is requested.
     */
    fun setListener(listener: OnMyLocationButtonListener) {
        release()

        this.listener = listener

        val map = listener.getMap()
        this.map = map
        map.addOnCameraMoveStartedListener(cameraMoveStartedListener)

        overlay = MyLocationOverlay(
            context,
            listener.getMaxBounds()
        ).apply {
            setMyLocationListener(object : MyLocationListener {
                override fun onLocationChanged(location: Location) {
                    if (myLocationState == MyLocationState.CONTINUOUS) centerOn(location)
                }

                override fun onLocationOutsideBoundaries(location: Location) {
                    stopAndHide()

                    Toast.makeText(
                        context,
                        R.string.toast_location_outside_map_boundaries,
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            })
        }

        if (hasPermission()) {
            onPermissionGranted()
        } else {
            setState(MyLocationState.DISABLED)
            listener.checkPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    /**
     * Must be called once the style of the map is loaded (or reloaded).
     */
    fun onStyleLoaded(style: Style) {
        overlay?.onStyleLoaded(style)

        if (!hasPermission()) return

        when (myLocationState) {
            MyLocationState.ONCE -> overlay?.showLastKnownLocation()
            MyLocationState.CONTINUOUS -> overlay?.startUpdates()
            else -> Unit
        }
    }

    /**
     * Gives back the result of the `ACCESS_FINE_LOCATION` permission request.
     */
    fun onPermissionResult(granted: Boolean) {
        if (granted) onPermissionGranted() else onPermissionDenied()
    }

    fun onResume() {
        if (listener == null) return

        if (!hasPermission()) {
            onPermissionDenied()

            return
        }

        when (myLocationState) {
            MyLocationState.DISABLED -> onPermissionGranted()
            MyLocationState.CONTINUOUS -> startContinuous()
            else -> Unit
        }
    }

    fun onPause() {
        if (myLocationState == MyLocationState.CONTINUOUS) overlay?.stopUpdates()
    }

    fun onDestroy() {
        release()
    }

    private fun release() {
        map?.removeOnCameraMoveStartedListener(cameraMoveStartedListener)
        map = null
        overlay?.onDestroy()
        overlay = null
        listener = null
        initialCenterDone = false
    }

    private fun hasPermission() = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun isLocationServiceEnabled(): Boolean {
        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false

        return runCatching {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }.getOrDefault(false)
    }

    private fun onPermissionGranted() {
        if (myLocationState == MyLocationState.DISABLED) setState(MyLocationState.STOPPED)

        if (!initialCenterDone && !restored) {
            initialCenterDone = true

            // at startup, center the map on the last known location, if any
            showOnce(silent = true)
        } else {
            initialCenterDone = true
        }

        if (myLocationState == MyLocationState.CONTINUOUS) startContinuous()
    }

    private fun onPermissionDenied() {
        overlay?.hide()
        setState(MyLocationState.DISABLED)
    }

    private fun onClick() {
        if (!isEnabled) return

        when (myLocationState) {
            MyLocationState.DISABLED -> Unit
            MyLocationState.STOPPED -> showOnce()
            MyLocationState.ONCE -> {
                if (isLocationServiceEnabled()) startContinuous() else showLocationDisabled()
            }

            MyLocationState.CONTINUOUS -> stopAndHide()
        }
    }

    private fun showOnce(silent: Boolean = false) {
        val overlay = overlay ?: return
        val location = overlay.showLastKnownLocation()

        if (location == null) {
            setState(MyLocationState.STOPPED)

            if (!silent) {
                Toast.makeText(
                    context,
                    if (isLocationServiceEnabled()) R.string.toast_location_unavailable else R.string.toast_location_disabled,
                    Toast.LENGTH_LONG
                )
                    .show()
            }

            return
        }

        setState(MyLocationState.ONCE)
        centerOn(location)
    }

    private fun startContinuous() {
        val overlay = overlay ?: return

        if (!overlay.startUpdates()) {
            overlay.hide()
            setState(MyLocationState.STOPPED)
            showLocationDisabled()

            return
        }

        setState(MyLocationState.CONTINUOUS)
        overlay.getLastKnownLocation()?.also { centerOn(it) }
    }

    private fun stopAndHide() {
        overlay?.hide()
        setState(MyLocationState.STOPPED)
    }

    private fun showLocationDisabled() {
        Toast.makeText(
            context,
            R.string.toast_location_disabled,
            Toast.LENGTH_LONG
        )
            .show()
    }

    private fun centerOn(location: Location) {
        val map = map ?: return

        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    location.latitude,
                    location.longitude
                ),
                map.cameraPosition.zoom.coerceAtLeast(MIN_ZOOM_ON_LOCATION)
            )
        )
    }

    private fun setState(state: MyLocationState) {
        myLocationState = state
        applyState()
    }

    private fun applyState() {
        isEnabled = myLocationState != MyLocationState.DISABLED
        alpha = if (isEnabled) 1f else 0.5f

        when (myLocationState) {
            MyLocationState.DISABLED -> setIcon(
                R.drawable.ic_gps_location_off,
                Color.DKGRAY
            )

            MyLocationState.STOPPED -> setIcon(
                R.drawable.ic_gps_location_none,
                Color.DKGRAY
            )

            MyLocationState.ONCE -> setIcon(
                R.drawable.ic_gps_location_found,
                Color.DKGRAY
            )

            MyLocationState.CONTINUOUS -> {
                setIcon(
                    R.drawable.ic_gps_location_searching,
                    ThemeUtils.getAccentColor(context)
                )
                (drawable as? AnimationDrawable)?.start()
            }
        }
    }

    private fun setIcon(
        drawableResId: Int,
        color: Int
    ) {
        setImageDrawable(ContextCompat.getDrawable(
            context,
            drawableResId
        )?.mutate())
        imageTintList = ColorStateList.valueOf(color)
    }

    interface OnMyLocationButtonListener {
        fun getMap(): MapLibreMap
        fun getMaxBounds(): LatLngBounds?

        /**
         * Requests the given permission: the result must be given back to
         * [onPermissionResult].
         */
        fun checkPermissions(permission: String)
    }

    internal enum class MyLocationState {
        DISABLED,
        STOPPED,
        ONCE,
        CONTINUOUS
    }

    companion object {
        private const val STATE_SUPER = "superState"
        private const val STATE_LOCATION = "locationState"
        private const val MIN_ZOOM_ON_LOCATION = 16.0
    }
}
