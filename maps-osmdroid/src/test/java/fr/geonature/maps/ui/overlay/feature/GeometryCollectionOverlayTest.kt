package fr.geonature.maps.ui.overlay.feature

import fr.geonature.maps.jts.geojson.JTSTestHelper.createCoordinate
import fr.geonature.maps.jts.geojson.JTSTestHelper.createGeometryCollection
import fr.geonature.maps.jts.geojson.JTSTestHelper.createLineString
import fr.geonature.maps.jts.geojson.JTSTestHelper.createLinearRing
import fr.geonature.maps.jts.geojson.JTSTestHelper.createMultiLineString
import fr.geonature.maps.jts.geojson.JTSTestHelper.createMultiPoint
import fr.geonature.maps.jts.geojson.JTSTestHelper.createMultiPolygon
import fr.geonature.maps.jts.geojson.JTSTestHelper.createPoint
import fr.geonature.maps.jts.geojson.JTSTestHelper.createPolygon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.locationtech.jts.geom.GeometryFactory
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [GeometryCollectionOverlay].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class GeometryCollectionOverlayTest {

    private lateinit var gf: GeometryFactory

    @Before
    fun setUp() {
        gf = GeometryFactory()
    }

    @Test
    fun testCreateOverlayFromMultiPoint() {
        // given MultiPoint
        val multiPoints = createMultiPoint(
            gf,
            createPoint(
                gf,
                47.2256258,
                -1.5545135
            ),
            createPoint(
                gf,
                47.225136,
                -1.553913
            )
        )

        // when create Overlay from MultiPoint
        val geometryCollectionOverlay =
            GeometryCollectionOverlay().apply { setGeometry(multiPoints) }

        // then
        assertEquals(
            2,
            geometryCollectionOverlay.backendOverlay.items.size
        )
        geometryCollectionOverlay.backendOverlay.items.forEach {
            assertTrue(it is CirclePointOverlay)
        }
    }

    @Test
    fun testCreateOverlayFromMultiLineString() {
        // given MultiLineString
        val multiLineString = createMultiLineString(
            gf,
            createLineString(
                gf,
                createCoordinate(
                    47.2256258,
                    -1.5545135
                ),
                createCoordinate(
                    47.225136,
                    -1.553913
                )
            )
        )

        // when create Overlay from MultiLineString
        val geometryCollectionOverlay =
            GeometryCollectionOverlay().apply { setGeometry(multiLineString) }

        // then
        assertEquals(
            1,
            geometryCollectionOverlay.backendOverlay.items.size
        )
        geometryCollectionOverlay.backendOverlay.items.forEach {
            assertTrue(it is LineStringOverlay)
        }
    }

    @Test
    fun testCreateOverlayFromMultiPolygon() {
        // given MultiPolygon
        val multiPolygon = createMultiPolygon(
            gf,
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
            )
        )

        // when create Overlay from MultiPolygon
        val geometryCollectionOverlay =
            GeometryCollectionOverlay().apply { setGeometry(multiPolygon) }

        // then
        assertEquals(
            2,
            geometryCollectionOverlay.backendOverlay.items.size
        )
        geometryCollectionOverlay.backendOverlay.items.forEach {
            assertTrue(it is PolygonOverlay)
        }
    }

    @Test
    fun testCreateOverlayFromEmptyGeometryCollection() {
        // given an empty GeometryCollection
        val multiPoints = createMultiPoint(gf)

        // when create Overlay from MultiPoint
        val geometryCollectionOverlay =
            GeometryCollectionOverlay().apply { setGeometry(multiPoints) }

        // then
        assertTrue(geometryCollectionOverlay.backendOverlay.items.isEmpty())
    }

    @Test
    fun testCreateOverlayFromGeometryCollection() {
        // given a GeometryCollection
        val geometryCollection = createGeometryCollection(
            gf,
            createPoint(
                gf,
                47.2256258,
                -1.5545135
            ),
            createMultiPoint(
                gf,
                createPoint(
                    gf,
                    47.2256258,
                    -1.5545135
                ),
                createPoint(
                    gf,
                    47.225136,
                    -1.553913
                )
            ),
            createLineString(
                gf,
                createCoordinate(
                    47.2256258,
                    -1.5545135
                ),
                createCoordinate(
                    47.225136,
                    -1.553913
                )
            ),
            createMultiLineString(
                gf,
                createLineString(
                    gf,
                    createCoordinate(
                        47.2256258,
                        -1.5545135
                    ),
                    createCoordinate(
                        47.225136,
                        -1.553913
                    )
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

        // when create Overlay from GeometryCollection
        val geometryCollectionOverlay =
            GeometryCollectionOverlay().apply { setGeometry(geometryCollection) }

        // then
        assertEquals(
            5,
            geometryCollectionOverlay.backendOverlay.items.size
        )
        assertTrue(geometryCollectionOverlay.backendOverlay.items[0] is CirclePointOverlay)
        assertTrue(geometryCollectionOverlay.backendOverlay.items[1] is GeometryCollectionOverlay)
        assertEquals(
            2,
            (geometryCollectionOverlay.backendOverlay.items[1] as GeometryCollectionOverlay).backendOverlay.items.size
        )
        assertTrue(geometryCollectionOverlay.backendOverlay.items[2] is LineStringOverlay)
        assertTrue(geometryCollectionOverlay.backendOverlay.items[3] is GeometryCollectionOverlay)
        assertEquals(
            1,
            (geometryCollectionOverlay.backendOverlay.items[3] as GeometryCollectionOverlay).backendOverlay.items.size
        )
        assertTrue(geometryCollectionOverlay.backendOverlay.items[4] is PolygonOverlay)
    }
}
