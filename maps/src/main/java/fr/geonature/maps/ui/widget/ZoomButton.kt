package fr.geonature.maps.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import fr.geonature.maps.R
import java.text.NumberFormat
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.views.MapView

/**
 * Zoom control.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class ZoomButton(
    context: Context,
    attrs: AttributeSet
) : RelativeLayout(context, attrs), MapListener {

    private val zoomInButton: ImageButton
    private val zoomOutButton: ImageButton
    private val zoomTextButton: TextView

    init {
        View.inflate(context, R.layout.button_zoom, this)

        zoomInButton = findViewById(android.R.id.button1)
        zoomOutButton = findViewById(android.R.id.button2)
        zoomTextButton = findViewById(android.R.id.title)
    }

    override fun onScroll(event: ScrollEvent?): Boolean {
        return true
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
        if (event == null) {
            zoomTextButton.text = "0"
            zoomTextButton.visibility = View.GONE
            return true
        }

        zoomTextButton.text =
            NumberFormat.getInstance().apply {
                minimumFractionDigits = 1
                maximumFractionDigits = 1
            }.format(event.zoomLevel)
        zoomTextButton.visibility = View.VISIBLE

        zoomInButton.isEnabled = event.source.canZoomIn()
        zoomOutButton.isEnabled = event.source.canZoomOut()

        return true
    }

    fun setMapView(mapView: MapView) {
        mapView.removeMapListener(this)
        mapView.addMapListener(this)

        zoomInButton.setOnClickListener {
            mapView.controller.zoomIn()
        }
        zoomOutButton.setOnClickListener {
            mapView.controller.zoomOut()
        }
    }
}
