package fr.geonature.maps.jts.geojson

import android.util.Log
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.Polygon
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.constants.GeoConstants.RADIUS_EARTH_METERS
import org.osmdroid.views.util.constants.MathConstants.DEG2RAD
import kotlin.math.abs
import kotlin.math.sin

/**
 * Helper class about [Geometry] instances.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
object GeometryUtils {

    private val TAG = GeometryUtils::class.java.name

    /**
     * Creates a GeoPoint from Point.
     *
     * @param point the point to convert
     * @return GeoPoint
     */
    fun fromPoint(point: Point): GeoPoint {
        return GeoPoint(
            point.y,
            point.x
        )
    }

    /**
     * Creates a Point from GeoPoint.
     *
     * @param geoPoint the geo point to convert
     * @return Point
     */
    fun toPoint(geoPoint: GeoPoint): Point {
        return GeometryFactory().createPoint(
            Coordinate(
                geoPoint.longitude,
                geoPoint.latitude
            )
        )
    }

    /**
     * Returns the minimum distance between two `Geometry` instances.
     *
     * @param fromGeometry the `Geometry` to check the distance from
     * @param toGeometry   the `Geometry` to check the distance to
     * @return the minimum distance in meters
     * @see GeoPoint.distanceToAsDouble
     */
    fun distanceTo(
        fromGeometry: Geometry,
        toGeometry: Geometry
    ): Double {
        var distance = java.lang.Double.MAX_VALUE

        when (fromGeometry.geometryType) {
            "Point" -> {
                val fromGeoPoint = fromPoint(fromGeometry as Point)

                when (toGeometry.geometryType) {
                    "Point" -> distance =
                        fromGeoPoint.distanceToAsDouble(fromPoint(toGeometry as Point))
                    "LineString", "LinearRing" -> for (i in 0 until toGeometry.numPoints) {
                        val distanceFromPoint =
                            fromGeoPoint.distanceToAsDouble(fromPoint((toGeometry as LineString).getPointN(i)))

                        if (distance > distanceFromPoint) {
                            distance = distanceFromPoint
                        }
                    }
                    "Polygon" -> distance = distanceTo(
                        fromGeometry,
                        (toGeometry as Polygon).exteriorRing
                    )
                    else -> Log.w(
                        TAG,
                        "distanceTo: no implementation found for geometry '" + toGeometry.geometryType + "'"
                    )
                }
            }
            "LineString", "LinearRing" -> when (toGeometry.geometryType) {
                "Point" -> distance = distanceTo(
                    toGeometry,
                    fromGeometry
                )
                "LineString", "LinearRing" -> for (i in 0 until fromGeometry.numPoints) {
                    for (j in 0 until toGeometry.numPoints) {
                        val distanceFromPoint =
                            fromPoint((fromGeometry as LineString).getPointN(i)).distanceToAsDouble(fromPoint((toGeometry as LineString).getPointN(i)))

                        if (distance > distanceFromPoint) {
                            distance = distanceFromPoint
                        }
                    }
                }
                "Polygon" -> distance = distanceTo(
                    fromGeometry,
                    (toGeometry as Polygon).exteriorRing
                )
                else -> Log.w(
                    TAG,
                    "distanceTo: no implementation found for geometry '" + toGeometry.geometryType + "'"
                )
            }
            "Polygon" -> when (toGeometry.geometryType) {
                "Point" -> distance = distanceTo(
                    toGeometry,
                    fromGeometry
                )
                "LineString", "LinearRing" -> distance = distanceTo(
                    toGeometry,
                    fromGeometry
                )
                "Polygon" -> distance = distanceTo(
                    (toGeometry as Polygon).exteriorRing,
                    (fromGeometry as Polygon).exteriorRing
                )
                else -> Log.w(
                    TAG,
                    "distanceTo: no implementation found for geometry '" + toGeometry.geometryType + "'"
                )
            }
            else -> Log.w(
                TAG,
                "distanceTo: geometry " + fromGeometry.geometryType + " not implemented"
            )
        }

        return distance
    }

    /**
     * Calculate the approximate length of a given `Geometry` were it projected onto the Earth.
     *
     * @return the approximate geodesic length in meters.
     * @see GeoPoint.distanceToAsDouble
     */
    fun getGeodesicLength(geometry: Geometry): Double {
        var length = 0.0

        when (geometry.geometryType) {
            "LineString", "LinearRing" -> if (!geometry.isEmpty) {
                for (i in 1 until geometry.numPoints) {
                    length += fromPoint((geometry as LineString).getPointN(i)).distanceToAsDouble(fromPoint(geometry.getPointN(i - 1)))
                }
            }
            "Polygon" -> {
                val exteriorRing = (geometry as Polygon).exteriorRing

                length = getGeodesicLength(exteriorRing)

                // adds the last segment of the LineString if the last Point is not the same as the first Point
                if (!exteriorRing.isClosed) {
                    length += fromPoint(exteriorRing.endPoint).distanceToAsDouble(fromPoint(exteriorRing.startPoint))
                }
            }
            else -> Log.w(
                TAG,
                "getGeodesicLength: no implementation found for geometry '" + geometry.geometryType + "'"
            )
        }

        return length
    }

    /**
     * Calculates the approximate area of this `LineString` were it projected onto the Earth.
     *
     * **Note:** The `LineString` may be closed or not and seen as a `LinearRing`.
     *
     * @return the approximate geodesic area in square meters.
     * @see [http://trs-new.jpl.nasa.gov/dspace/handle/2014/40409](http://trs-new.jpl.nasa.gov/dspace/handle/2014/40409)
     *
     * @see GeoPoint.distanceToAsDouble
     */
    fun getGeodesicArea(lineString: LineString): Double {
        var area = 0.0

        // do not add the last point if it's the same as the first point
        for (i in 0 until if (lineString.isClosed) lineString.numPoints - 1 else lineString.numPoints) {
            val p1 = fromPoint(lineString.getPointN(i))
            val p2 = fromPoint(lineString.getPointN((i + 1) % lineString.numPoints))

            area += (p2.longitude - p1.longitude) * DEG2RAD * (2.0 + sin(p1.latitude * DEG2RAD) + sin(p2.latitude * DEG2RAD))
        }

        area = area * RADIUS_EARTH_METERS.toDouble() * RADIUS_EARTH_METERS.toDouble() / 2.0

        return abs(area)
    }

    /**
     * Calculates the approximate area of this `Polygon` were it projected onto the Earth.
     *
     * @param checkHoles also check if this `Polygon` contains holes and subtract the areas of
     * all these internal holes
     * @return the approximate geodesic area in square meters.
     * @see [http://trs-new.jpl.nasa.gov/dspace/handle/2014/40409](http://trs-new.jpl.nasa.gov/dspace/handle/2014/40409)
     *
     * @see .getGeodesicArea
     */
    fun getGeodesicArea(
        polygon: Polygon,
        checkHoles: Boolean
    ): Double {
        var area = 0.0

        if (polygon.isValid) {
            area = getGeodesicArea(polygon.exteriorRing)

            if (checkHoles) {
                for (i in 0 until polygon.numInteriorRing) {
                    area -= getGeodesicArea(polygon.getInteriorRingN(i))
                }
            }
        }

        return abs(area)
    }
}
