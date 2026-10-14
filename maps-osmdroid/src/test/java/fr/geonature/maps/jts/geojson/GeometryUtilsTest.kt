package fr.geonature.maps.jts.geojson

import fr.geonature.maps.jts.geojson.GeometryUtils.distanceTo
import fr.geonature.maps.jts.geojson.GeometryUtils.getGeodesicArea
import fr.geonature.maps.jts.geojson.GeometryUtils.getGeodesicLength
import fr.geonature.maps.jts.geojson.JTSTestHelper.createCoordinate
import fr.geonature.maps.jts.geojson.JTSTestHelper.createLineString
import fr.geonature.maps.jts.geojson.JTSTestHelper.createLinearRing
import fr.geonature.maps.jts.geojson.JTSTestHelper.createMultiPoint
import fr.geonature.maps.jts.geojson.JTSTestHelper.createPoint
import fr.geonature.maps.jts.geojson.JTSTestHelper.createPolygon
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.locationtech.jts.geom.GeometryFactory
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [GeometryUtils].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class GeometryUtilsTest {

    private lateinit var gf: GeometryFactory

    @Before
    fun setUp() {
        gf = GeometryFactory()
    }

    @Test
    fun `should compute distance between two points`() {
        // given two Points
        val distance = distanceTo(
            createPoint(
                gf,
                47.225782,
                -1.554476
            ),
            createPoint(
                gf,
                47.226468,
                -1.554996
            )
        )

        // then the computed distance should be roughly equals to 86m
        assertEquals(
            86.0,
            distance,
            1.0
        )
    }

    @Test
    fun `should compute distance between point and line string`() {
        // given Point and LineString
        val distance1 = distanceTo(
            createPoint(
                gf,
                47.225782,
                -1.554476
            ),
            createLineString(
                gf,
                createCoordinate(
                    47.226468,
                    -1.554996
                ),
                createCoordinate(
                    47.226600,
                    -1.554846
                )
            )
        )

        // then the computed distance should be roughly equals to 86m
        assertEquals(
            86.0,
            distance1,
            1.0
        )

        // given LineString and Point
        val distance2 = distanceTo(
            createLineString(
                gf,
                createCoordinate(
                    47.226468,
                    -1.554996
                ),
                createCoordinate(
                    47.226600,
                    -1.554846
                )
            ),
            createPoint(
                gf,
                47.225782,
                -1.554476
            )
        )

        // then the computed distance should be roughly equals to 86m
        assertEquals(
            86.0,
            distance2,
            1.0
        )
    }

    @Test
    fun `should compute distance between point and polygon`() {
        // given Point and Polygon
        val distance1 = distanceTo(
            createPoint(
                gf,
                47.225782,
                -1.554476
            ),
            createPolygon(
                gf,
                createCoordinate(
                    47.226219,
                    -1.554430
                ),
                createCoordinate(
                    47.226237,
                    -1.554261
                ),
                createCoordinate(
                    47.226122,
                    -1.554245
                ),
                createCoordinate(
                    47.226106,
                    -1.554411
                ),
                createCoordinate(
                    47.226219,
                    -1.554430
                )
            )
        )

        // then the computed distance should be roughly equals to 36m
        assertEquals(
            36.0,
            distance1,
            1.0
        )

        // given Polygon and Point
        val distance2 = distanceTo(
            createPolygon(
                gf,
                createCoordinate(
                    47.226219,
                    -1.554430
                ),
                createCoordinate(
                    47.226237,
                    -1.554261
                ),
                createCoordinate(
                    47.226122,
                    -1.554245
                ),
                createCoordinate(
                    47.226106,
                    -1.554411
                ),
                createCoordinate(
                    47.226219,
                    -1.554430
                )
            ),
            createPoint(
                gf,
                47.225782,
                -1.554476
            )
        )

        // then the computed distance should be roughly equals to 36m
        assertEquals(
            36.0,
            distance2,
            1.0
        )
    }

    @Test
    fun `should compute distance between two line strings`() {
        // given two LineStrings
        val distance = distanceTo(
            createLineString(
                gf,
                createCoordinate(
                    47.226468,
                    -1.554996
                ),
                createCoordinate(
                    47.226600,
                    -1.554846
                )
            ),
            createLineString(
                gf,
                createCoordinate(
                    47.226219,
                    -1.554430
                ),
                createCoordinate(
                    47.226237,
                    -1.554261
                ),
                createCoordinate(
                    47.226122,
                    -1.554245
                )
            )
        )

        // then the computed distance should be roughly equals to 50m
        assertEquals(
            50.0,
            distance,
            1.0
        )
    }

    @Test
    fun `should compute distance between line string and polygon`() {
        // given LineString and Polygon
        val distance1 = distanceTo(
            createLineString(
                gf,
                createCoordinate(
                    47.226468,
                    -1.554996
                ),
                createCoordinate(
                    47.226600,
                    -1.554846
                )
            ),
            createPolygon(
                gf,
                createCoordinate(
                    47.226219,
                    -1.554430
                ),
                createCoordinate(
                    47.226237,
                    -1.554261
                ),
                createCoordinate(
                    47.226122,
                    -1.554245
                ),
                createCoordinate(
                    47.226106,
                    -1.554411
                ),
                createCoordinate(
                    47.226219,
                    -1.554430
                )
            )
        )

        // then the computed distance should be roughly equals to 50m
        assertEquals(
            50.0,
            distance1,
            1.0
        )

        // given Polygon and LineString
        val distance2 = distanceTo(
            createPolygon(
                gf,
                createCoordinate(
                    47.226219,
                    -1.554430
                ),
                createCoordinate(
                    47.226237,
                    -1.554261
                ),
                createCoordinate(
                    47.226122,
                    -1.554245
                ),
                createCoordinate(
                    47.226106,
                    -1.554411
                ),
                createCoordinate(
                    47.226219,
                    -1.554430
                )
            ),
            createLineString(
                gf,
                createCoordinate(
                    47.226468,
                    -1.554996
                ),
                createCoordinate(
                    47.226600,
                    -1.554846
                )
            )
        )

        // then the computed distance should be roughly equals to 50m
        assertEquals(
            50.0,
            distance2,
            1.0
        )
    }

    @Test
    fun `should compute distance between two polygons`() {
        // given two Polygons
        val distance = distanceTo(
            createPolygon(
                gf,
                createCoordinate(
                    47.226116,
                    -1.554169
                ),
                createCoordinate(
                    47.226126,
                    -1.554097
                ),
                createCoordinate(
                    47.225527,
                    -1.553986
                ),
                createCoordinate(
                    47.225519,
                    -1.554061
                ),
                createCoordinate(
                    47.226116,
                    -1.554169
                )
            ),
            createPolygon(
                gf,
                createCoordinate(
                    47.226219,
                    -1.554430
                ),
                createCoordinate(
                    47.226237,
                    -1.554261
                ),
                createCoordinate(
                    47.226122,
                    -1.554245
                ),
                createCoordinate(
                    47.226106,
                    -1.554411
                ),
                createCoordinate(
                    47.226219,
                    -1.554430
                )
            )
        )

        // then the computed distance should be roughly equals to 5.8m
        assertEquals(
            5.8,
            distance,
            1.0
        )
    }

    @Test
    fun `should compute geodesic length for line string`() {
        // when computing the geodesic length of a given LineString
        val length = getGeodesicLength(
            createLineString(
                gf,
                createCoordinate(
                    47.226116,
                    -1.554169
                ),
                createCoordinate(
                    47.226126,
                    -1.554097
                ),
                createCoordinate(
                    47.225527,
                    -1.553986
                )
            )
        )

        // then the computed length should be roughly equals to 72m
        assertEquals(
            72.0,
            length,
            1.0
        )
    }

    @Test
    fun `should compute geodesic length for polygon`() {
        // when computing the geodesic length of a given LineString
        val length = getGeodesicLength(
            createPolygon(
                gf,
                createCoordinate(
                    47.226116,
                    -1.554169
                ),
                createCoordinate(
                    47.226126,
                    -1.554097
                ),
                createCoordinate(
                    47.225527,
                    -1.553986
                ),
                createCoordinate(
                    47.225519,
                    -1.554061
                ),
                createCoordinate(
                    47.226116,
                    -1.554169
                )
            )
        )

        // then the computed length should be roughly equals to 145m
        assertEquals(
            145.0,
            length,
            1.0
        )
    }

    @Test
    fun `should compute geodesic area for line string`() {
        // when computing the geodesic area of a given LineString (no closed)
        val area1 = getGeodesicArea(
            createLineString(
                gf,
                createCoordinate(
                    47.226116,
                    -1.554169
                ),
                createCoordinate(
                    47.226126,
                    -1.554097
                ),
                createCoordinate(
                    47.225527,
                    -1.553986
                )
            )
        )

        // then the computed area should be roughly equals to 186m
        assertEquals(
            186.0,
            area1,
            1.0
        )

        // when computing the geodesic area of the same LineString but closed
        val area2 = getGeodesicArea(
            createLineString(
                gf,
                createCoordinate(
                    47.226116,
                    -1.554169
                ),
                createCoordinate(
                    47.226126,
                    -1.554097
                ),
                createCoordinate(
                    47.225527,
                    -1.553986
                ),
                createCoordinate(
                    47.226116,
                    -1.554169
                )
            )
        )
        // then the computed area should be the same
        assertEquals(
            area1,
            area2,
            0.0
        )
    }

    @Test
    fun `should compute geodesic area for simple polygon`() {
        // when computing the geodesic area of a given Polygon
        val area = getGeodesicArea(
            createPolygon(
                gf,
                createCoordinate(
                    47.226116,
                    -1.554169
                ),
                createCoordinate(
                    47.226126,
                    -1.554097
                ),
                createCoordinate(
                    47.225527,
                    -1.553986
                ),
                createCoordinate(
                    47.225519,
                    -1.554061
                ),
                createCoordinate(
                    47.226116,
                    -1.554169
                )
            ),
            false
        )

        // then the computed area should be roughly equals to 378m
        assertEquals(
            378.0,
            area,
            1.0
        )
    }

    @Test
    fun `should compute geodesic area for polygon with holes`() {
        // when computing the geodesic area of a given Polygon with holes
        val areaWithHole = getGeodesicArea(
            createPolygon(
                gf,
                createLinearRing(
                    gf,
                    createCoordinate(
                        47.226257,
                        -1.554564
                    ),
                    createCoordinate(
                        47.226295,
                        -1.554202
                    ),
                    createCoordinate(
                        47.226075,
                        -1.554169
                    ),
                    createCoordinate(
                        47.226049,
                        -1.554496
                    ),
                    createCoordinate(
                        47.226257,
                        -1.554564
                    )
                ),
                createLinearRing(
                    gf,
                    createCoordinate(
                        47.226219,
                        -1.554430
                    ),
                    createCoordinate(
                        47.226237,
                        -1.554261
                    ),
                    createCoordinate(
                        47.226122,
                        -1.554245
                    ),
                    createCoordinate(
                        47.226106,
                        -1.554411
                    ),
                    createCoordinate(
                        47.226219,
                        -1.554430
                    )
                )
            ),
            true
        )

        // then the computed area should be roughly equals to 470m
        assertEquals(
            470.0,
            areaWithHole,
            1.0
        )

        // when computing the geodesic area of the same Polygon but without checking holes
        val area = getGeodesicArea(
            createPolygon(
                gf,
                createLinearRing(
                    gf,
                    createCoordinate(
                        47.226257,
                        -1.554564
                    ),
                    createCoordinate(
                        47.226295,
                        -1.554202
                    ),
                    createCoordinate(
                        47.226075,
                        -1.554169
                    ),
                    createCoordinate(
                        47.226049,
                        -1.554496
                    ),
                    createCoordinate(
                        47.226257,
                        -1.554564
                    )
                ),
                createLinearRing(
                    gf,
                    createCoordinate(
                        47.226219,
                        -1.554430
                    ),
                    createCoordinate(
                        47.226237,
                        -1.554261
                    ),
                    createCoordinate(
                        47.226122,
                        -1.554245
                    ),
                    createCoordinate(
                        47.226106,
                        -1.554411
                    ),
                    createCoordinate(
                        47.226219,
                        -1.554430
                    )
                )
            ),
            false
        )

        // then the computed area should be roughly equals to 634m
        assertEquals(
            634.0,
            area,
            1.0
        )
    }

    @Test
    fun `should compute distance between two coordinates`() {
        assertEquals(
            111.3,
            GeometryUtils.distanceBetween(
                createCoordinate(0.0, 0.0),
                createCoordinate(0.001, 0.0)
            ),
            0.5
        )
    }

    @Test
    fun `should compute distance between point inside polygon`() {
        assertEquals(
            0.0,
            distanceTo(
                createPoint(gf, 0.0005, 0.0005),
                createPolygon(gf, *squareCoordinates)
            ),
            0.0
        )
    }

    @Test
    fun `should compute distance between point inside polygon hole`() {
        val polygon = createPolygon(
            gf,
            createLinearRing(
                gf,
                createCoordinate(0.0, 0.0),
                createCoordinate(0.0, 0.01),
                createCoordinate(0.01, 0.01),
                createCoordinate(0.01, 0.0),
                createCoordinate(0.0, 0.0)
            ),
            createLinearRing(
                gf,
                createCoordinate(0.004, 0.004),
                createCoordinate(0.004, 0.006),
                createCoordinate(0.006, 0.006),
                createCoordinate(0.006, 0.004),
                createCoordinate(0.004, 0.004)
            )
        )

        assertEquals(
            111.3,
            distanceTo(
                createPoint(gf, 0.005, 0.005),
                polygon
            ),
            1.0
        )
    }

    @Test
    fun `should compute distance between intersecting line strings`() {
        assertEquals(
            0.0,
            distanceTo(
                createLineString(
                    gf,
                    createCoordinate(0.0, 0.0),
                    createCoordinate(0.001, 0.001)
                ),
                createLineString(
                    gf,
                    createCoordinate(0.0, 0.001),
                    createCoordinate(0.001, 0.0)
                )
            ),
            0.0
        )
    }

    @Test
    fun `should compute distance between point and line string segment`() {
        val lineString = createLineString(
            gf,
            createCoordinate(0.0, 0.0),
            createCoordinate(0.0, 0.001)
        )

        // perpendicular to the middle of the segment
        assertEquals(
            111.3,
            distanceTo(
                createPoint(gf, 0.001, 0.0005),
                lineString
            ),
            1.0
        )

        // beyond the end of the segment
        assertEquals(
            111.3,
            distanceTo(
                createPoint(gf, 0.0, 0.002),
                lineString
            ),
            1.0
        )
    }

    @Test
    fun `should compute distance between point and multi point`() {
        assertEquals(
            111.3,
            distanceTo(
                createPoint(gf, 0.0, 0.0),
                createMultiPoint(
                    gf,
                    createPoint(gf, 0.001, 0.0),
                    createPoint(gf, 0.01, 0.0)
                )
            ),
            1.0
        )
    }

    @Test
    fun `should compute distance with empty geometry`() {
        assertEquals(
            Double.MAX_VALUE,
            distanceTo(
                createPoint(gf, 0.0, 0.0),
                gf.createPoint()
            ),
            0.0
        )
    }

    // 0.001 degree of latitude is roughly equals to 111.3m
    private val squareCoordinates = arrayOf(
        createCoordinate(0.0, 0.0),
        createCoordinate(0.0, 0.001),
        createCoordinate(0.001, 0.001),
        createCoordinate(0.001, 0.0),
        createCoordinate(0.0, 0.0)
    )
}
