package fr.geonature.maps.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import fr.geonature.maps.R
import fr.geonature.maps.util.DrawableUtils
import fr.geonature.maps.util.ThemeUtils
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.util.UUID

/**
 * Edit feature (POI) on the map:
 * - by a long pressing gesture on the map
 * - by tapping this floating action button
 *
 * A [Snackbar] may be shown if the current zoom level doesn't meet the minimal editing zoom.
 *
 * @author S. Grimault
 */
class EditFeatureButton(
    context: Context,
    attrs: AttributeSet
) : FloatingActionButton(
    context,
    attrs
), MapListener {
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

                    clearActiveSelection()?.also {
                        it.remove(mapView)
                        mapView.invalidate()
                    }

                    listener?.onSelectedPOIs(getSelectedPOIs())
                    showSnackbarAboutDeletedPoi(geoPoint)

                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            actionMode = null
            clearActiveSelection()
        }
    }

    private val mapEventReceiver = object : MapEventsReceiver {
        override fun longPressHelper(p: GeoPoint?): Boolean {
            if (showSnackbarAboutAddingPoiAndInsufficientZoomLevel(p)) {
                return false
            }

            if (listener?.getEditMode() == EditMode.SINGLE) {
                val mapView = listener?.getMapView() ?: return false

                with(pois) {
                    forEach { poi ->
                        findMarkerOverlay { it.id == poi.key }?.also {
                            deselectMarker(it)
                            it.remove(mapView)
                            mapView.invalidate()
                        }
                    }
                    clear()
                }
            }

            addPoi(p)

            return true
        }

        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
            clearActiveSelection()

            return true
        }
    }

    init {
        setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.ic_add_poi
            )
        )
        setOnClickListener { addPoi() }
    }

    override fun onScroll(event: ScrollEvent?): Boolean {
        return true
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
        if (!selectedPoi.isNullOrBlank()) return true
        if (pois.isNotEmpty() && listener?.getEditMode() == EditMode.SINGLE) return true

        if ((listener?.getMinZoomEditing() ?: 0.0) <= (event?.zoomLevel ?: 0.0)) {
            show()
        } else {
            hide()
        }

        return true
    }

    fun setListener(listener: OnEditFeatureButtonListener) {
        this.listener = listener

        val mapView = this.listener?.getMapView() ?: return

        val overlayEvents = MapEventsOverlay(mapEventReceiver)
        mapView.overlays.add(overlayEvents)

        mapView.addMapListener(this)
    }

    /**
     * Returns the currently added POIs on the map.
     */
    fun getSelectedPOIs(): List<GeoPoint> {
        return pois.values.toList()
    }

    /**
     * Sets POIs on the map.
     * Clear previous selection.
     */
    fun setSelectedPOIs(selectedPois: List<GeoPoint>) {
        val mapView = this.listener?.getMapView() ?: return

        pois.forEach { poi ->
            findMarkerOverlay { overlay -> overlay.id == poi.key }?.also {
                deselectMarker(it)
                it.remove(mapView)
            }
        }
        pois.clear()
        mapView.invalidate()

        selectedPois.forEach {
            addPoi(it)
        }

        mapView.zoomToBoundingBox(
            BoundingBox.fromGeoPoints(selectedPois),
            true
        )
    }

    /**
     * Clear the currently selected POI.
     */
    fun clearActiveSelection(): Marker? {
        return findMarkerOverlay { it.id == selectedPoi }?.also { deselectMarker(it) }
    }

    private fun addPoi(geoPoint: GeoPoint? = null) {
        val context = context ?: return
        val mapView = listener?.getMapView() ?: return

        val poiMarker = Marker(mapView)
        poiMarker.id = UUID.randomUUID()
            .toString()
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
                centerMapToMarker(marker)
            }

            true
        }
        poiMarker.setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
            override fun onMarkerDragEnd(marker: Marker?) {
                if (marker == null) return

                marker.alpha = 1.0f
                deselectMarker(marker)
                centerMapToMarker(marker)

                pois[marker.id] = marker.position
                listener?.onSelectedPOIs(getSelectedPOIs())
            }

            override fun onMarkerDragStart(marker: Marker?) {
                val selectedMarker = marker ?: return

                selectedMarker.alpha = 0.5f
                selectMarker(selectedMarker)
            }

            override fun onMarkerDrag(marker: Marker?) {
                marker?.run {
                    val mapViewForMarker = listener?.getMapView() ?: return

                    if (!mapViewForMarker.isAnimating && !mapViewForMarker.boundingBox.increaseByScale(
                            0.75f
                        )
                            .contains(marker.position)
                    ) {
                        centerMapToMarker(marker)
                    }
                }
            }
        })

        mapView.overlays.add(poiMarker)
        mapView.invalidate()
        centerMapToMarker(poiMarker)

        pois[poiMarker.id] = poiMarker.position
        listener?.onSelectedPOIs(getSelectedPOIs())

        if (listener?.getEditMode() == EditMode.SINGLE) {
            hide()
        }
    }

    private fun setMarkerIcon(
        marker: Marker,
        @ColorInt
        tintColor: Int,
        scale: Float = 1.0f
    ) {
        val context = context ?: return

        marker.icon = DrawableUtils.createScaledDrawable(
            context,
            R.drawable.ic_poi,
            tintColor,
            scale
        )
    }

    private fun selectMarker(marker: Marker) {
        hide()

        val context = context ?: return
        val mapView = listener?.getMapView() ?: return

        if (actionMode == null) {
            actionMode = listener?.startActionMode(actionModeCallback)
            actionMode?.setTitle(R.string.action_title_poi_edit)
        }

        mapView.minZoomLevel = listener?.getMinZoomEditing() ?: mapView.zoomLevelDouble

        setMarkerIcon(
            marker,
            ThemeUtils.getAccentColor(context),
            2.5f
        )
    }

    private fun deselectMarker(marker: Marker) {
        if (listener?.getEditMode() == EditMode.MULTIPLE) {
            show()
        }

        selectedPoi = null
        actionMode?.finish()

        val context = context ?: return
        val mapView = listener?.getMapView() ?: return

        mapView.minZoomLevel = listener?.getMinZoom() ?: mapView.minZoomLevel

        setMarkerIcon(
            marker,
            ThemeUtils.getPrimaryColor(context),
            2.0f
        )
    }

    private fun centerMapToMarker(marker: Marker) {
        val mapView = listener?.getMapView() ?: return
        val editZoom =
            if ((listener?.getMinZoomEditing()
                    ?: mapView.zoomLevelDouble) <= mapView.zoomLevelDouble
            ) mapView.zoomLevelDouble
            else listener?.getMinZoomEditing() ?: mapView.zoomLevelDouble

        animateTo(
            mapView,
            marker.position,
            editZoom
        )
    }

    private fun showSnackbarAboutAddingPoiAndInsufficientZoomLevel(geoPoint: GeoPoint?): Boolean {
        val mapView = listener?.getMapView() ?: return false
        if (geoPoint == null) return false

        if ((listener?.getMinZoomEditing()
                ?: mapView.zoomLevelDouble) <= mapView.zoomLevelDouble
        ) {
            return false
        }

        listener?.makeSnackbar(
            R.string.snackbar_add_poi_zoom_min,
            Snackbar.LENGTH_SHORT
        )
            ?.show()

        return true
    }

    private fun showSnackbarAboutDeletedPoi(geoPoint: GeoPoint?) {
        if (geoPoint == null) return

        listener?.makeSnackbar(
            R.string.action_poi_deleted,
            Snackbar.LENGTH_SHORT
        )
            ?.setAction(R.string.action_undo) {
                addPoi(geoPoint)
            }
            ?.addCallback(object : Snackbar.Callback() {
                override fun onShown(sb: Snackbar?) {
                    super.onShown(sb)
                    hide()
                }

                override fun onDismissed(
                    transientBottomBar: Snackbar?,
                    event: Int
                ) {
                    if ((pois.isEmpty() && listener?.getEditMode() == EditMode.SINGLE) || listener?.getEditMode() == EditMode.MULTIPLE) {
                        show()
                    }
                }
            })
            ?.show()
    }

    private fun findMarkerOverlay(filter: (overlay: Marker) -> Boolean): Marker? {
        val mapView = listener?.getMapView() ?: return null

        return mapView.overlays.asSequence()
            .filterNotNull()
            .filter { it is Marker }
            .map { it as Marker }
            .find(filter)
    }

    private fun animateTo(
        mapView: MapView,
        point: GeoPoint,
        zoom: Double
    ) {
        mapView.controller.animateTo(
            point,
            zoom,
            Configuration.getInstance().animationSpeedDefault.toLong()
        )
    }

    interface OnEditFeatureButtonListener {
        fun getMapView(): MapView
        fun getEditMode(): EditMode
        fun getMinZoom(): Double
        fun getMinZoomEditing(): Double
        fun startActionMode(callback: ActionMode.Callback): ActionMode?
        fun makeSnackbar(@StringRes resId: Int, duration: Int): Snackbar?
        fun onSelectedPOIs(pois: List<GeoPoint>)
    }

    enum class EditMode {
        SINGLE,
        MULTIPLE
    }
}
