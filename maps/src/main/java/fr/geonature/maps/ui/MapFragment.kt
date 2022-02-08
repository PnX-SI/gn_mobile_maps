package fr.geonature.maps.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import fr.geonature.maps.R
import fr.geonature.maps.settings.LayerSettings
import fr.geonature.maps.settings.LayerSettingsViewModel
import fr.geonature.maps.settings.MapSettings
import fr.geonature.maps.ui.dialog.LayerSettingsDialogFragment
import fr.geonature.maps.ui.overlay.feature.FeatureCollectionOverlay
import fr.geonature.maps.ui.overlay.feature.FeatureOverlay
import fr.geonature.maps.ui.widget.EditFeatureButton
import fr.geonature.maps.ui.widget.MyLocationButton
import fr.geonature.maps.ui.widget.RotateCompassButton
import fr.geonature.maps.ui.widget.ZoomButton
import fr.geonature.maps.util.MapSettingsPreferencesUtils.setDefaultPreferences
import fr.geonature.maps.util.MapSettingsPreferencesUtils.showCompass
import fr.geonature.maps.util.MapSettingsPreferencesUtils.showScale
import fr.geonature.maps.util.MapSettingsPreferencesUtils.showZoom
import fr.geonature.maps.util.MapSettingsPreferencesUtils.useOnlineLayers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay

/**
 * Simple [Fragment] embedding a [MapView] instance.
 *
 * Use the [MapFragment.newInstance] factory method to create an instance of this fragment.
 *
 * @author S. Grimault
 */
open class MapFragment : Fragment(),
    LayerSettingsDialogFragment.OnLayerSettingsDialogFragmentListener {

    var onSelectedPOIsListener: (pois: List<GeoPoint>) -> Unit = {}
    var onVectorLayersChangedListener: (activeVectorOverlays: List<Overlay>) -> Unit = {}

    private var layerSettingsViewModel: LayerSettingsViewModel? = null
    private var listener: OnMapFragmentPermissionsListener? = null

    private lateinit var container: View
    private lateinit var mapView: MapView
    private lateinit var editFeatureFab: EditFeatureButton
    private lateinit var myLocationFab: MyLocationButton
    private lateinit var rotateCompassFab: RotateCompassButton
    private lateinit var layersFab: FloatingActionButton
    private lateinit var zoomFab: ZoomButton
    private lateinit var mapSettings: MapSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = context ?: return

        mapSettings = getMapSettings(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(
            R.layout.fragment_map,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        this.container = view.findViewById(R.id.map_content)
        this.mapView = view.findViewById(R.id.map)
        this.editFeatureFab = view.findViewById(R.id.fab_poi)
        this.myLocationFab = view.findViewById(R.id.fab_location)
        this.rotateCompassFab = view.findViewById(R.id.fab_compass)
        this.layersFab = view.findViewById(R.id.fab_layers)
        this.zoomFab = view.findViewById(R.id.fab_zoom)

        // check permissions and configure MapView
        activity?.also {
            lifecycleScope.launch {
                val granted = listener?.onStoragePermissionsGranted() ?: false

                if (!granted) {
                    showSnackbar(getString(R.string.snackbar_permissions_not_granted))
                }

                layerSettingsViewModel = ViewModelProvider(it,
                    LayerSettingsViewModel.Factory {
                        LayerSettingsViewModel(
                            it.application,
                            mapSettings.baseTilesPath
                        )
                    })[LayerSettingsViewModel::class.java]

                // then load map configuration from preferences
                Configuration.getInstance()
                    .apply {
                        load(
                            it,
                            PreferenceManager.getDefaultSharedPreferences(it)
                        )
                    }

                configureMapView()
                loadLayersSettings()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context !is OnMapFragmentPermissionsListener) {
            throw RuntimeException("$context must implement OnMapFragmentPermissionsListener")
        }

        listener = context
    }

    override fun onDetach() {
        super.onDetach()

        mapView.onDetach()
    }

    override fun onResume() {
        super.onResume()

        mapView.onResume()
        myLocationFab.onResume()

        context?.also { context ->
            showCompass(context).also {
                rotateCompassFab.setMapView(mapView)
                rotateCompassFab.visibility = if (it) View.VISIBLE else View.GONE
            }
            configureScaleBarOverlay(showScale(context))
            showZoom(context).also {
                zoomFab.setMapView(mapView)
                zoomFab.visibility = if (it) View.VISIBLE else View.GONE
            }
        }

        loadLayersSettings()
    }

    override fun onPause() {
        super.onPause()

        mapView.onPause()
    }

    override fun onSelectedLayersSettings(layersSettings: List<LayerSettings>) {
        layerSettingsViewModel?.load(layersSettings)
    }

    fun getSelectedPOIs(): List<GeoPoint> {
        return editFeatureFab.getSelectedPOIs()
    }

    fun setSelectedPOIs(pois: List<GeoPoint>) {
        editFeatureFab.setSelectedPOIs(pois)
    }

    fun clearActiveSelection() {
        editFeatureFab.clearActiveSelection()
    }

    /**
     * Gets [Overlay]s matching given predicate.
     *
     * @return [List] of [Overlay]s
     */
    fun getOverlays(filter: (overlay: Overlay) -> Boolean = DEFAULT_OVERLAY_FILTER): List<Overlay> {
        if (filter === DEFAULT_OVERLAY_FILTER) {
            return mapView.overlays
        }

        return mapView.overlays.filter(filter)
    }

    private fun getMapSettings(context: Context): MapSettings {
        // read map settings from arguments or build the default one
        val mapSettingsBuilder = MapSettings.Builder.newInstance()
            .from(arguments?.getParcelable(ARG_MAP_SETTINGS))

        setDefaultPreferences(
            context,
            mapSettingsBuilder.build()
        )

        // update map settings according to preferences
        return mapSettingsBuilder
            .useOnlineLayers(mapSettingsBuilder.layersSettings.any { it.isOnline() } && useOnlineLayers(
                context,
                mapSettingsBuilder.useOnlineLayers
            ))
            .showCompass(showCompass(context))
            .showScale(showScale(context))
            .showZoom(showZoom(context))
            .build()
    }

    private fun loadLayersSettings() {
        layerSettingsViewModel?.also { vm ->
            vm.load(vm.getSelectedLayers(mapSettings))
        }
    }

    private fun configureMapView() {
        // disable default zoom controller
        mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        mapView.setMultiTouchControls(true)

        // offline mode as default
        mapView.setUseDataConnection(false)

        // configure and activate rotation gesture
        val rotationGestureOverlay = RotationGestureOverlay(mapView)
        rotationGestureOverlay.isEnabled = true
        mapView.overlays.add(rotationGestureOverlay)

        // configure and display attribution notice for the current online source
        if (mapSettings.showAttribution) {
            val copyrightOverlay = CopyrightOverlay(context)
            copyrightOverlay.setAlignBottom(true)
            copyrightOverlay.setAlignRight(true)
            mapView.overlays.add(copyrightOverlay)
        }

        // configure and display map compass
        if (mapSettings.showCompass) {
            rotateCompassFab.setMapView(mapView)
            rotateCompassFab.show()
        }

        // configure and display scale bar
        configureScaleBarOverlay()

        // configure and display zoom control
        if (mapSettings.showZoom) {
            zoomFab.setMapView(mapView)
            zoomFab.visibility = View.VISIBLE
        }

        // configure edit POIs overlay
        editFeatureFab.setListener(object : EditFeatureButton.OnEditFeatureButtonListener {
            override fun getMapView(): MapView {
                return mapView
            }

            override fun getEditMode(): EditFeatureButton.EditMode {
                return arguments?.getSerializable(ARG_EDIT_MODE) as EditFeatureButton.EditMode
            }

            override fun getMinZoom(): Double {
                return mapSettings.minZoomLevel
            }

            override fun getMinZoomEditing(): Double {
                return mapSettings.minZoomEditing
            }

            override fun startActionMode(callback: ActionMode.Callback): ActionMode? {
                return (activity as AppCompatActivity?)?.startSupportActionMode(callback)
            }

            override fun makeSnackbar(
                resId: Int,
                duration: Int
            ): Snackbar {
                return Snackbar.make(
                    container,
                    resId,
                    duration
                )
            }

            override fun onSelectedPOIs(pois: List<GeoPoint>) {
                onSelectedPOIsListener(pois)
            }
        })

        // configure my location overlay
        myLocationFab.setListener(object : MyLocationButton.OnMyLocationButtonListener {
            override fun getMapView(): MapView {
                return mapView
            }

            override fun getMaxBounds(): BoundingBox? {
                return mapSettings.maxBounds
            }

            override fun checkPermissions(permission: String) {
                lifecycleScope.launch {
                    val granted = listener?.onLocationPermissionGranted() ?: false

                    if (!granted) {
                        showSnackbar(getString(R.string.snackbar_permissions_not_granted))
                        return@launch
                    }

                    myLocationFab.requestLocation()
                }
            }
        })

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

        configureLayersSelector()

        activity?.also {
            configureLayers(it)
        }
    }

    private fun configureScaleBarOverlay(enabled: Boolean = mapSettings.showScale) {
        val scaleBarOverlay = mapView.overlays.firstOrNull { it is ScaleBarOverlay }
            ?: ScaleBarOverlay(mapView).apply {
                setCentred(false)
                setAlignBottom(true)
                setAlignRight(false)
            }
                .also {
                    mapView.overlays.add(it)
                }

        scaleBarOverlay.isEnabled = enabled
    }

    private fun configureLayersSelector() {
        layerSettingsViewModel?.also { vm ->
            val layerSettings = mapSettings.layersSettings.takeIf { it.isNotEmpty() } ?: return@also

            with(layersFab) {
                setOnClickListener {
                    LayerSettingsDialogFragment.newInstance(
                        layerSettings,
                        vm.getSelectedLayers(mapSettings)
                    )
                        .also { dialogFragment ->
                            dialogFragment.show(
                                childFragmentManager,
                                LAYER_SETTINGS_DIALOG_FRAGMENT
                            )
                        }
                }
                show()
            }
        }
    }

    private fun configureLayers(activity: FragmentActivity) {
        layerSettingsViewModel?.also { vm ->
            mapView.addMapListener(object : MapListener {
                override fun onScroll(event: ScrollEvent?): Boolean {
                    return true
                }

                override fun onZoom(event: ZoomEvent?): Boolean {
                    val zoomLevel = event?.zoomLevel ?: return true

                    val selectedLayers =
                        vm.getActiveLayersOnZoomLevel(zoomLevel)

                    getOverlays { it is FeatureCollectionOverlay }.forEach { overlay ->
                        (overlay as FeatureCollectionOverlay).isEnabled =
                            selectedLayers.any { it.label == overlay.name }
                    }

                    return true
                }
            })

            vm.tileProvider.removeObservers(activity)
            vm.tileProvider.observe(
                activity
            ) {
                if (!isResumed) {
                    return@observe
                }

                if (it == null) {
                    mapView.tileProvider?.detach()
                } else {
                    mapView.tileProvider = it
                }

                mapView.invalidate()
            }
            vm.vectorOverlays.removeObservers(activity)
            vm.vectorOverlays.observe(
                activity
            ) {
                CoroutineScope(Main).launch {
                    if (!isResumed) {
                        return@launch
                    }

                    val selectedLayers =
                        vm.getActiveLayersOnZoomLevel(mapView.zoomLevelDouble)

                    mapView.overlays.asSequence()
                        .filter { it is FeatureCollectionOverlay || it is FeatureOverlay }
                        .forEach {
                            mapView.overlays.remove(it)
                        }

                    val markerOverlaysFirstIndex =
                        mapView.overlays.indexOfFirst { it is Marker }
                            .coerceAtLeast(0)
                    it.forEach { overlay ->
                        mapView.overlays.add(
                            markerOverlaysFirstIndex,
                            (overlay as FeatureCollectionOverlay).apply {
                                isEnabled =
                                    selectedLayers.any { selectedLayer -> selectedLayer.label == name }
                            }
                        )
                    }

                    mapView.invalidate()

                    delay(100)

                    onVectorLayersChangedListener(it)
                }
            }
        }
    }

    private fun showSnackbar(text: CharSequence) {
        Snackbar.make(
            container,
            text,
            LENGTH_LONG
        )
            .show()
    }

    /**
     * Callback used by [MapFragment].
     */
    interface OnMapFragmentPermissionsListener {

        /**
         * Whether storage access permissions were granted.
         */
        suspend fun onStoragePermissionsGranted(): Boolean

        /**
         * Whether location permission was granted.
         */
        suspend fun onLocationPermissionGranted(): Boolean
    }

    companion object {

        const val ARG_MAP_SETTINGS = "arg_map_settings"
        const val ARG_EDIT_MODE = "arg_edit_mode"

        private const val LAYER_SETTINGS_DIALOG_FRAGMENT =
            "layer_settings_dialog_fragment"

        private val DEFAULT_OVERLAY_FILTER: (overlay: Overlay) -> Boolean = { true }

        /**
         * Use this factory method to create a new instance of this fragment.
         *
         * @return A new instance of [MapFragment]
         */
        @JvmStatic
        fun newInstance(
            mapSettings: MapSettings,
            editMode: EditFeatureButton.EditMode = EditFeatureButton.EditMode.SINGLE
        ) =
            MapFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(
                        ARG_MAP_SETTINGS,
                        mapSettings
                    )
                    putSerializable(
                        ARG_EDIT_MODE,
                        editMode
                    )
                }
            }
    }
}
