package fr.geonature.maps.sample.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import fr.geonature.maps.sample.R
import fr.geonature.maps.settings.MapSettings
import fr.geonature.maps.ui.MapFragment
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay

/**
 * custom Map fragment.
 *
 * @author S. Grimault
 * @see MapFragment
 */
class CustomMapFragment : MapFragment() {

    private var bottomSheetBehavior: BottomSheetBehavior<ViewGroup>? = null
    private var bottomSheetContent: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onConfigureBottomSheetListener = { parent, bottomSheetBehavior ->
            bottomSheetBehavior.isDraggable = false
            bottomSheetBehavior.isFitToContents = true
            parent.isVisible = true
            parent.layoutParams = parent.layoutParams.apply { height = LayoutParams.WRAP_CONTENT }

            parent.addView(LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.list_item_1,
                    parent,
                    false
                )
                .apply {
                    this@CustomMapFragment.bottomSheetContent = (this as TextView)
                })

            this.bottomSheetBehavior = bottomSheetBehavior
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        mapView.overlays.add(MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                bottomSheetContent?.text = p?.toIntString()
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED

                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        }))
        mapView.addMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
                return true
            }

            override fun onZoom(event: ZoomEvent?): Boolean {
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
                return true
            }
        })
    }

    companion object {

        /**
         * Use this factory method to create a new instance of [CustomMapFragment].
         *
         * @return A new instance of [CustomMapFragment]
         */
        @JvmStatic
        fun newInstance(mapSettings: MapSettings) = CustomMapFragment().apply {
            arguments = Bundle().apply {
                putParcelable(
                    ARG_MAP_SETTINGS,
                    mapSettings
                )
            }
        }
    }
}