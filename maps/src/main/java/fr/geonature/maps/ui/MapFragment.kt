package fr.geonature.maps.ui

import android.Manifest
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import fr.geonature.maps.BuildConfig
import fr.geonature.maps.R
import fr.geonature.maps.settings.MapSettings
import fr.geonature.maps.ui.overlay.FeatureOverlayProvider
import fr.geonature.maps.ui.widget.EditFeatureButton
import fr.geonature.maps.ui.widget.MyLocationButton
import fr.geonature.maps.ui.widget.RotateCompassButton
import fr.geonature.maps.util.PermissionUtils
import fr.geonature.maps.util.PermissionUtils.checkPermissions
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.modules.OfflineTileProvider
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import java.io.File

/**
 * Simple [Fragment] embedding a [MapView] instance.
 *
 * Use the [MapFragment.newInstance] factory method to create an instance of this fragment.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
open class MapFragment : Fragment() {

    var onSelectedPOIsListener: OnSelectedPOIsListener? = null

    private lateinit var container: View
    private lateinit var mapView: MapView
    private lateinit var editFeatureFab: EditFeatureButton
    private lateinit var myLocationFab: MyLocationButton
    private lateinit var rotateCompassFab: RotateCompassButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = context ?: return

        Configuration.getInstance()
                .load(context,
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
        return inflater.inflate(R.layout.fragment_map,
                                container,
                                false)
    }

    override fun onViewCreated(view: View,
                               savedInstanceState: Bundle?) {
        super.onViewCreated(view,
                            savedInstanceState)

        this.container = map_content
        this.mapView = map
        this.rotateCompassFab = fab_compass
        this.editFeatureFab = fab_poi
        this.myLocationFab = fab_location

        configureMapView()

        // check storage permissions
        val context = context ?: return
        val granted = PermissionUtils.checkSelfPermissions(context,
                                                           Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (granted) {
            configureTileProvider()
            configureVectorLayers()
        }
        else {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                               REQUEST_STORAGE_PERMISSIONS)
        }
    }

    override fun onDetach() {
        super.onDetach()

        mapView.onDetach()
    }

    override fun onResume() {
        super.onResume()

        mapView.onResume()
        myLocationFab.onResume()
    }

    override fun onPause() {
        super.onPause()

        mapView.onPause()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            REQUEST_STORAGE_PERMISSIONS -> {
                val requestPermissionsResult = checkPermissions(grantResults)

                if (requestPermissionsResult) {
                    configureTileProvider()
                    configureVectorLayers()

                    showSnackbar(getString(R.string.snackbar_permissions_granted))
                }
                else {
                    showSnackbar(getString(R.string.snackbar_permissions_not_granted))
                }
            }
            REQUEST_LOCATION_PERMISSIONS -> {
                val requestPermissionsResult = checkPermissions(grantResults)

                if (requestPermissionsResult) {
                    myLocationFab.requestLocation()
                }
                else {
                    showSnackbar(getString(R.string.snackbar_permissions_not_granted))
                }
            }
            else -> super.onRequestPermissionsResult(requestCode,
                                                     permissions,
                                                     grantResults)
        }
    }

    fun getSelectedPOIs(): List<GeoPoint> {
        return editFeatureFab.getSelectedPOIs()
    }

    fun setSelectedPOIs(pois: List<GeoPoint>) {
        editFeatureFab.setSelectedPOIs(pois)
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

        val mapSettings = arguments?.getParcelable(ARG_MAP_SETTINGS) as MapSettings

        // configure and display scale bar
        if (mapSettings.showScale) {
            val scaleBarOverlay = ScaleBarOverlay(mapView)
            scaleBarOverlay.setCentred(false)
            scaleBarOverlay.setAlignBottom(true)
            scaleBarOverlay.setAlignRight(false)
            mapView.overlays.add(scaleBarOverlay)
        }

        // configure and display map compass
        if (mapSettings.showCompass) {
            rotateCompassFab.setMapView(mapView)
            rotateCompassFab.show()
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

            override fun makeSnackbar(resId: Int,
                                      duration: Int): Snackbar? {
                return Snackbar.make(container,
                                     resId,
                                     duration)
            }

            override fun onSelectedPOIs(pois: List<GeoPoint>) {
                onSelectedPOIsListener?.onSelectedPOIs(pois)
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

            override fun checkPermissions(): Boolean {
                val context = context ?: return false
                val granted = PermissionUtils.checkSelfPermissions(context,
                                                                   Manifest.permission.ACCESS_FINE_LOCATION)

                if (!granted) {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                       REQUEST_LOCATION_PERMISSIONS)
                }

                return granted
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
    }

    private fun configureTileProvider() {
        val context = context ?: return
        val mapSettings = arguments?.getParcelable(ARG_MAP_SETTINGS) as MapSettings

        if (mapSettings.layersSettings.isEmpty()) {
            Log.w(TAG,
                  "No layers defined")

            showSnackbar(getString(R.string.snackbar_layers_undefined))

            return
        }

        if (!File(mapSettings.baseTilesPath).exists()) {
            Log.w(TAG,
                  "Unable to load tiles from '${mapSettings.baseTilesPath}'")

            showSnackbar(getString(R.string.snackbar_base_path_undefined,
                                   mapSettings.baseTilesPath))

            return
        }

        val tileSources = mapSettings.getLayersAsTileSources()
                .map { File("${mapSettings.baseTilesPath}/$it") }

        val tileProvider = OfflineTileProvider(SimpleRegisterReceiver(context),
                                               tileSources.toTypedArray())

        mapView.tileProvider = tileProvider
    }

    private fun configureVectorLayers() {
        val mapSettings = arguments?.getParcelable(ARG_MAP_SETTINGS) as MapSettings

        if (mapSettings.layersSettings.isEmpty()) {
            Log.w(TAG,
                  "No vector layers defined")

            showSnackbar(getString(R.string.snackbar_layers_undefined))

            return
        }

        if ((mapSettings.baseTilesPath == null) || !File(mapSettings.baseTilesPath).exists()) {
            Log.w(TAG,
                  "Unable to load vector layers from '${mapSettings.baseTilesPath}'")

            showSnackbar(getString(R.string.snackbar_base_path_undefined,
                                   mapSettings.baseTilesPath))

            return
        }

        GlobalScope.launch(Dispatchers.Main) {
            val overlays = FeatureOverlayProvider(mapSettings.baseTilesPath).loadFeaturesAsOverlays(mapSettings.getVectorLayers())
            overlays.forEach { mapView.overlays.add(it) }
        }
    }

    private fun showSnackbar(text: CharSequence) {
        Snackbar.make(container,
                      text,
                      Snackbar.LENGTH_LONG)
                .show()
    }

    interface OnSelectedPOIsListener {
        fun onSelectedPOIs(pois: List<GeoPoint>)
    }

    companion object {

        private val TAG = MapFragment::class.java.name

        const val ARG_MAP_SETTINGS = "arg_map_settings"
        const val ARG_EDIT_MODE = "arg_edit_mode"

        private const val REQUEST_STORAGE_PERMISSIONS = 0
        private const val REQUEST_LOCATION_PERMISSIONS = 1

        /**
         * Use this factory method to create a new instance of this fragment.
         *
         * @return A new instance of [MapFragment]
         */
        @JvmStatic
        fun newInstance(mapSettings: MapSettings,
                        editMode: EditFeatureButton.EditMode = EditFeatureButton.EditMode.MULTIPLE) =
                MapFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_MAP_SETTINGS,
                                      mapSettings)
                        putSerializable(ARG_EDIT_MODE,
                                        editMode)
                    }
                }
    }
}
