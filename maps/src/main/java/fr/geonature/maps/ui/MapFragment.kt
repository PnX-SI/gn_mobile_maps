package fr.geonature.maps.ui

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import fr.geonature.maps.BuildConfig
import fr.geonature.maps.R
import fr.geonature.maps.settings.MapSettings
import fr.geonature.maps.util.DrawableUtils.createScaledDrawable
import fr.geonature.maps.util.ThemeUtils
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.modules.OfflineTileProvider
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import java.io.File

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
    private var mapView: MapView? = null
    private val pois = ArrayList<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = requireContext()

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

        mapView = view.findViewById(R.id.map)
        view.findViewById<View>(R.id.fab)
            .setOnClickListener { addPoi() }

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

    private fun addPoi() {
        val context = context ?: return
        val mapView = mapView ?: return

        val accentColor = ThemeUtils.getAccentColor(context)
        val poiMarker = Marker(mapView)
        poiMarker.position = mapView.mapCenter as GeoPoint
        poiMarker.setAnchor(
            Marker.ANCHOR_CENTER,
            Marker.ANCHOR_BOTTOM)
        poiMarker.icon = createScaledDrawable(
            context,
            R.drawable.ic_poi,
            accentColor,
            2.0f)
        poiMarker.isDraggable = true
        poiMarker.infoWindow = null
        poiMarker.setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
            override fun onMarkerDragEnd(marker: Marker?) {
                marker?.alpha = 1.0f
                marker?.icon = createScaledDrawable(
                    context,
                    R.drawable.ic_poi,
                    accentColor,
                    2.0f)
            }

            override fun onMarkerDragStart(marker: Marker?) {
                marker?.alpha = 0.5f
                marker?.icon = createScaledDrawable(
                    context,
                    R.drawable.ic_poi,
                    accentColor,
                    2.5f)
            }

            override fun onMarkerDrag(marker: Marker?) {

            }
        })
        mapView.overlays.add(poiMarker)
        pois.add(poiMarker)
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
