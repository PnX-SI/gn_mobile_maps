package fr.geonature.maps.ui.overlay

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Location
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.TileSystem
import org.osmdroid.views.MapView
import org.osmdroid.views.Projection
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider

/**
 * Shows on the map the current user position.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class MyLocationOverlay(mapView: MapView) : Overlay(), IMyLocationConsumer {

    private val myLocationProvider: IMyLocationProvider
    private var location: Location? = null

    init {
        myLocationProvider = GpsMyLocationProvider(mapView.context)
        location = myLocationProvider.lastKnownLocation
    }

    override fun onResume() {
        super.onResume()

        enableMyLocation()
    }

    override fun onPause() {
        super.onPause()

        disableMyLocation()
    }

    override fun draw(pCanvas: Canvas?, pProjection: Projection?) {
        super.draw(pCanvas, pProjection)

        if (pCanvas != null && pProjection != null) drawMyLocation(pCanvas, pProjection)
    }

    override fun onDetach(mapView: MapView?) {
        disableMyLocation()
        myLocationProvider.destroy()

        super.onDetach(mapView)
    }

    fun enableMyLocation(): Boolean {
        return myLocationProvider.startLocationProvider(this)
    }

    fun disableMyLocation() {
        myLocationProvider.stopLocationProvider()
    }

    override fun onLocationChanged(location: Location?, source: IMyLocationProvider?) {
        this.location = location
    }

    private fun drawMyLocation(canvas: Canvas, projection: Projection) {
        val location = location ?: return

        val radius: (Location) -> Float = {
            (it.accuracy / TileSystem.GroundResolution(
                it.latitude, projection.zoomLevel
            )).toFloat()
        }

        val toPoint: (Location) -> Pair<Float, Float> = {
            val point = projection.toPixels(GeoPoint(it), null)
            Pair(point.x.toFloat(), point.y.toFloat())
        }

        val locationPoint = toPoint(location)

        drawLocation(canvas, radius(location), locationPoint.first, locationPoint.second)
    }

    private fun drawLocation(canvas: Canvas, radius: Float, x: Float, y: Float) {
        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        circlePaint.color = Color.BLUE

        // draw current location precision accuracy
        circlePaint.alpha = 40
        circlePaint.style = Paint.Style.FILL
        canvas.drawCircle(x, y, radius, circlePaint)

        circlePaint.alpha = 80
        circlePaint.style = Paint.Style.STROKE
        canvas.drawCircle(x, y, radius, circlePaint)

        // draw the current position as plain colored circle
        circlePaint.color = Color.WHITE
        circlePaint.alpha = 250
        circlePaint.style = Paint.Style.FILL
        circlePaint.setShadowLayer(8f, 0f, 0f, Color.GRAY)
        canvas.drawCircle(x, y, 32f, circlePaint)

        circlePaint.style = Paint.Style.STROKE
        circlePaint.alpha = 180
        circlePaint.color = Color.BLUE
        canvas.drawCircle(x, y, 32f, circlePaint)

        circlePaint.color = Color.BLUE
        circlePaint.alpha = 120
        circlePaint.style = Paint.Style.FILL
        canvas.drawCircle(x, y, 24f, circlePaint)
    }
}