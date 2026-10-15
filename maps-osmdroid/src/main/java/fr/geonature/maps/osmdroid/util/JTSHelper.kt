package fr.geonature.maps.osmdroid.util

import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.CoordinateSequence
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.osmdroid.util.GeoPoint

/**
 * Function helpers for JTS geometries and `GeoPoint`.
 *
 * @author S. Grimault
 */

/**
 * Creates a [GeoPoint] from [Point].
 */
fun Point.toGeoPoint(): GeoPoint {
    return coordinate.toGeoPoint()
}

/**
 * Creates a [GeoPoint] from [Coordinate].
 */
fun Coordinate.toGeoPoint(): GeoPoint {
    return GeoPoint(
        y,
        x
    )
}

/**
 * Creates a list of [GeoPoint] from [CoordinateSequence].
 */
fun CoordinateSequence.toGeoPoints(): List<GeoPoint> {
    return toCoordinateArray().map { it.toGeoPoint() }
}

/**
 * Creates a [Point] from [GeoPoint].
 */
fun GeoPoint.toPoint(): Point {
    return GeometryFactory().createPoint(
        Coordinate(
            longitude,
            latitude
        )
    )
}

