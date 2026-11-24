package fr.geonature.maps.maplibre.ui

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import fr.geonature.maps.maplibre.layer.BaseLayer
import fr.geonature.maps.maplibre.layer.BaseLayerSelector
import fr.geonature.maps.maplibre.layer.BaseLayerStyleFactory
import fr.geonature.maps.maplibre.layer.LocalLayerResolver
import fr.geonature.maps.maplibre.settings.toMapLibreLatLng
import fr.geonature.maps.maplibre.settings.toMapLibreLatLngBounds
import fr.geonature.maps.maplibre.ui.widget.ZoomButton
import fr.geonature.maps.settings.MapSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.tinylog.kotlin.Logger

/**
 * Displays a base map with [MapLibre](https://maplibre.org), configured from [MapSettings].
 *
 * Supported settings: `center`, `zoom`, `minZoomLevel`, `maxZoomLevel`, `maxBounds`, `showCompass`,
 * `showScale`, `showZoom`, `rotationGesture`, `useOnlineLayers` and the layers' definition.
 * Edit mode settings are ignored.
 *
 * The base layer is the first embedded tiles layer (`mbtiles` or `pmtiles`) if any, otherwise the
 * first online layer.
 *
 * @author S. Grimault
 */
class MapLibreFragment : Fragment() {

    private var mapView: MapView? = null
    private var mapLibreMap: MapLibreMap? = null
    private var scaleBarView: ScaleBarView? = null
    private var zoomButton: ZoomButton? = null

    private lateinit var mapSettings: MapSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mapSettings = requireNotNull(
            arguments?.let {
                BundleCompat.getParcelable(
                    it,
                    ARG_MAP_SETTINGS,
                    MapSettings::class.java
                )
            }) { "missing argument $ARG_MAP_SETTINGS" }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()
        MapLibre.getInstance(context)

        val density = resources.displayMetrics.density
        val margin = (16 * density).toInt()

        val mapView = MapView(context).also {
            this.mapView = it
            it.onCreate(savedInstanceState)
        }

        return FrameLayout(context).apply {
            addView(
                mapView,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )

            scaleBarView = ScaleBarView(context).also {
                addView(
                    it,
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.BOTTOM or Gravity.START
                    ).apply { setMargins(margin, margin, margin, margin) }
                )
            }

            zoomButton = ZoomButton(context).also {
                addView(
                    it,
                    FrameLayout.LayoutParams(
                        (48 * density).toInt(),
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.TOP or Gravity.START
                    ).apply { setMargins(margin, margin, margin, margin) }
                )
            }

            applyPreferences()
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

        mapView?.getMapAsync { map ->
            mapLibreMap = map
            configure(map)
            zoomButton?.setMap(map)
            loadBaseLayer(map)
        }
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
        applyPreferences()
    }

    override fun onPause() {
        mapView?.onPause()
        super.onPause()
    }

    override fun onStop() {
        mapView?.onStop()
        super.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        mapView?.onDestroy()
        mapView = null
        mapLibreMap = null
        scaleBarView = null
        zoomButton = null

        super.onDestroyView()
    }

    private fun configure(map: MapLibreMap) {
        if (mapSettings.minZoomLevel > 0.0) map.setMinZoomPreference(mapSettings.minZoomLevel)
        if (mapSettings.maxZoomLevel > 0.0) map.setMaxZoomPreference(mapSettings.maxZoomLevel)

        mapSettings.maxBounds?.also {
            map.setLatLngBoundsForCameraTarget(it.toMapLibreLatLngBounds())
        }

        val target = mapSettings.center?.toMapLibreLatLng()
            ?: mapSettings.maxBounds?.toMapLibreLatLngBounds()?.center

        map.cameraPosition = CameraPosition.Builder()
            .apply { target?.also { target(it) } }
            .zoom(mapSettings.zoom.coerceAtLeast(mapSettings.minZoomLevel))
            .build()

        scaleBarView?.also { scaleBar ->
            val updateScale = {
                map.cameraPosition.target?.also {
                    scaleBar.update(map.projection.getMetersPerPixelAtLatitude(it.latitude))
                }
            }

            map.addOnCameraMoveListener { updateScale() }
            map.addOnCameraIdleListener { updateScale() }
        }

        applyPreferences()
    }

    /**
     * Refreshes the map controls from the shared preferences, falling back on [MapSettings].
     */
    private fun applyPreferences() {
        val context = context ?: return
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        scaleBarView?.visibility = if (preferences.getBoolean(
                PREF_SHOW_SCALE,
                mapSettings.showScale
            )
        ) View.VISIBLE else View.GONE
        zoomButton?.visibility = if (preferences.getBoolean(
                PREF_SHOW_ZOOM,
                mapSettings.showZoom
            )
        ) View.VISIBLE else View.GONE

        mapLibreMap?.uiSettings?.apply {
            isCompassEnabled = preferences.getBoolean(
                PREF_SHOW_COMPASS,
                mapSettings.showCompass
            )
            isRotateGesturesEnabled = preferences.getBoolean(
                PREF_ROTATION_GESTURE,
                mapSettings.rotationGesture
            )
        }
    }

    private fun loadBaseLayer(map: MapLibreMap) {
        val appContext = requireContext().applicationContext

        viewLifecycleOwner.lifecycleScope.launch {
            val baseLayer = withContext(Dispatchers.IO) {
                val resolver = LocalLayerResolver(
                    appContext,
                    mapSettings.baseTilesPath
                )

                BaseLayerSelector.select(
                    mapSettings,
                    resolver::resolve
                )
            }

            Logger.info {
                when (baseLayer) {
                    is BaseLayer.Local -> "base layer: local layer '${baseLayer.settings.label}' (${baseLayer.file})"
                    is BaseLayer.Online -> "base layer: online layer '${baseLayer.settings.label}'"
                    null -> "no base layer found"
                }
            }

            map.setStyle(BaseLayerStyleFactory.build(baseLayer))
        }
    }

    companion object {
        private const val ARG_MAP_SETTINGS = "arg_map_settings"

        // same keys as the ones used by the map preferences screen
        private const val PREF_SHOW_COMPASS = "map_show_compass"
        private const val PREF_SHOW_SCALE = "map_show_scale"
        private const val PREF_SHOW_ZOOM = "map_show_zoom"
        private const val PREF_ROTATION_GESTURE = "map_rotation_gesture"

        @JvmStatic
        fun newInstance(mapSettings: MapSettings) = MapLibreFragment().apply {
            arguments = Bundle().apply {
                putParcelable(
                    ARG_MAP_SETTINGS,
                    mapSettings
                )
            }
        }
    }
}
