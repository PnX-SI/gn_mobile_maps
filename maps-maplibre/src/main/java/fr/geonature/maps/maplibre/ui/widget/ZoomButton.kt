package fr.geonature.maps.maplibre.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import fr.geonature.maps.maplibre.R
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.maps.MapLibreMap
import java.text.NumberFormat

/**
 * Zoom control for [MapLibreMap]: zoom in/out buttons with the current zoom level.
 *
 * @author S. Grimault
 */
class ZoomButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RelativeLayout(
    context,
    attrs
) {

    private val zoomInButton: ImageButton
    private val zoomOutButton: ImageButton
    private val zoomTextButton: TextView
    private val numberFormat = NumberFormat.getInstance().apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    }

    private var map: MapLibreMap? = null
    private val cameraMoveListener = MapLibreMap.OnCameraMoveListener { refresh() }
    private val cameraIdleListener = MapLibreMap.OnCameraIdleListener { refresh() }

    init {
        inflate(
            context,
            R.layout.button_zoom,
            this
        )

        zoomInButton = findViewById(android.R.id.button1)
        zoomOutButton = findViewById(android.R.id.button2)
        zoomTextButton = findViewById(android.R.id.title)
    }

    /**
     * Binds this control to the given map.
     */
    fun setMap(map: MapLibreMap) {
        unbind()

        this.map = map
        map.addOnCameraMoveListener(cameraMoveListener)
        map.addOnCameraIdleListener(cameraIdleListener)

        zoomInButton.setOnClickListener {
            map.animateCamera(CameraUpdateFactory.zoomIn())
        }
        zoomOutButton.setOnClickListener {
            map.animateCamera(CameraUpdateFactory.zoomOut())
        }

        refresh()
    }

    override fun onDetachedFromWindow() {
        unbind()

        super.onDetachedFromWindow()
    }

    private fun unbind() {
        map?.also {
            it.removeOnCameraMoveListener(cameraMoveListener)
            it.removeOnCameraIdleListener(cameraIdleListener)
        }
        map = null
    }

    private fun refresh() {
        val map = map ?: return
        val zoom = map.cameraPosition.zoom

        zoomTextButton.text = numberFormat.format(zoom)
        zoomInButton.isEnabled = zoom < map.maxZoomLevel
        zoomOutButton.isEnabled = zoom > map.minZoomLevel
    }
}
