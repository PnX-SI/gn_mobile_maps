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
import fr.geonature.maps.util.ThemeUtils.getAccentColor
import fr.geonature.maps.util.ThemeUtils.getPrimaryColor
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
 * - by a long pressing gesture on the map to move the selected [Marker].
 * - by tapping this floating action button
 * - by modifying the [Marker]'s position, using the center of the map as a guide and moving it
 * virtually by moving the map.
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
) {
    private var listener: OnEditFeatureButtonListener? = null
    private val pois = HashMap<String, GeoPoint>()

    private val mapListener = object : MapListener {
        override fun onScroll(event: ScrollEvent?): Boolean {
            return true
        }

        override fun onZoom(event: ZoomEvent?): Boolean {
            if (selectedMarker != null) return true
            if (pois.isNotEmpty() && listener?.getEditMode() == EditMode.SINGLE) return true

            if ((listener?.getMinZoomEditing() ?: 0.0) <= (event?.zoomLevel ?: 0.0)) {
                show()
            } else {
                hide()
            }

            return true
        }
    }
    private var editPoiMapListener: MapListener? = null

    private var selectedMarker: Marker? = null
    private var guideMarker: Marker? = null

    private var actionMode: ActionMode? = null
    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(
            mode: ActionMode?,
            menu: Menu?
        ): Boolean {
            mode?.menuInflater?.inflate(
                R.menu.map_edit_poi_action_mode,
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
                R.id.action_poi_edit -> {
                    val mapView = listener?.getMapView() ?: return true
                    val selectedMarker = selectedMarker ?: return true
                    selectedMarker.alpha = 0.5f
                    selectedMarker.isDraggable = false
                    selectMarker(selectedMarker)

                    guideMarker = createMarker(
                        mapView,
                        mapView.mapCenter as GeoPoint
                    ).also {
                        it.alpha = 0.8f
                        mapView.overlays.add(it)
                        mapView.invalidate()
                    }

                    editPoiMapListener = object : MapListener {
                        override fun onScroll(event: ScrollEvent?): Boolean {
                            guideMarker?.position = mapView.mapCenter as GeoPoint
                            return true
                        }

                        override fun onZoom(event: ZoomEvent?): Boolean {
                            guideMarker?.position = mapView.mapCenter as GeoPoint
                            return true
                        }
                    }.also {
                        mapView.addMapListener(it)
                    }

                    actionMode?.finish()
                    showSnackbarAboutMovingPoi()

                    true
                }

                R.id.action_poi_delete -> {
                    val mapView = listener?.getMapView() ?: return true
                    val selectedMarker = selectedMarker ?: return true

                    val geoPoint = pois.remove(selectedMarker.id)

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

            if (guideMarker == null) clearActiveSelection()
        }
    }

    private val mapEventReceiver = object : MapEventsReceiver {
        override fun longPressHelper(p: GeoPoint?): Boolean {
            if (guideMarker != null) return true

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
            if (guideMarker != null) return true

            clearActiveSelection()

            return true
        }
    }

    init {
        setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.ic_poi_add
            )
        )
        setOnClickListener { addPoi() }
    }

    fun setListener(listener: OnEditFeatureButtonListener) {
        this.listener = listener

        val editMode = listener.getEditMode()

        if (editMode == EditMode.NONE) {
            hide()
            return
        }

        val mapView = listener.getMapView()

        val overlayEvents = MapEventsOverlay(mapEventReceiver)
        mapView.overlays.add(overlayEvents)
        mapView.addMapListener(mapListener)
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
        if (listener?.getEditMode() == EditMode.NONE) return

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
        return selectedMarker?.also { deselectMarker(it) }
    }

    private fun addPoi(geoPoint: GeoPoint? = null) {
        val mapView = listener?.getMapView() ?: return

        val poiMarker = createMarker(
            mapView,
            geoPoint ?: mapView.mapCenter as GeoPoint
        ).also {
            it.isDraggable = true
            it.setOnMarkerClickListener { marker, _ ->
                if (guideMarker != null) return@setOnMarkerClickListener true

                if (selectedMarker?.id !== marker.id) {
                    selectedMarker = marker
                    selectMarker(marker)
                    centerMapToMarker(marker)
                }

                true
            }
            it.setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
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
        }

        mapView.overlays.add(poiMarker)
        mapView.invalidate()
        centerMapToMarker(poiMarker)

        pois[poiMarker.id] = poiMarker.position
        listener?.onSelectedPOIs(getSelectedPOIs())

        if (listener?.getEditMode() == EditMode.SINGLE) {
            hide()
        }
    }

    private fun createMarker(
        mapView: MapView,
        geoPoint: GeoPoint
    ): Marker {
        val poiMarker = Marker(mapView)
        poiMarker.id = UUID.randomUUID()
            .toString()
        poiMarker.position = geoPoint
        poiMarker.setAnchor(
            Marker.ANCHOR_CENTER,
            Marker.ANCHOR_BOTTOM
        )
        setMarkerIcon(
            poiMarker,
            getPrimaryColor(mapView.context),
            2.0f
        )
        poiMarker.infoWindow = null

        return poiMarker
    }

    private fun setMarkerIcon(
        marker: Marker,
        @ColorInt tintColor: Int,
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
            getAccentColor(context),
            2.5f
        )
    }

    private fun deselectMarker(marker: Marker) {
        if (listener?.getEditMode() == EditMode.MULTIPLE) show()

        selectedMarker = null
        actionMode?.finish()

        val context = context ?: return
        val mapView = listener?.getMapView() ?: return

        mapView.minZoomLevel = listener?.getMinZoom() ?: mapView.minZoomLevel

        setMarkerIcon(
            marker,
            getPrimaryColor(context),
            2.0f
        )

        mapView.invalidate()
    }

    private fun centerMapToMarker(marker: Marker) {
        val mapView = listener?.getMapView() ?: return
        val editZoom = if ((listener?.getMinZoomEditing()
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

        if ((listener?.getMinZoomEditing() ?: mapView.zoomLevelDouble) <= mapView.zoomLevelDouble) {
            return false
        }

        listener?.makeSnackbar(
            R.string.snackbar_add_poi_zoom_min,
            Snackbar.LENGTH_SHORT
        )
            ?.show()

        return true
    }

    private fun showSnackbarAboutMovingPoi() {
        listener?.makeSnackbar(
            R.string.action_poi_editing,
            Snackbar.LENGTH_INDEFINITE
        )
            ?.setAction(R.string.action_done) {
                listener?.getMapView()
                    ?.also { mapView ->
                        selectedMarker?.also {
                            it.position = mapView.mapCenter as GeoPoint
                            pois[it.id] = it.position
                        }

                        editPoiMapListener?.also {
                            mapView.removeMapListener(it)
                        }
                        editPoiMapListener = null

                        mapView.invalidate()
                    }
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
                    selectedMarker?.also {
                        it.alpha = 1.0f
                        it.isDraggable = true
                    }

                    // remove the guide marker
                    guideMarker?.also { marker ->
                        listener?.getMapView()
                            ?.also { mapView ->
                                marker.remove(mapView)

                                editPoiMapListener?.also {
                                    mapView.removeMapListener(it)
                                }
                                editPoiMapListener = null

                                mapView.invalidate()
                            }
                    }

                    guideMarker = null
                    clearActiveSelection()

                    if ((pois.isEmpty() && listener?.getEditMode() == EditMode.SINGLE) || listener?.getEditMode() == EditMode.MULTIPLE) {
                        show()
                    }
                }
            })
            ?.show()
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
        fun makeSnackbar(
            @StringRes resId: Int,
            duration: Int
        ): Snackbar?

        fun onSelectedPOIs(pois: List<GeoPoint>)
    }

    enum class EditMode {
        NONE,
        SINGLE,
        MULTIPLE
    }
}
