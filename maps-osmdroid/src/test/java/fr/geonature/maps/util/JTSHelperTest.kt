package fr.geonature.maps.util

import org.junit.Assert.assertEquals
import org.junit.Test
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.impl.CoordinateArraySequence
import org.osmdroid.util.GeoPoint

/**
 * Unit tests about `JTSHelper`.
 *
 * @author S. Grimault
 */
class JTSHelperTest {

    private val gf = GeometryFactory()

    @Test
    fun `should convert Point to GeoPoint`() {
        val geoPoint = gf.createPoint(
            Coordinate(
                -1.5545135,
                47.2256258
            )
        ).toGeoPoint()

        assertEquals(
            47.2256258,
            geoPoint.latitude,
            0.0
        )
        assertEquals(
            -1.5545135,
            geoPoint.longitude,
            0.0
        )
    }

    @Test
    fun `should convert Coordinate to GeoPoint`() {
        val geoPoint = Coordinate(
            -1.5545135,
            47.2256258
        ).toGeoPoint()

        assertEquals(
            47.2256258,
            geoPoint.latitude,
            0.0
        )
        assertEquals(
            -1.5545135,
            geoPoint.longitude,
            0.0
        )
    }

    @Test
    fun `should convert CoordinateSequence to GeoPoints`() {
        val geoPoints = CoordinateArraySequence(
            arrayOf(
                Coordinate(
                    -1.5,
                    47.2
                ),
                Coordinate(
                    -1.6,
                    47.3
                )
            )
        ).toGeoPoints()

        assertEquals(
            listOf(
                GeoPoint(
                    47.2,
                    -1.5
                ),
                GeoPoint(
                    47.3,
                    -1.6
                )
            ),
            geoPoints
        )
    }

    @Test
    fun `should convert empty CoordinateSequence to empty list`() {
        assertEquals(
            emptyList<GeoPoint>(),
            CoordinateArraySequence(arrayOf<Coordinate>()).toGeoPoints()
        )
    }

    @Test
    fun `should convert GeoPoint to Point`() {
        val point = GeoPoint(
            47.2256258,
            -1.5545135
        ).toPoint()

        assertEquals(
            -1.5545135,
            point.x,
            0.0
        )
        assertEquals(
            47.2256258,
            point.y,
            0.0
        )
    }
}

