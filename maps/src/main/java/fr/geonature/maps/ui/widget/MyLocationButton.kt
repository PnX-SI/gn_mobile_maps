package fr.geonature.maps.ui.widget

import android.content.Context
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
import org.osmdroid.util.BoundingBox
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
) {
    private var myLocationOverlay: MyLocationOverlay? = null
    private var isMyLocationActive: Boolean = false

    init {
        setImageDrawable(context.getDrawable(R.drawable.ic_gps_location_searching))
        setOnClickListener {
            val myLocationOverlay = myLocationOverlay ?: return@setOnClickListener

            if (myLocationOverlay.isEnabled) disableMyLocation() else enableMyLocation()
        }
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

        myLocationOverlay?.setMyLocationListener(object : MyLocationListener {
            override fun onLocationChanged(
                location: Location?,
                source: IMyLocationProvider?
            ) {
                val context = context ?: return

                val drawable = context.getDrawable(R.drawable.ic_gps_location_found)
                drawable?.setTint(ThemeUtils.getAccentColor(context))
                setImageDrawable(context.getDrawable(R.drawable.ic_gps_location_found))
            }

            override fun onLocationOutsideBoundaries(location: Location?) {
                disableMyLocation()

                val context = context ?: return

                Toast.makeText(
                    context,
                    R.string.toast_location_outside_map_boundaries,
                    Toast.LENGTH_LONG
                ).show()
            }
        })

        isEnabled = true
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState() ?: return null
        val ss = SavedState(superState)

        ss.isMyLocationActive = isMyLocationActive

        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)
        isMyLocationActive = state.isMyLocationActive

        onResume()
    }

    fun onResume() {
        if (isMyLocationActive) enableMyLocation() else disableMyLocation()
    }

    private fun enableMyLocation() {
        setImageResource(R.drawable.ic_gps_location_searching)
        val drawable = drawable as AnimationDrawable?

        val myLocationOverlay = myLocationOverlay ?: return

        drawable?.start()
        isMyLocationActive = myLocationOverlay.enableMyLocation()
    }


    private fun disableMyLocation() {
        setImageResource(R.drawable.ic_gps_location_searching)
        val drawable = drawable as AnimationDrawable?
        drawable?.stop()

        val myLocationOverlay = myLocationOverlay ?: return

        myLocationOverlay.disableMyLocation()
        isMyLocationActive = false
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