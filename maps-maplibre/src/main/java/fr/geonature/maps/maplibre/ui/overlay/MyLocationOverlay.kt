package fr.geonature.maps.maplibre.ui.overlay

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.tinylog.kotlin.Logger
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow

/**
 * Listener about location changes resolved by [MyLocationOverlay].
 */
interface MyLocationListener {
    fun onLocationChanged(location: Location)
    fun onLocationOutsideBoundaries(location: Location)
}

/**
 * Shows on the map the current user position, as MapLibre layers:
 * - a translucent circle whose radius is the position accuracy (the larger, the less accurate);
 * - a plain circle for the position itself;
 * - a small triangular arrow around it for the device orientation.
 *
 * Requires `ACCESS_FINE_LOCATION`: without it nothing is resolved.
 *
 * @author S. Grimault
 */
class MyLocationOverlay(
    private val context: Context,
    private val maxBounds: LatLngBounds? = null
) : LocationListener, SensorEventListener {

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val density = context.resources.displayMetrics.density

    private var style: Style? = null
    private var listener: MyLocationListener? = null
    private var location: Location? = null
    private var orientation: Float? = null
    private var updating = false
    private var visible = false

    private val rotationMatrix = FloatArray(9)

    fun setMyLocationListener(listener: MyLocationListener?) {
        this.listener = listener
    }

    /**
     * Adds the overlay layers to the given (freshly loaded) style.
     */
    fun onStyleLoaded(style: Style) {
        this.style = style

        style.addImage(
            ARROW_IMAGE_ID,
            createArrowBitmap()
        )
        style.addSource(
            GeoJsonSource(
                SOURCE_ID,
                EMPTY_FEATURE_COLLECTION
            )
        )
        style.addLayer(
            CircleLayer(
                ACCURACY_LAYER_ID,
                SOURCE_ID
            ).withProperties(
                PropertyFactory.circleRadius(0f),
                PropertyFactory.circleColor(COLOR),
                PropertyFactory.circleOpacity(0.19f),
                PropertyFactory.circleStrokeColor(COLOR),
                PropertyFactory.circleStrokeOpacity(0.38f),
                PropertyFactory.circleStrokeWidth(1f),
                PropertyFactory.circlePitchAlignment(Property.CIRCLE_PITCH_ALIGNMENT_MAP)
            )
        )
        style.addLayer(
            CircleLayer(
                POSITION_LAYER_ID,
                SOURCE_ID
            ).withProperties(
                PropertyFactory.circleRadius(POSITION_RADIUS_DP),
                PropertyFactory.circleColor(COLOR),
                PropertyFactory.circleStrokeColor(Color.WHITE),
                PropertyFactory.circleStrokeWidth(3f)
            )
        )
        style.addLayer(
            SymbolLayer(
                ARROW_LAYER_ID,
                SOURCE_ID
            ).withProperties(
                PropertyFactory.iconImage(ARROW_IMAGE_ID),
                PropertyFactory.iconRotate(Expression.get(PROPERTY_BEARING)),
                PropertyFactory.iconRotationAlignment(Property.ICON_ROTATION_ALIGNMENT_MAP),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconIgnorePlacement(true)
            )
        )

        refresh()
    }

    /**
     * Returns the last known location: the last resolved one or the most recent among the
     * enabled location providers, if inside the maximum bounds.
     */
    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(): Location? {
        location?.also { return it }

        if (!hasPermission()) return null

        val locationManager = locationManager ?: return null

        return PROVIDERS.mapNotNull {
            runCatching { locationManager.getLastKnownLocation(it) }.getOrNull()
        }
            .maxWithOrNull(compareBy<Location> { it.time }.thenBy { -it.accuracy })
            ?.takeIf { isInsideBounds(it) }
    }

    /**
     * Shows the last known location without listening to location updates.
     *
     * @return the shown location or `null` if none
     */
    fun showLastKnownLocation(): Location? {
        stopUpdates()

        val lastKnownLocation = getLastKnownLocation() ?: return null

        location = lastKnownLocation
        visible = true
        refresh()

        return lastKnownLocation
    }

    /**
     * Shows the position and keeps it up to date.
     *
     * @return `true` if updates are running
     */
    @SuppressLint("MissingPermission")
    fun startUpdates(): Boolean {
        if (updating) return true
        if (!hasPermission()) return false

        val locationManager = locationManager ?: return false

        var started = false

        PROVIDERS.forEach {
            try {
                if (locationManager.isProviderEnabled(it)) {
                    locationManager.requestLocationUpdates(
                        it,
                        MIN_TIME_MS,
                        MIN_DISTANCE_M,
                        this
                    )
                    started = true
                }
            } catch (e: Exception) {
                Logger.warn(e) { "failed to request location updates from '$it'" }
            }
        }

        if (!started) return false

        rotationSensor?.also {
            sensorManager?.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_UI
            )
        }

        updating = true
        visible = true

        getLastKnownLocation()?.also { onLocationChanged(it) }
        refresh()

        return true
    }

    /**
     * Stops location updates, keeping the marker on the map.
     */
    fun stopUpdates() {
        if (!updating) return

        locationManager?.removeUpdates(this)
        sensorManager?.unregisterListener(this)
        orientation = null
        updating = false
        refresh()
    }

    /**
     * Stops location updates and removes the marker from the map.
     */
    fun hide() {
        stopUpdates()
        visible = false
        location = null
        refresh()
    }

    fun onDestroy() {
        locationManager?.removeUpdates(this)
        sensorManager?.unregisterListener(this)
        updating = false
        style = null
    }

    override fun onLocationChanged(location: Location) {
        if (!isInsideBounds(location)) {
            listener?.onLocationOutsideBoundaries(location)

            return
        }

        this.location = location
        refresh()
        listener?.onLocationChanged(location)
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(
        provider: String?,
        status: Int,
        extras: Bundle?
    ) {
        // nothing to do
    }

    override fun onProviderEnabled(provider: String) {
        // nothing to do
    }

    override fun onProviderDisabled(provider: String) {
        // nothing to do
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return

        SensorManager.getRotationMatrixFromVector(
            rotationMatrix,
            event.values
        )
        val azimuth = headingFromRotationMatrix(rotationMatrix)
        val previous = orientation

        if (previous != null && angularDistance(
                previous,
                azimuth
            ) < MIN_ORIENTATION_DELTA
        ) return

        orientation = azimuth
        refresh()
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int
    ) {
        // nothing to do
    }

    private fun hasPermission() =
        context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun isInsideBounds(location: Location) = maxBounds?.contains(
        LatLng(
            location.latitude,
            location.longitude
        )
    ) ?: true

    private fun refresh() {
        val style = style ?: return
        if (!style.isFullyLoaded) return

        val source = style.getSourceAs<GeoJsonSource>(SOURCE_ID) ?: return
        val location = location

        if (!visible || location == null) {
            source.setGeoJson(EMPTY_FEATURE_COLLECTION)

            return
        }

        source.setGeoJson(
            toFeature(
                location,
                orientation
            )
        )

        style.getLayerAs<CircleLayer>(ACCURACY_LAYER_ID)
            ?.setProperties(
                PropertyFactory.circleRadius(
                    accuracyRadiusExpression(
                        location.accuracy,
                        location.latitude
                    )
                )
            )
        style.getLayerAs<SymbolLayer>(ARROW_LAYER_ID)
            ?.setProperties(PropertyFactory.visibility(if (orientation != null) Property.VISIBLE else Property.NONE))
    }

    private fun createArrowBitmap(): Bitmap {
        val size = (ARROW_IMAGE_SIZE_DP * density).toInt()
        val bitmap = Bitmap.createBitmap(
            context.resources.displayMetrics,
            size,
            size,
            Bitmap.Config.ARGB_8888
        )
        val center = size / 2f
        val baseY = center - (POSITION_RADIUS_DP + 4f) * density
        val halfWidth = 5f * density

        // the image is centered on the position: the arrow head sits on top of the position circle
        val path = Path().apply {
            moveTo(
                center,
                baseY - 8f * density
            )
            lineTo(
                center + halfWidth,
                baseY
            )
            lineTo(
                center - halfWidth,
                baseY
            )
            close()
        }

        Canvas(bitmap).drawPath(
            path,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLUE
                alpha = 200
                style = Paint.Style.FILL
            })

        return bitmap
    }

    companion object {
        internal const val SOURCE_ID = "geonature-my-location-source"
        internal const val ACCURACY_LAYER_ID = "geonature-my-location-accuracy"
        internal const val POSITION_LAYER_ID = "geonature-my-location-position"
        internal const val ARROW_LAYER_ID = "geonature-my-location-arrow"
        internal const val ARROW_IMAGE_ID = "geonature-my-location-arrow-image"
        internal const val PROPERTY_BEARING = "bearing"

        private const val COLOR = "#0000FF"
        private const val POSITION_RADIUS_DP = 8f
        private const val ARROW_IMAGE_SIZE_DP = 48
        private const val MIN_TIME_MS = 10 * 1000L
        private const val MIN_DISTANCE_M = 2f
        private const val MIN_ORIENTATION_DELTA = 3f
        private const val EMPTY_FEATURE_COLLECTION =
            """{"type":"FeatureCollection","features":[]}"""
        private const val MAX_ZOOM = 24
        private const val FLAT_THRESHOLD = 0.7f
        private val PROVIDERS = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER
        )

        // ground resolution at the equator for zoom level 0 and 512px tiles (in meters per pixel)
        private const val METERS_PER_PIXEL_AT_ZOOM_0 = 78271.51696402048

        /**
         * Converts a distance in meters to pixels at the given latitude and zoom level.
         */
        internal fun metersToPixels(
            meters: Float,
            latitude: Double,
            zoom: Double
        ): Double {
            val metersPerPixel =
                METERS_PER_PIXEL_AT_ZOOM_0 * cos(Math.toRadians(latitude)) / 2.0.pow(zoom)

            return if (metersPerPixel <= 0.0) 0.0 else meters / metersPerPixel
        }

        /**
         * The accuracy circle radius (in pixels) is proportional to `2^zoom`: an exponential
         * interpolation of base 2 between two zoom levels reproduces it exactly.
         */
        internal fun accuracyRadiusExpression(
            accuracy: Float,
            latitude: Double
        ): Expression = Expression.interpolate(
            Expression.exponential(2f),
            Expression.zoom(),
            Expression.stop(
                0,
                metersToPixels(
                    accuracy,
                    latitude,
                    0.0
                )
            ),
            Expression.stop(
                MAX_ZOOM,
                metersToPixels(
                    accuracy,
                    latitude,
                    MAX_ZOOM.toDouble()
                )
            )
        )

        internal fun toFeature(
            location: Location,
            orientation: Float?
        ): String = String.format(
            Locale.ROOT,
            """{"type":"Feature","properties":{"%s":%.1f},"geometry":{"type":"Point","coordinates":[%.8f,%.8f]}}""",
            PROPERTY_BEARING,
            orientation ?: 0f,
            location.longitude,
            location.latitude
        )

        /**
         * Returns the compass heading (in degrees, clockwise from north) of where the device is
         * facing: the top edge of the device when held flat, otherwise (held upright, the usual
         * way to read a map) the direction opposite to the screen normal, i.e. the one the back of
         * the device points to. The raw azimuth of the top edge is unreliable when upright.
         */
        internal fun headingFromRotationMatrix(rotationMatrix: FloatArray): Float {
            val flat = abs(rotationMatrix[8]) > FLAT_THRESHOLD
            val east = if (flat) rotationMatrix[1] else -rotationMatrix[2]
            val north = if (flat) rotationMatrix[4] else -rotationMatrix[5]

            return normalize(Math.toDegrees(atan2(east.toDouble(), north.toDouble())).toFloat())
        }

        internal fun normalize(degrees: Float): Float = ((degrees % 360f) + 360f) % 360f

        internal fun angularDistance(
            a: Float,
            b: Float
        ): Float {
            val delta = abs(a - b) % 360f

            return if (delta > 180f) 360f - delta else delta
        }
    }
}
