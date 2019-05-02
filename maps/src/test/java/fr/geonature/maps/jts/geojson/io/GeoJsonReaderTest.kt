package fr.geonature.maps.jts.geojson.io

import fr.geonature.maps.FixtureHelper.getFixture
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.locationtech.jts.geom.GeometryFactory
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.StringReader

/**
 * Unit tests about [GeoJsonReader].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class GeoJsonReaderTest {

    private lateinit var gf: GeometryFactory
    private lateinit var geoJsonReader: GeoJsonReader

    @Before
    fun setUp() {
        gf = GeometryFactory()
        geoJsonReader = GeoJsonReader()
    }

    @Test
    fun testReadFeatureFromInvalidJsonString() {
        // when read an invalid JSON as Feature
        val feature = geoJsonReader.readFeature("")

        // then
        assertNull(feature)
    }

    @Test
    fun testReadFeatureAsPoint() {
        // given a JSON Feature as Point
        val reader = StringReader(getFixture("feature_point.json"))

        // when read the JSON as Feature
        val feature = geoJsonReader.readFeature(reader)

        // then
        assertNotNull(feature)
        assertEquals(
            "id1",
            feature.id
        )
        assertEquals(
            "Feature",
            feature.type
        )
        assertEquals(
            5,
            feature.properties.size()
        )
        assertEquals(
            "Ile de Versailles",
            feature.properties["name"]
        )
        assertEquals(
            1831,
            feature.properties["year"]
        )
        assertEquals(
            3.14,
            feature.properties["double_attribute"]
        )
        assertEquals(
            false,
            feature.properties["boolean_attribute_false"]
        )
        assertEquals(
            true,
            feature.properties["boolean_attribute_true"]
        )
        assertNotNull(feature.geometry)
        assertEquals(
            createPoint(
                gf,
                47.2256258,
                -1.5545135
            ),
            feature.geometry
        )
    }

    @Test
    fun testReadFeatureAsMultiPoint() {
        // given a JSON Feature as MultiPoint
        val reader = StringReader(getFixture("feature_multipoint.json"))

        // when read the JSON as Feature
        val feature = geoJsonReader.readFeature(reader)

        // then
        assertNotNull(feature)
        assertEquals(
            "id1",
            feature.id
        )
        assertEquals(
            "Feature",
            feature.type
        )
        assertNotNull(feature.geometry)
        assertEquals(
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
            feature.geometry
        )
    }

    @Test
    fun testReadFeatureAsLineString() {
        // given a JSON Feature as LineString
        val reader = StringReader(getFixture("feature_linestring.json"))

        // when read the JSON as Feature
        val feature = geoJsonReader.readFeature(reader)

        // then
        assertNotNull(feature)
        assertEquals(
            "id1",
            feature.id
        )
        assertEquals(
            "Feature",
            feature.type
        )
        assertNotNull(feature.geometry)
        assertEquals(
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
            feature.geometry
        )
    }

    @Test
    fun testReadFeatureAsMultiLineString() {
        // given a JSON Feature as MultiLineString
        val reader = StringReader(getFixture("feature_multilinestring.json"))

        // when read the JSON as Feature
        val feature = geoJsonReader.readFeature(reader)

        // then
        assertNotNull(feature)
        assertEquals(
            "id1",
            feature.id
        )
        assertEquals(
            "Feature",
            feature.type
        )
        assertNotNull(feature.geometry)
        assertEquals(
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
            feature.geometry
        )
    }

    @Test
    fun testReadFeatureAsSimplePolygon() {
        // given a JSON Feature as simple Polygon
        val reader = StringReader(getFixture("feature_polygon_simple.json"))

        // when read the JSON as Feature
        val feature = geoJsonReader.readFeature(reader)

        // then
        assertNotNull(feature)
        assertEquals(
            "id1",
            feature.id
        )
        assertEquals(
            "Feature",
            feature.type
        )
        assertNotNull(feature.geometry)
        assertEquals(
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
            feature.geometry
        )
    }

    @Test
    fun testReadFeatureAsPolygonWithHoles() {
        // given a JSON Feature as Polygon with holes
        val reader = StringReader(getFixture("feature_polygon_holes.json"))

        // when read the JSON as Feature
        val feature = geoJsonReader.readFeature(reader)

        // then
        assertNotNull(feature)
        assertEquals(
            "id1",
            feature.id
        )
        assertEquals(
            "Feature",
            feature.type
        )
        assertNotNull(feature.geometry)
        assertEquals(
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
            feature.geometry
        )
    }

    @Test
    fun testReadFeatureAsMultiPolygon() {
        // given a JSON Feature as MultiPolygon
        val reader = StringReader(getFixture("feature_multipolygon.json"))

        // when read the JSON as Feature
        val feature = geoJsonReader.readFeature(reader)

        // then
        assertNotNull(feature)
        assertEquals(
            "id1",
            feature.id
        )
        assertEquals(
            "Feature",
            feature.type
        )
        assertNotNull(feature.geometry)
        assertEquals(
            createMultiPolygon(
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
            ),
            feature.geometry
        )
    }

    @Test
    fun testReadFeatureAsGeometryCollection() {
        // given a JSON Feature as GeometryCollection
        val reader = StringReader(getFixture("feature_geometrycollection.json"))

        // when read the JSON as Feature
        val feature = geoJsonReader.readFeature(reader)

        // then
        assertNotNull(feature)
        assertEquals(
            "id1",
            feature.id
        )
        assertEquals(
            "Feature",
            feature.type
        )
        assertNotNull(feature.geometry)
        assertEquals(
            createGeometryCollection(
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
            ),
            feature.geometry
        )
    }

    @Test
    fun testReadFeatureCollectionFromJsonString() {
        // given a JSON FeatureCollection
        val json = getFixture("featurecollection.json")

        // when read the JSON as FeatureCollection
        val featureCollection = geoJsonReader.readFeatureCollection(json)

        // then
        assertNotNull(featureCollection)

        if (featureCollection != null) {
            assertEquals(
                "FeatureCollection",
                featureCollection.type
            )

            assertTrue(featureCollection.hasFeature("id1"))
            assertTrue(featureCollection.hasFeature("id2"))
            assertTrue(featureCollection.hasFeature("id3"))
            assertTrue(featureCollection.hasFeature("id4"))
            assertTrue(featureCollection.hasFeature("id5"))
        }
    }

    @Test
    fun testReadFeatureCollectionFromInvalidJsonString() {
        // when read an invalid JSON as FeatureCollection
        val featureCollection = geoJsonReader.readFeatureCollection("")

        // then
        assertNull(featureCollection)
    }

    @Test
    fun testReadEmptyFeatureCollection() {
        // given a JSON empty FeatureCollection
        val reader = StringReader(getFixture("featurecollection_empty.json"))

        // when read the JSON as FeatureCollection
        val featureCollection = geoJsonReader.readFeatureCollection(reader)

        // then
        assertNotNull(featureCollection)
        assertEquals(
            "FeatureCollection",
            featureCollection.type
        )
        assertTrue(featureCollection.isEmpty())
    }

    @Test
    fun testReadFeatureCollection() {
        // given a JSON FeatureCollection
        val reader = StringReader(getFixture("featurecollection.json"))

        // when read the JSON as FeatureCollection
        val featureCollection = geoJsonReader.readFeatureCollection(reader)

        // then
        assertNotNull(featureCollection)
        assertEquals(
            "FeatureCollection",
            featureCollection.type
        )
        assertFalse(featureCollection.isEmpty())
        assertTrue(featureCollection.hasFeature("id1"))
        assertTrue(featureCollection.hasFeature("id2"))
        assertTrue(featureCollection.hasFeature("id3"))
        assertTrue(featureCollection.hasFeature("id4"))
        assertTrue(featureCollection.hasFeature("id5"))
        assertFalse(featureCollection.hasFeature("no_such_feature"))

        val feature1 = featureCollection.getFeature("id1")
        assertNotNull(feature1)

        if (feature1 != null) {
            assertEquals(
                "id1",
                feature1.id
            )
            assertEquals(
                "Feature",
                feature1.type
            )
            assertNotNull(feature1.geometry)
            assertEquals(
                createPoint(
                    gf,
                    47.2256258,
                    -1.5545135
                ),
                feature1.geometry
            )
        }

        val feature2 = featureCollection.getFeature("id2")
        assertNotNull(feature2)

        if (feature2 != null) {
            assertEquals(
                "id2",
                feature2.id
            )
            assertEquals(
                "Feature",
                feature2.type
            )
            assertNotNull(feature2.geometry)
            assertEquals(
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
                feature2.geometry
            )
        }

        val feature3 = featureCollection.getFeature("id3")
        assertNotNull(feature3)

        if (feature3 != null) {
            assertEquals(
                "id3",
                feature3.id
            )
            assertEquals(
                "Feature",
                feature3.type
            )
            assertNotNull(feature3.geometry)
            assertEquals(
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
                feature3.geometry
            )
        }

        val feature4 = featureCollection.getFeature("id4")
        assertNotNull(feature4)

        if (feature4 != null) {
            assertEquals(
                "id4",
                feature4.id
            )
            assertEquals(
                "Feature",
                feature4.type
            )
            assertNotNull(feature4.geometry)
            assertEquals(
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
                feature4.geometry
            )
        }

        val feature5 = featureCollection.getFeature("id5")
        assertNotNull(feature5)

        if (feature5 != null) {
            assertEquals(
                "id5",
                feature5.id
            )
            assertEquals(
                "Feature",
                feature5.type
            )
            assertNotNull(feature5.geometry)
            assertEquals(
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
                feature5.geometry
            )
        }
    }

    @Test
    fun testReadGeometryFromInvalidJsonString() {
        // when read an invalid JSON as FeatureCollection
        val geometry = geoJsonReader.readGeometry("")

        // then
        assertNull(geometry)
    }

    @Test
    fun testReadGeometry() {
        // given a JSON Geometry as Point
        val reader = StringReader(getFixture("geometry_point.json"))

        // when read the JSON as Point
        val point = geoJsonReader.readGeometry(reader)

        // then
        assertNotNull(point)
        assertEquals(
            createPoint(
                gf,
                47.2256258,
                -1.5545135
            ),
            point
        )
    }
}