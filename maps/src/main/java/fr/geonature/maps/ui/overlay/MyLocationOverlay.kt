package fr.geonature.maps.ui.overlay

import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.location.Location
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
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
 * @author S. Grimault
 */
class MyLocationOverlay(
    private val mapView: MapView,
    private var maxBounds: BoundingBox? = null
) : Overlay(), MyLocationListener, IOrientationConsumer {

    private val myLocationProvider: IMyLocationProvider
    private val compassOrientationProvider: IOrientationProvider
    private var myLocationListener: MyLocationListener? = null
    private val compassBitmap: Bitmap?
    private var location: Location? = null
    private var compassOrientationProviderEnabled = false
    private var compassOrientation = 0f
    private var compassOrientationOffset = 0f
        set(value) {
            field = value
            invalidate(true)
        }
    private var compassOrientationValueAnimator: ValueAnimator? = null

    // in ms, if the previous rendering was less than this value ago, we don't render again
    private var lastRender = 0L
    private val lastRenderDelay = 500

    private val radius: (Location, Projection) -> Float = { location, projection ->
        (location.accuracy / TileSystem.GroundResolution(
            location.latitude,
            projection.zoomLevel
        )).toFloat()
    }

    private val toPoint: (Location, Projection) -> Pair<Float, Float> = { location, projection ->
        val point = projection.toPixels(
            GeoPoint(location),
            null
        )
        Pair(
            point.x.toFloat(),
            point.y.toFloat()
        )
    }

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

        lastRender = System.currentTimeMillis()
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
        invalidate()

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
        if (compassOrientationValueAnimator != null) return
        if (orientation.toInt() == compassOrientation.toInt()) return

        compassOrientationValueAnimator = ValueAnimator.ofFloat(
            compassOrientation,
            orientation
        )
            .apply {
                duration = 500 // in ms
                interpolator = LinearInterpolator()
                addUpdateListener {
                    compassOrientationOffset = it.animatedValue as Float
                }
                doOnEnd {
                    compassOrientationOffset = compassOrientation
                    compassOrientationValueAnimator = null
                }
                start()
            }

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

        Logger.debug { "location provider: $isEnabled, orientation provider: $compassOrientationProviderEnabled" }

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
        invalidate(true)
    }

    private fun drawMyLocation(
        canvas: Canvas,
        projection: Projection
    ) {
        val location = location ?: return

        val locationPoint = toPoint(
            location,
            projection
        )

        drawLocation(
            canvas,
            radius(
                location,
                projection
            ),
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
            compassPaint.alpha = (Math.min(compassOrientationOffset, compassOrientation) / Math.max(compassOrientationOffset, compassOrientation) * 128).toInt()
            canvas.rotate(
                compassOrientationOffset,
                x,
                y
            )

            compassBitmap?.also {
                canvas.drawBitmap(
                    it,
                    x - compassBitmap.width / 2,
                    y - compassBitmap.height + 4, // add some padding between the compass bitmap and the main circle
                    compassPaint
                )
            }

            canvas.restore()
        }
    }

    /**
     * Invalidate the current view.
     */
    private fun invalidate(force: Boolean = false) {
        if (!force && (lastRender + lastRenderDelay > System.currentTimeMillis())) return

        val location = location ?: return
        val compassBitmap = compassBitmap ?: return

        val screenRect: Rect = mapView.getProjection().screenRect
        val center = toPoint(
            location,
            mapView.projection
        )

        val frameLeft: Int = (screenRect.left + center.first - compassBitmap.width).toInt()
        val frameTop: Int = (screenRect.top + center.second - compassBitmap.height).toInt()
        val frameRight: Int = (screenRect.left + center.first + compassBitmap.width).toInt()
        val frameBottom: Int = (screenRect.top + center.first + compassBitmap.height).toInt()

        // add padding by 2
        mapView.invalidateMapCoordinates(
            frameLeft - 2,
            frameTop - 2,
            frameRight + 2,
            frameBottom + 2
        )
    }
}
