package fr.geonature.maps.ui.widget

import android.Manifest
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.location.Location
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.BundleCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fr.geonature.maps.R
import fr.geonature.maps.ui.overlay.MyLocationListener
import fr.geonature.maps.ui.overlay.MyLocationOverlay
import fr.geonature.maps.util.ThemeUtils
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider

/**
 * Show or hide current user location overlay on the map.
 *
 * @author S. Grimault
 */
class MyLocationButton(
    context: Context,
    attrs: AttributeSet
) : FloatingActionButton(
    context,
    attrs
), MapListener {
    private var onMyLocationButtonListener: OnMyLocationButtonListener? = null
    private var myLocationOverlay: MyLocationOverlay? = null
    private var myLocationState: MyLocationState = MyLocationState.INACTIVE

    init {
        setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.ic_gps_location_searching
            )
        )
    }

    override fun onScroll(event: ScrollEvent?): Boolean {
        val mapView = event?.source ?: return true
        if (mapView.isAnimating) return true

        if (myLocationState == MyLocationState.ACTIVE_TRACKER) {
            val drawable = ContextCompat.getDrawable(
                context,
                R.drawable.ic_gps_location_found
            )
            drawable?.setTint(Color.DKGRAY)
            setImageDrawable(drawable)
            imageTintList = ColorStateList.valueOf(Color.DKGRAY)

            myLocationState = MyLocationState.ACTIVE
        }

        return true
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
        return true
    }

    override fun onSaveInstanceState(): Parcelable {
        return Bundle().apply {
            putParcelable(
                "superState",
                super.onSaveInstanceState()
            )
            putByte(
                "locationState",
                (if (myLocationState != MyLocationState.INACTIVE) 1 else 0).toByte()
            )
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            myLocationState = if (state.getByte("locationState") == Integer.valueOf(1)
                    .toByte()
            ) MyLocationState.ACTIVE else MyLocationState.INACTIVE
            super.onRestoreInstanceState(
                BundleCompat.getParcelable(
                    state,
                    "superState",
                    Parcelable::class.java
                )
            )

            return
        }

        super.onRestoreInstanceState(state)

        onResume()
    }

    fun setListener(listener: OnMyLocationButtonListener) {
        this.onMyLocationButtonListener = listener

        val mapView = listener.getMapView()
        val maxBounds = listener.getMaxBounds()

        // configure my location overlay
        myLocationOverlay = MyLocationOverlay(
            mapView,
            maxBounds
        )
        mapView.overlays.add(myLocationOverlay)
        mapView.addMapListener(this)

        myLocationOverlay?.setMyLocationListener(object : MyLocationListener {
            override fun onLocationChanged(
                location: Location?,
                source: IMyLocationProvider?
            ) {
                val context = context ?: return
                if (location == null) return

                val drawable = ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_gps_location_found
                )

                myLocationState = if (myLocationState == MyLocationState.ACTIVE_TRACKER) {
                    animateTo(
                        mapView,
                        location
                    )
                    drawable?.setTint(ThemeUtils.getAccentColor(context))
                    setImageDrawable(drawable)
                    imageTintList = ColorStateList.valueOf(ThemeUtils.getAccentColor(context))
                    MyLocationState.ACTIVE_TRACKER
                } else {
                    drawable?.setTint(Color.DKGRAY)
                    setImageDrawable(drawable)
                    imageTintList = ColorStateList.valueOf(Color.DKGRAY)
                    MyLocationState.ACTIVE
                }
            }

            override fun onLocationOutsideBoundaries(location: Location?) {
                disableMyLocation()

                val context = context ?: return

                Toast.makeText(
                    context,
                    R.string.toast_location_outside_map_boundaries,
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        })

        setOnClickListener {
            onMyLocationButtonListener?.checkPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        isEnabled = true
    }

    fun onResume() {
        if (myLocationState == MyLocationState.ACTIVE || myLocationState == MyLocationState.ACTIVE_TRACKER) enableMyLocation() else disableMyLocation()
    }

    internal fun requestLocation() {
        val myLocationOverlay = myLocationOverlay ?: return
        val mapView = onMyLocationButtonListener?.getMapView() ?: return

        if (myLocationOverlay.isEnabled) {
            if (myLocationState == MyLocationState.ACTIVE_TRACKER) {
                disableMyLocation()
            } else {
                animateTo(
                    mapView,
                    myLocationOverlay.getLastKnownLocation()
                )

                val drawable = ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_gps_location_found
                )
                drawable?.setTint(ThemeUtils.getAccentColor(context))
                setImageDrawable(drawable)
                imageTintList = ColorStateList.valueOf(ThemeUtils.getAccentColor(context))

                myLocationState = MyLocationState.ACTIVE_TRACKER
            }
        } else {
            enableMyLocation(MyLocationState.ACTIVE_TRACKER)
        }
    }

    private fun enableMyLocation(myLocationState: MyLocationState = MyLocationState.ACTIVE) {
        setImageResource(R.drawable.ic_gps_location_searching)
        val drawable = drawable as AnimationDrawable?

        val myLocationOverlay = myLocationOverlay ?: return

        drawable?.start()
        this.myLocationState =
            if (myLocationOverlay.enableMyLocation()) myLocationState else MyLocationState.INACTIVE
    }

    private fun disableMyLocation() {
        setImageResource(R.drawable.ic_gps_location_searching)
        val drawable = drawable as AnimationDrawable?
        drawable?.stop()

        val myLocationOverlay = myLocationOverlay ?: return

        myLocationOverlay.disableMyLocation()
        myLocationState = MyLocationState.INACTIVE
    }

    private fun animateTo(
        mapView: MapView,
        location: Location
    ) {
        mapView.controller.animateTo(
            GeoPoint(location),
            18.0,
            Configuration.getInstance().animationSpeedDefault.toLong()
        )
    }

    interface OnMyLocationButtonListener {
        fun getMapView(): MapView
        fun getMaxBounds(): BoundingBox?
        fun checkPermissions(permission: String)
    }

    internal enum class MyLocationState {
        INACTIVE,
        ACTIVE,
        ACTIVE_TRACKER
    }
}
