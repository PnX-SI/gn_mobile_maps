package fr.geonature.maps.ui.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.location.Location
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fr.geonature.maps.R
import fr.geonature.maps.ui.overlay.MyLocationListener
import fr.geonature.maps.ui.overlay.MyLocationOverlay
import fr.geonature.maps.util.ThemeUtils
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
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class MyLocationButton(
    context: Context,
    attrs: AttributeSet
) : FloatingActionButton(
    context,
    attrs
), MapListener {
    private var myLocationOverlay: MyLocationOverlay? = null
    private var myLocationState: MyLocationState = MyLocationState.INACTIVE

    init {
        setImageDrawable(context.getDrawable(R.drawable.ic_gps_location_searching))
    }

    override fun onScroll(event: ScrollEvent?): Boolean {
        val mapView = event?.source ?: return true
        if (mapView.isAnimating) return true

        if (myLocationState == MyLocationState.ACTIVE_TRACKER) {
            val drawable = context.getDrawable(R.drawable.ic_gps_location_found)
            drawable?.setTint(Color.DKGRAY)
            setImageDrawable(drawable)

            myLocationState = MyLocationState.ACTIVE
        }

        return true
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
        return true
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState() ?: return null
        val ss = SavedState(superState)

        ss.isMyLocationActive = myLocationState != MyLocationState.INACTIVE

        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)

        myLocationState =
            if (state.isMyLocationActive) MyLocationState.ACTIVE else MyLocationState.INACTIVE

        onResume()
    }

    fun setMapView(
        mapView: MapView,
        maxBounds: BoundingBox? = null
    ) {
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

                val drawable = context.getDrawable(R.drawable.ic_gps_location_found)

                myLocationState =
                    if (myLocationState == MyLocationState.ACTIVE || myLocationState == MyLocationState.ACTIVE_TRACKER) {
                        mapView.controller.animateTo(GeoPoint(location))
                        drawable?.setTint(ThemeUtils.getAccentColor(context))
                        MyLocationState.ACTIVE_TRACKER
                    }
                    else {
                        drawable?.setTint(Color.DKGRAY)
                        MyLocationState.ACTIVE
                    }

                setImageDrawable(drawable)
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
            val myLocationOverlay = myLocationOverlay ?: return@setOnClickListener

            if (myLocationOverlay.isEnabled) {
                if (myLocationState == MyLocationState.ACTIVE_TRACKER) {
                    disableMyLocation()
                }
                else {
                    mapView.controller.animateTo(GeoPoint(myLocationOverlay.getLastKnownLocation()))

                    val drawable = context.getDrawable(R.drawable.ic_gps_location_found)
                    drawable?.setTint(ThemeUtils.getAccentColor(context))
                    setImageDrawable(drawable)

                    myLocationState = MyLocationState.ACTIVE_TRACKER
                }
            }
            else {
                enableMyLocation()
            }
        }

        isEnabled = true
    }

    fun onResume() {
        if (myLocationState == MyLocationState.ACTIVE) enableMyLocation() else disableMyLocation()
    }

    private fun enableMyLocation() {
        setImageResource(R.drawable.ic_gps_location_searching)
        val drawable = drawable as AnimationDrawable?

        val myLocationOverlay = myLocationOverlay ?: return

        drawable?.start()
        myLocationState =
            if (myLocationOverlay.enableMyLocation()) MyLocationState.ACTIVE else MyLocationState.INACTIVE
    }

    private fun disableMyLocation() {
        setImageResource(R.drawable.ic_gps_location_searching)
        val drawable = drawable as AnimationDrawable?
        drawable?.stop()

        val myLocationOverlay = myLocationOverlay ?: return

        myLocationOverlay.disableMyLocation()
        myLocationState = MyLocationState.INACTIVE
    }

    internal enum class MyLocationState {
        INACTIVE, ACTIVE, ACTIVE_TRACKER
    }

    internal class SavedState : BaseSavedState {
        var isMyLocationActive: Boolean = false

        constructor(superState: Parcelable) : super(superState)

        private constructor(source: Parcel) : super(source) {
            this.isMyLocationActive =
                source.readByte() == Integer.valueOf(1).toByte() // as boolean value
        }

        override fun writeToParcel(
            out: Parcel,
            flags: Int
        ) {
            super.writeToParcel(
                out,
                flags
            )

            out.writeByte((if (isMyLocationActive) 1 else 0).toByte()) // as boolean value
        }

        companion object {

            @Suppress("unused")
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState {
                    return SavedState(source)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
}