package fr.geonature.maps.jts.geojson

import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryCollection
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.Polygon
import org.tinylog.kotlin.Logger
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Helper class about [Geometry] instances.
 *
 * @author S. Grimault
 */
object GeometryUtils {

    /**
     * The earth radius in meters.
     * See: [https://en.wikipedia.org/wiki/Earth_radius](https://en.wikipedia.org/wiki/Earth_radius)
     */
    private const val RADIUS_EARTH_METERS = 6378137.0

    private const val DEG2RAD = Math.PI / 180.0

    /**
     * Returns the distance between two coordinates (x: longitude, y: latitude) using the
     * [haversine formula](https://en.wikipedia.org/wiki/Haversine_formula) to calculate the
     * [great-circle distance](https://en.wikipedia.org/wiki/Great-circle_distance) between two
     * points – that is, the shortest distance over the earth’s surface – giving an
     * "as-the-crow-flies" distance between the points (ignoring any hills, of course!).
     *
     * @param from the first coordinate
     * @param to the second coordinate
     *
     * @return the distance in meters
     */
    fun distanceBetween(
        from: Coordinate,
        to: Coordinate
    ): Double {
        val lat1 = DEG2RAD * from.y
        val lat2 = DEG2RAD * to.y
        val a1 = sin((lat2 - lat1) / 2.0)
        val a2 = sin(DEG2RAD * (to.x - from.x) / 2.0)
        val a = a1 * a1 + cos(lat1) * cos(lat2) * a2 * a2

        return RADIUS_EARTH_METERS * 2.0 * asin(min(1.0, sqrt(a)))
    }

    /**
     * Returns the distance between two [Point]s.
     *
     * @return the distance in meters
     * @see GeometryUtils.distanceBetween(Coordinate, Coordinate)
     */
    fun distanceBetween(
        from: Point,
        to: Point
    ): Double {
        return distanceBetween(
            from.coordinate,
            to.coordinate
        )
    }

    /**
     * Returns the minimum distance between two `Geometry` instances.
     *
     * Returns `0.0` if both geometries intersect and [Double.MAX_VALUE] if one of them is empty.
     *
     * @param fromGeometry the `Geometry` to check the distance from
     * @param toGeometry the `Geometry` to check the distance to
     *
     * @return the minimum distance in meters
     *
     * @see GeometryUtils.distanceBetween(Coordinate, Coordinate)
     */
    fun distanceTo(
        fromGeometry: Geometry,
        toGeometry: Geometry
    ): Double {
        if (fromGeometry.isEmpty || toGeometry.isEmpty) return Double.MAX_VALUE

        if (fromGeometry is GeometryCollection) {
            return (0 until fromGeometry.numGeometries).minOf {
                distanceTo(
                    fromGeometry.getGeometryN(it),
                    toGeometry
                )
            }
        }

        if (toGeometry is GeometryCollection) {
            return (0 until toGeometry.numGeometries).minOf {
                distanceTo(
                    fromGeometry,
                    toGeometry.getGeometryN(it)
                )
            }
        }

        if (fromGeometry is Point) return distanceFromCoordinate(
            fromGeometry.coordinate,
            toGeometry
        )

        if (toGeometry is Point) return distanceFromCoordinate(
            toGeometry.coordinate,
            fromGeometry
        )

        if (fromGeometry.intersects(toGeometry)) return 0.0

        return min(
            fromGeometry.coordinates.minOf { distanceFromCoordinate(it, toGeometry) },
            toGeometry.coordinates.minOf { distanceFromCoordinate(it, fromGeometry) }
        )
    }

    /**
     * Calculate the approximate length of a given `Geometry` were it projected onto the Earth.
     *
     * @return the approximate geodesic length in meters.
     * @see GeometryUtils.distanceBetween(Coordinate, Coordinate)
     * @see GeometryUtils.distanceBetween(Point, Point)
     */
    fun getGeodesicLength(geometry: Geometry): Double {
        var length = 0.0

        when (geometry.geometryType) {
            "LineString", "LinearRing" -> if (!geometry.isEmpty) {
                val coordinates = geometry.coordinates

                for (i in 1 until coordinates.size) {
                    length += distanceBetween(
                        coordinates[i],
                        coordinates[i - 1]
                    )
                }
            }

            "Polygon" -> {
                val exteriorRing = (geometry as Polygon).exteriorRing

                length = getGeodesicLength(exteriorRing)

                // adds the last segment of the LineString if the last Point is not the same as the first Point
                if (!exteriorRing.isClosed) {
                    length += distanceBetween(
                        exteriorRing.endPoint,
                        exteriorRing.startPoint
                    )
                }
            }

            else -> Logger.warn { "getGeodesicLength: no implementation found for geometry '" + geometry.geometryType + "'" }
        }

        return length
    }

    /**
     * Calculates the approximate area of this `LineString` were it projected onto the Earth.
     *
     * **Note:** The `LineString` may be closed or not and seen as a `LinearRing`.
     *
     * @return the approximate geodesic area in square meters
     *
     * @see <a href="https://dataverse.jpl.nasa.gov/dataset.xhtml?persistentId=hdl:2014/41271">Chamberlain, Robert G.; Duquette, William H., 2007, "Some algorithms for polygons on a sphere.", Association of American Geographers Annual Meeting, San Francisco, California, April 17-21, 2007., JPL Open Repository</a>
     */
    fun getGeodesicArea(lineString: LineString): Double {
        var area = 0.0
        val coordinates = lineString.coordinates

        // do not add the last point if it's the same as the first point
        for (i in 0 until if (lineString.isClosed) coordinates.size - 1 else coordinates.size) {
            val p1 = coordinates[i]
            val p2 = coordinates[(i + 1) % coordinates.size]

            area += (p2.x - p1.x) * DEG2RAD * (2.0 + sin(p1.y * DEG2RAD) + sin(
                p2.y * DEG2RAD
            ))
        }

        area = area * RADIUS_EARTH_METERS * RADIUS_EARTH_METERS / 2.0

        return abs(area)
    }

    /**
     * Calculates the approximate area of this `Polygon` were it projected onto the Earth.
     *
     * @param checkHoles also check if this `Polygon` contains holes and subtract the areas of all
     * these internal holes
     * @return the approximate geodesic area in square meters
     *
     * @see <a href="https://dataverse.jpl.nasa.gov/dataset.xhtml?persistentId=hdl:2014/41271">Chamberlain, Robert G.; Duquette, William H., 2007, "Some algorithms for polygons on a sphere.", Association of American Geographers Annual Meeting, San Francisco, California, April 17-21, 2007., JPL Open Repository</a>
     * @see GeometryUtils.getGeodesicArea(LineString)
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

    /**
     * Returns the minimum distance in meters between a coordinate and a `Geometry`.
     */
    private fun distanceFromCoordinate(
        coordinate: Coordinate,
        geometry: Geometry
    ): Double {
        return when {
            geometry.isEmpty -> Double.MAX_VALUE
            geometry is GeometryCollection -> (0 until geometry.numGeometries).minOf {
                distanceFromCoordinate(
                    coordinate,
                    geometry.getGeometryN(it)
                )
            }

            geometry is Point -> distanceBetween(
                coordinate,
                geometry.coordinate
            )

            geometry is LineString -> distanceToPolyline(
                coordinate,
                geometry.coordinates
            )

            geometry is Polygon -> {
                if (geometry.contains(geometry.factory.createPoint(coordinate))) 0.0
                else (0 until geometry.numInteriorRing).fold(
                    distanceToPolyline(
                        coordinate,
                        geometry.exteriorRing.coordinates
                    )
                ) { acc, i ->
                    min(
                        acc,
                        distanceToPolyline(
                            coordinate,
                            geometry.getInteriorRingN(i).coordinates
                        )
                    )
                }
            }

            else -> {
                Logger.warn { "distanceTo: geometry " + geometry.geometryType + " not implemented" }
                Double.MAX_VALUE
            }
        }
    }

    private fun distanceToPolyline(
        coordinate: Coordinate,
        polyline: Array<Coordinate>
    ): Double {
        if (polyline.isEmpty()) return Double.MAX_VALUE
        if (polyline.size == 1) return distanceBetween(
            coordinate,
            polyline[0]
        )

        return (1 until polyline.size).minOf {
            distanceToSegment(
                coordinate,
                polyline[it - 1],
                polyline[it]
            )
        }
    }

    /**
     * Returns the distance in meters between a coordinate and a segment, using a local projection
     * in meters centered on this coordinate.
     */
    private fun distanceToSegment(
        coordinate: Coordinate,
        start: Coordinate,
        end: Coordinate
    ): Double {
        val cosLat = cos(coordinate.y * DEG2RAD)

        // segment in a local metric frame, the coordinate being the origin
        val ax = (start.x - coordinate.x) * DEG2RAD * RADIUS_EARTH_METERS * cosLat
        val ay = (start.y - coordinate.y) * DEG2RAD * RADIUS_EARTH_METERS
        val bx = (end.x - coordinate.x) * DEG2RAD * RADIUS_EARTH_METERS * cosLat
        val by = (end.y - coordinate.y) * DEG2RAD * RADIUS_EARTH_METERS

        val dx = bx - ax
        val dy = by - ay
        val lengthSquared = dx * dx + dy * dy

        if (lengthSquared == 0.0) return distanceBetween(
            coordinate,
            start
        )

        val t = -(ax * dx + ay * dy) / lengthSquared

        return when {
            t <= 0.0 -> distanceBetween(
                coordinate,
                start
            )

            t >= 1.0 -> distanceBetween(
                coordinate,
                end
            )

            else -> {
                val px = ax + t * dx
                val py = ay + t * dy

                sqrt(px * px + py * py)
            }
        }
    }
}

