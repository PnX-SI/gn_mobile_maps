package fr.geonature.maps.jts.geojson

import fr.geonature.maps.jts.geojson.GeometryUtils.distanceTo
import fr.geonature.maps.jts.geojson.GeometryUtils.getGeodesicArea
import fr.geonature.maps.jts.geojson.GeometryUtils.getGeodesicLength
import fr.geonature.maps.jts.geojson.JTSTestHelper.createCoordinate
import fr.geonature.maps.jts.geojson.JTSTestHelper.createLineString
import fr.geonature.maps.jts.geojson.JTSTestHelper.createLinearRing
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
    fun testDistanceBetweenTwoPoints() {
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
    fun testDistanceBetweenPointAndLineString() {
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
    fun testDistanceBetweenPointAndPolygon() {
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
    fun testDistanceBetweenTwoLineStrings() {
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
    fun testDistanceBetweenLineStringAndPolygon() {
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
    fun testDistanceBetweenTwoPolygons() {
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

        // then the computed distance should be roughly equals to 17m
        assertEquals(
            17.0,
            distance,
            1.0
        )
    }

    @Test
    fun testGeodesicLengthForLineString() {
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
    fun testGeodesicLengthForPolygon() {
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
    fun testGeodesicAreaForLineString() {
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
    fun testGeodesicAreaForSimplePolygon() {
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
    fun testGeodesicAreaForPolygonWithHoles() {
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
}
