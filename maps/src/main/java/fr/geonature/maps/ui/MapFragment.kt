package fr.geonature.maps.ui

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import fr.geonature.maps.BuildConfig
import fr.geonature.maps.R
import fr.geonature.maps.settings.MapSettings
import fr.geonature.maps.util.DrawableUtils.createScaledDrawable
import fr.geonature.maps.util.ThemeUtils.getAccentColor
import fr.geonature.maps.util.ThemeUtils.getPrimaryColor
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.modules.OfflineTileProvider
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import java.io.File
import java.util.*
import kotlin.collections.HashMap

/**
 * Simple [Fragment] embedding a [MapView] instance.
 *
 * Activities that contain this fragment must implement the [MapFragment.OnMapFragmentListener]
 * interface to handle interaction events.
 *
 * Use the [MapFragment.newInstance] factory method to create an instance of this fragment.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class MapFragment : Fragment() {

    private var listener: OnMapFragmentListener? = null
    private var container: View? = null
    private var mapView: MapView? = null
    private var fab: FloatingActionButton? = null
    private val pois = HashMap<String, GeoPoint>()
    private var selectedPoi: String? = null

    private var actionMode: ActionMode? = null
    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?,
                                        menu: Menu?): Boolean {
            mode?.menuInflater?.inflate(
                R.menu.map_action_mode,
                menu)

            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?,
                                         menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?,
                                         item: MenuItem?): Boolean {
            return when (item?.itemId) {
                R.id.action_poi_delete -> {
                    val geoPoint = pois.remove(selectedPoi)
                    val selectedMarker =
                        mapView?.overlays?.find { it is Marker && it.id == selectedPoi } as Marker?
                    deselectMarker(selectedMarker)
                    selectedMarker?.remove(mapView)
                    showSnackbarForDeletedPoi(geoPoint)

                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            actionMode = null

            val selectedMarker =
                mapView?.overlays?.find { it is Marker && it.id == selectedPoi } as Marker?

            deselectMarker(selectedMarker)
        }
    }

    private val mapEventReceiver = object : MapEventsReceiver {
        override fun longPressHelper(p: GeoPoint?): Boolean {
            // nothing to do...
            return false
        }

        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
            val selectedMarker =
                mapView?.overlays?.find { it is Marker && it.id == selectedPoi } as Marker?

            deselectMarker(selectedMarker)

            return true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = context ?: return

        Configuration.getInstance()
            .load(
                context,
                PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance()
            .isDebugMode = BuildConfig.DEBUG
        Configuration.getInstance()
            .isDebugTileProviders = BuildConfig.DEBUG
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(
            R.layout.fragment_map,
            container,
            false)

        this.container = view.findViewById(android.R.id.content)
        this.mapView = view.findViewById(R.id.map)
        this.fab = view.findViewById(R.id.fab)
        this.fab?.setOnClickListener { addPoi() }

        configureMapView()
        configureTileProvider()

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnMapFragmentListener) {
            listener = context
        }
        else {
            throw RuntimeException("$context must implement OnMapFragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()

        mapView?.onDetach()
        listener = null
    }

    override fun onResume() {
        super.onResume()

        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()

        mapView?.onPause()
    }

    private fun configureMapView() {
        val mapView = mapView ?: return

        // disable default zoom controller
        mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        mapView.setMultiTouchControls(true)

        // offline mode as default
        mapView.setUseDataConnection(false)

        // configure and activate rotation gesture
        val rotationGestureOverlay = RotationGestureOverlay(mapView)
        rotationGestureOverlay.isEnabled = true
        mapView.overlays.add(rotationGestureOverlay)

        val overlayEvents = MapEventsOverlay(mapEventReceiver)
        mapView.overlays.add(overlayEvents)

        val mapSettings = listener?.getMapSettings() ?: return

        // configure and display scale bar
        if (mapSettings.displayScale) {
            val scaleBarOverlay = ScaleBarOverlay(mapView)
            scaleBarOverlay.setCentred(false)
            scaleBarOverlay.setAlignBottom(true)
            scaleBarOverlay.setAlignRight(false)
            mapView.overlays.add(scaleBarOverlay)
        }

        if (mapSettings.zoom > 0.0) {
            mapView.controller.setZoom(mapSettings.zoom)
        }

        if (mapSettings.minZoomLevel > 0.0) {
            mapView.minZoomLevel = mapSettings.minZoomLevel
        }

        if (mapSettings.maxZoomLevel > 0.0) {
            mapView.maxZoomLevel = mapSettings.maxZoomLevel
        }

        if (mapSettings.center != null) {
            mapView.controller.setCenter(mapSettings.center)
        }

        if (mapSettings.maxBounds != null) {
            mapView.setScrollableAreaLimitDouble(mapSettings.maxBounds)
        }
    }

    private fun configureTileProvider() {
        val context = context ?: return
        val mapView = mapView ?: return
        val mapSettings = listener?.getMapSettings() ?: return

        if (mapSettings.tileSourceSettings.isEmpty()) {
            return
        }

        val baseTilePath = mapSettings.baseTilesPath
        // TODO: set the right default storage directory to use as fallback
            ?: "${Environment.getExternalStorageDirectory().absolutePath}/osmdroid"

        val tileSources = mapSettings.tileSourceSettings.map { File("$baseTilePath/${it.name}") }

        val tileProvider = OfflineTileProvider(
            SimpleRegisterReceiver(context),
            tileSources.toTypedArray())

        mapView.tileProvider = tileProvider
    }

    private fun showSnackbarForDeletedPoi(geoPoint: GeoPoint?) {
        val container = container ?: return
        if (geoPoint == null) return

        val snackbar = Snackbar.make(
            container,
            R.string.action_poi_deleted,
            Snackbar.LENGTH_SHORT)
        snackbar.setAction(
            R.string.action_undo
        ) {
            addPoi(geoPoint)
        }
        snackbar.show()
    }

    private fun addPoi(geoPoint: GeoPoint? = null) {
        val context = context ?: return
        val mapView = mapView ?: return

        val poiMarker = Marker(mapView)
        poiMarker.id = UUID.randomUUID()
            .toString()
        poiMarker.position = geoPoint ?: mapView.mapCenter as GeoPoint
        poiMarker.setAnchor(
            Marker.ANCHOR_CENTER,
            Marker.ANCHOR_BOTTOM)
        setMarkerIcon(
            poiMarker,
            getPrimaryColor(context),
            2.0f)
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

    private fun setMarkerIcon(marker: Marker?,
                              @ColorInt tintColor: Int,
                              scale: Float = 1.0f) {
        val context = context ?: return

        marker?.icon = createScaledDrawable(
            context,
            R.drawable.ic_poi,
            tintColor,
            scale)
    }

    private fun selectMarker(marker: Marker?) {
        if (!fab?.isOrWillBeHidden!!) fab?.hide()

        val context = context ?: return

        if (actionMode == null) {
            actionMode =
                (activity as AppCompatActivity?)?.startSupportActionMode(actionModeCallback)
            actionMode?.setTitle(R.string.action_title_poi_edit)
        }

        setMarkerIcon(
            marker,
            getAccentColor(context),
            2.5f)
    }

    private fun deselectMarker(marker: Marker?) {
        if (!fab?.isOrWillBeShown!!) fab?.show()
        selectedPoi = null
        actionMode?.finish()

        val context = context ?: return

        setMarkerIcon(
            marker,
            getPrimaryColor(context),
            2.0f)
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    interface OnMapFragmentListener {
        fun getMapSettings(): MapSettings
    }

    companion object {

        /**
         * Use this factory method to create a new instance of this fragment.
         *
         * @return A new instance of [MapFragment]
         */
        @JvmStatic
        fun newInstance() = MapFragment()
    }
}
