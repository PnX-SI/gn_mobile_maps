package fr.geonature.maps.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.appcompat.view.ActionMode
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import fr.geonature.maps.R
import fr.geonature.maps.util.DrawableUtils
import fr.geonature.maps.util.ThemeUtils
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.util.*

/**
 * Edit feature (POI) on the map.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class EditFeatureButton(
    context: Context,
    attrs: AttributeSet
) : FloatingActionButton(
    context,
    attrs
) {
    private var listener: OnEditFeatureButtonListener? = null
    private val pois = HashMap<String, GeoPoint>()
    private var selectedPoi: String? = null

    private var actionMode: ActionMode? = null
    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(
            mode: ActionMode?,
            menu: Menu?
        ): Boolean {
            mode?.menuInflater?.inflate(
                R.menu.map_action_mode,
                menu
            )

            return true
        }

        override fun onPrepareActionMode(
            mode: ActionMode?,
            menu: Menu?
        ): Boolean {
            return false
        }

        override fun onActionItemClicked(
            mode: ActionMode?,
            item: MenuItem?
        ): Boolean {
            return when (item?.itemId) {
                R.id.action_poi_delete -> {
                    val mapView = listener?.getMapView() ?: return true

                    val geoPoint = pois.remove(selectedPoi)
                    val selectedMarker =
                        mapView.overlays?.find { it is Marker && it.id == selectedPoi } as Marker?
                    deselectMarker(selectedMarker)
                    selectedMarker?.remove(mapView)
                    showSnackbarAboutDeletedPoi(geoPoint)

                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            actionMode = null

            val mapView = listener?.getMapView() ?: return
            val selectedMarker =
                mapView.overlays?.find { it is Marker && it.id == selectedPoi } as Marker?

            deselectMarker(selectedMarker)
        }
    }

    private val mapEventReceiver = object : MapEventsReceiver {
        override fun longPressHelper(p: GeoPoint?): Boolean {
            // nothing to do...
            return false
        }

        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
            val mapView = listener?.getMapView() ?: return true
            val selectedMarker =
                mapView.overlays?.find { it is Marker && it.id == selectedPoi } as Marker?

            deselectMarker(selectedMarker)

            return true
        }
    }

    init {
        setImageDrawable(context.getDrawable(R.drawable.ic_add_poi))
        setOnClickListener { addPoi() }
    }

    fun setListener(listener: OnEditFeatureButtonListener) {
        this.listener = listener

        val mapView = this.listener?.getMapView() ?: return

        val overlayEvents = MapEventsOverlay(mapEventReceiver)
        mapView.overlays.add(overlayEvents)
    }

    fun getPois(): List<GeoPoint> {
        return pois.values.toList()
    }

    private fun addPoi(geoPoint: GeoPoint? = null) {
        val context = context ?: return
        val mapView = listener?.getMapView() ?: return

        val poiMarker = Marker(mapView)
        poiMarker.id = UUID.randomUUID().toString()
        poiMarker.position = geoPoint ?: mapView.mapCenter as GeoPoint
        poiMarker.setAnchor(
            Marker.ANCHOR_CENTER,
            Marker.ANCHOR_BOTTOM
        )
        setMarkerIcon(
            poiMarker,
            ThemeUtils.getPrimaryColor(context),
            2.0f
        )
        poiMarker.isDraggable = true
        poiMarker.infoWindow = null
        poiMarker.setOnMarkerClickListener { marker, _ ->
            if (selectedPoi !== marker.id) {
                selectedPoi = marker.id
                selectMarker(marker)
            }

            true
        }
        poiMarker.setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
            override fun onMarkerDragEnd(marker: Marker?) {
                if (marker == null) return

                marker.alpha = 1.0f
                deselectMarker(marker)

                pois[marker.id] = marker.position
            }

            override fun onMarkerDragStart(marker: Marker?) {
                marker?.alpha = 0.5f
                selectMarker(marker)
            }

            override fun onMarkerDrag(marker: Marker?) {
                // nothing to do...
            }
        })

        mapView.overlays.add(poiMarker)
        pois[poiMarker.id] = poiMarker.position
    }

    private fun setMarkerIcon(
        marker: Marker?,
        @ColorInt
        tintColor: Int,
        scale: Float = 1.0f
    ) {
        val context = context ?: return

        marker?.icon = DrawableUtils.createScaledDrawable(
            context,
            R.drawable.ic_poi,
            tintColor,
            scale
        )
    }

    private fun selectMarker(marker: Marker?) {
        if (!isOrWillBeHidden) hide()

        val context = context ?: return

        if (actionMode == null) {
            actionMode = listener?.startActionMode(actionModeCallback)
            actionMode?.setTitle(R.string.action_title_poi_edit)
        }

        setMarkerIcon(
            marker,
            ThemeUtils.getAccentColor(context),
            2.5f
        )
    }

    private fun deselectMarker(marker: Marker?) {
        if (!isOrWillBeShown) show()
        selectedPoi = null
        actionMode?.finish()

        val context = context ?: return

        setMarkerIcon(
            marker,
            ThemeUtils.getPrimaryColor(context),
            2.0f
        )
    }

    private fun showSnackbarAboutDeletedPoi(geoPoint: GeoPoint?) {
        if (geoPoint == null) return

        listener?.makeSnackbar(
            R.string.action_poi_deleted,
            Snackbar.LENGTH_SHORT
        )?.setAction(
            R.string.action_undo
        ) {
            addPoi(geoPoint)
        }?.show()
    }

    interface OnEditFeatureButtonListener {
        fun getMapView(): MapView
        fun startActionMode(callback: ActionMode.Callback): ActionMode?
        fun makeSnackbar(
            @StringRes
            resId: Int, duration: Int
        ): Snackbar?
    }
}