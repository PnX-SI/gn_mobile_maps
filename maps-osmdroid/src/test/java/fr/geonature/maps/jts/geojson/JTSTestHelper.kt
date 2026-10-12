package fr.geonature.maps.jts.geojson

import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryCollection
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.LinearRing
import org.locationtech.jts.geom.MultiLineString
import org.locationtech.jts.geom.MultiPoint
import org.locationtech.jts.geom.MultiPolygon
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.Polygon

/**
 * Helper class about JTS.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
object JTSTestHelper {

    fun createCoordinate(
        latitude: Double,
        longitude: Double
    ): Coordinate {
        return Coordinate(
            longitude,
            latitude
        )
    }

    fun createPoint(
        gf: GeometryFactory,
        latitude: Double,
        longitude: Double
    ): Point {
        return gf.createPoint(
            createCoordinate(
                latitude,
                longitude
            )
        )
    }

    fun createMultiPoint(
        gf: GeometryFactory,
        vararg points: Point
    ): MultiPoint {
        return gf.createMultiPoint(points)
    }

    fun createLineString(
        gf: GeometryFactory,
        vararg coordinates: Coordinate
    ): LineString {
        return gf.createLineString(coordinates)
    }

    fun createMultiLineString(
        gf: GeometryFactory,
        vararg lineStrings: LineString
    ): MultiLineString {
        return gf.createMultiLineString(lineStrings)
    }

    fun createLinearRing(
        gf: GeometryFactory,
        vararg coordinates: Coordinate
    ): LinearRing {
        return gf.createLinearRing(coordinates)
    }

    fun createPolygon(
        gf: GeometryFactory,
        vararg coordinates: Coordinate
    ): Polygon {
        return gf.createPolygon(coordinates)
    }

    fun createPolygon(
        gf: GeometryFactory,
        shell: LinearRing,
        vararg holes: LinearRing
    ): Polygon {
        return gf.createPolygon(
            shell,
            holes
        )
    }

    fun createMultiPolygon(
        gf: GeometryFactory,
        vararg polygons: Polygon
    ): MultiPolygon {
        return gf.createMultiPolygon(polygons)
    }

    fun createGeometryCollection(
        gf: GeometryFactory,
        vararg geometries: Geometry
    ): GeometryCollection {
        return gf.createGeometryCollection(geometries)
    }
}
