package fr.geonature.maps.ui

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
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
import fr.geonature.maps.ui.widget.EditFeatureButton
import fr.geonature.maps.ui.widget.MyLocationButton
import fr.geonature.maps.ui.widget.RotateCompassButton
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.modules.OfflineTileProvider
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
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
    private var container: View? = null
    private var mapView: MapView? = null
    private var editFeatureFab: EditFeatureButton? = null
    private var myLocationFab: MyLocationButton? = null
    private var rotateCompassFab: RotateCompassButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = context ?: return

        Configuration.getInstance()
            .load(
                context,
                PreferenceManager.getDefaultSharedPreferences(context)
            )
        Configuration.getInstance()
            .isDebugMode = BuildConfig.DEBUG
        Configuration.getInstance()
            .isDebugTileProviders = BuildConfig.DEBUG
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(
            R.layout.fragment_map,
            container,
            false
        )

        this.container = view.findViewById(android.R.id.content)
        this.mapView = view.findViewById(R.id.map)
        this.rotateCompassFab = view.findViewById(R.id.fab_compass)
        this.editFeatureFab = view.findViewById(R.id.fab_poi)
        this.myLocationFab = view.findViewById(R.id.fab_location)

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
        myLocationFab?.onResume()
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
        if (mapSettings.showScale) {
            val scaleBarOverlay = ScaleBarOverlay(mapView)
            scaleBarOverlay.setCentred(false)
            scaleBarOverlay.setAlignBottom(true)
            scaleBarOverlay.setAlignRight(false)
            mapView.overlays.add(scaleBarOverlay)
        }

        // configure and display map compass
        if (mapSettings.showCompass) {
            rotateCompassFab?.setMapView(mapView)
            rotateCompassFab?.show()
        }

        // configure edit POIs overlay
        editFeatureFab?.setListener(object : EditFeatureButton.OnEditFeatureButtonListener {
            override fun getMapView(): MapView {
                return mapView
            }

            override fun startActionMode(callback: ActionMode.Callback): ActionMode? {
                return (activity as AppCompatActivity?)?.startSupportActionMode(callback)
            }

            override fun makeSnackbar(
                resId: Int,
                duration: Int
            ): Snackbar? {
                val container = container ?: return null

                return Snackbar.make(
                    container,
                    resId,
                    duration
                )
            }
        })

        // configure my location overlay
        myLocationFab?.setMapView(mapView)

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

        if (mapSettings.layersSettings.isEmpty()) {
            return
        }

        val baseTilePath = mapSettings.baseTilesPath
        // TODO: set the right default storage directory to use as fallback
            ?: "${Environment.getExternalStorageDirectory().absolutePath}/osmdroid"

        val tileSources = mapSettings.getLayersAsTileSources()
            .map { File("$baseTilePath/$it") }

        val tileProvider = OfflineTileProvider(
            SimpleRegisterReceiver(context),
            tileSources.toTypedArray()
        )

        mapView.tileProvider = tileProvider
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
        fun newInstance() =
            MapFragment()
    }
}
