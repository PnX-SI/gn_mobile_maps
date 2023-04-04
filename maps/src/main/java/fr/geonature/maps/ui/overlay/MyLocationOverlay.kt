package fr.geonature.maps.ui.overlay

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Location
import fr.geonature.maps.R
import fr.geonature.maps.util.DrawableUtils
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.TileSystem
import org.osmdroid.views.MapView
import org.osmdroid.views.Projection
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.compass.IOrientationConsumer
import org.osmdroid.views.overlay.compass.IOrientationProvider
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider
import org.tinylog.kotlin.Logger

/**
 * Shows on the map the current user position.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class MyLocationOverlay(
    mapView: MapView,
    private var maxBounds: BoundingBox? = null
) : Overlay(), MyLocationListener, IOrientationConsumer {

    private val myLocationProvider: IMyLocationProvider
    private val compassOrientationProvider: IOrientationProvider
    private var myLocationListener: MyLocationListener? = null
    private val compassBitmap: Bitmap?
    private var location: Location? = null
    private var compassOrientationProviderEnabled = false
    private var compassOrientation = 0f

    init {
        myLocationProvider = GpsMyLocationProvider(mapView.context).apply {
            locationUpdateMinDistance = 2F // 2 meters
            locationUpdateMinTime = (10 * 1000).toLong() // 10s
        }

        compassOrientationProvider = CompassOrientationProvider(mapView.context)
        compassBitmap = DrawableUtils.toBitmap(
            mapView.context,
            R.drawable.ic_compass,
            Color.BLUE
        )

        location = myLocationProvider.lastKnownLocation
        compassOrientation = compassOrientationProvider.lastKnownOrientation

        isEnabled = false
    }

    override fun onPause() {
        super.onPause()

        disableMyLocation()
    }

    override fun draw(
        pCanvas: Canvas?,
        pProjection: Projection?
    ) {
        super.draw(
            pCanvas,
            pProjection
        )

        if (pCanvas != null && pProjection != null) drawMyLocation(
            pCanvas,
            pProjection
        )
    }

    override fun onDetach(mapView: MapView?) {
        disableMyLocation()
        myLocationProvider.stopLocationProvider()
        myLocationProvider.destroy()
        compassOrientationProvider.destroy()

        super.onDetach(mapView)
    }

    override fun onLocationChanged(
        location: Location?,
        source: IMyLocationProvider?
    ) {
        Logger.debug { "onLocationChanged: $location" }

        if (location == null) return

        val isLocationInsideMaxBounds = maxBounds?.contains(GeoPoint(location)) ?: true

        if (!isLocationInsideMaxBounds) {
            this.onLocationOutsideBoundaries(location)

            return
        }

        this.location = location

        this.myLocationListener?.onLocationChanged(
            location,
            source
        )
    }

    override fun onLocationOutsideBoundaries(location: Location?) {
        this.myLocationListener?.onLocationOutsideBoundaries(location)
    }

    override fun onOrientationChanged(
        orientation: Float,
        source: IOrientationProvider?
    ) {
        this.compassOrientation = orientation
    }

    fun getLastKnownLocation(): Location {
        return location ?: myLocationProvider.lastKnownLocation
    }

    fun setMyLocationListener(myLocationListener: MyLocationListener) {
        this.myLocationListener = myLocationListener
    }

    fun enableMyLocation(): Boolean {
        if (isEnabled) return true

        isEnabled = myLocationProvider.startLocationProvider(this)
        compassOrientationProviderEnabled =
            compassOrientationProvider.startOrientationProvider(this)

        Logger.debug { "enableMyLocation: $isEnabled" }

        onLocationChanged(
            myLocationProvider.lastKnownLocation,
            myLocationProvider
        )

        return isEnabled
    }

    fun disableMyLocation() {
        myLocationProvider.stopLocationProvider()
        compassOrientationProvider.stopOrientationProvider()
        isEnabled = false
        compassOrientationProviderEnabled = false
    }

    private fun drawMyLocation(
        canvas: Canvas,
        projection: Projection
    ) {
        val location = location ?: return

        val radius: (Location) -> Float = {
            (it.accuracy / TileSystem.GroundResolution(
                it.latitude,
                projection.zoomLevel
            )).toFloat()
        }

        val toPoint: (Location) -> Pair<Float, Float> = {
            val point = projection.toPixels(
                GeoPoint(it),
                null
            )
            Pair(
                point.x.toFloat(),
                point.y.toFloat()
            )
        }

        val locationPoint = toPoint(location)

        drawLocation(
            canvas,
            radius(location),
            locationPoint.first,
            locationPoint.second
        )
    }

    private fun drawLocation(
        canvas: Canvas,
        radius: Float,
        x: Float,
        y: Float
    ) {
        canvas.save()

        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        circlePaint.color = Color.BLUE

        // draw current location precision accuracy
        circlePaint.alpha = 48
        circlePaint.style = Paint.Style.FILL
        canvas.drawCircle(
            x,
            y,
            radius,
            circlePaint
        )

        circlePaint.alpha = 96
        circlePaint.style = Paint.Style.STROKE
        canvas.drawCircle(
            x,
            y,
            radius,
            circlePaint
        )

        // draw the current position as plain colored circle
        circlePaint.color = Color.WHITE
        circlePaint.alpha = 248
        circlePaint.style = Paint.Style.FILL
        circlePaint.setShadowLayer(
            8f,
            0f,
            0f,
            Color.GRAY
        )
        canvas.drawCircle(
            x,
            y,
            32f,
            circlePaint
        )

        circlePaint.style = Paint.Style.STROKE
        circlePaint.alpha = 160
        circlePaint.color = Color.BLUE
        canvas.drawCircle(
            x,
            y,
            32f,
            circlePaint
        )

        circlePaint.color = Color.BLUE
        circlePaint.alpha = 128
        circlePaint.style = Paint.Style.FILL
        canvas.drawCircle(
            x,
            y,
            24f,
            circlePaint
        )

        // draw compass orientation
        if (compassOrientationProviderEnabled) {
            val compassPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            compassPaint.color = Color.BLUE
            compassPaint.alpha = 128

            canvas.rotate(
                compassOrientation,
                x,
                y
            )

            compassBitmap?.also {
                canvas.drawBitmap(
                    it,
                    x - compassBitmap.width / 2,
                    y - compassBitmap.height + 8,
                    compassPaint
                )
            }

            canvas.restore()
        }
    }
}
