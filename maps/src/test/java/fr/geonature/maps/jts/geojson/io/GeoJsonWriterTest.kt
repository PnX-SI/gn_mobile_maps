package fr.geonature.maps.jts.geojson.io

import fr.geonature.maps.jts.geojson.Feature
import fr.geonature.maps.jts.geojson.FeatureCollection
import fr.geonature.maps.jts.geojson.JTSTestHelper.createCoordinate
import fr.geonature.maps.jts.geojson.JTSTestHelper.createGeometryCollection
import fr.geonature.maps.jts.geojson.JTSTestHelper.createLineString
import fr.geonature.maps.jts.geojson.JTSTestHelper.createLinearRing
import fr.geonature.maps.jts.geojson.JTSTestHelper.createMultiLineString
import fr.geonature.maps.jts.geojson.JTSTestHelper.createMultiPoint
import fr.geonature.maps.jts.geojson.JTSTestHelper.createMultiPolygon
import fr.geonature.maps.jts.geojson.JTSTestHelper.createPoint
import fr.geonature.maps.jts.geojson.JTSTestHelper.createPolygon
import io.mockk.every
import io.mockk.mockkClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [GeoJsonWriter].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class GeoJsonWriterTest {

    private lateinit var gf: GeometryFactory
    private lateinit var geoJsonWriter: GeoJsonWriter
    private lateinit var geoJsonReader: GeoJsonReader

    @Before
    fun setUp() {
        gf = GeometryFactory()
        geoJsonWriter = GeoJsonWriter()
        geoJsonReader = GeoJsonReader()
    }

    @Test
    fun testWriteInvalidFeature() {
        // given an invalid Feature (e.g. with no geometry defined)
        val feature = mockkClass(Feature::class)
        val geometry = mockkClass(Geometry::class)

        every { feature.id } returns "id1"
        every { feature.type } returns Geometry.TYPENAME_POINT
        every { feature.geometry } returns geometry
        every { geometry.geometryType } returns ""

        // when write this Feature as JSON string
        val json = geoJsonWriter.write(feature)

        // then
        assertNull(json)
    }

    @Test
    fun testWriteFeatureAsPoint() {
        // given a Feature as Point
        val feature = Feature(
            "id1",
            createPoint(
                gf,
                47.2256258,
                -1.5545135
            ),
            hashMapOf(
                "name" to "Ile de Versailles",
                "year" to 1831,
                "double_attribute" to 3.14,
                "boolean_attribute_false" to false,
                "boolean_attribute_true" to true
            )
        )

        // when write this Feature as JSON string
        val json = geoJsonWriter.write(feature)

        // then
        assertNotNull(json)
        assertEquals(
            geoJsonReader.readFeature(json),
            feature
        )
    }

    @Test
    fun testWriteFeatureAsMultiPoint() {
        // given a Feature as MultiPoint
        val feature = Feature(
            "id",
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
            )
        )

        // when write this Feature as JSON string
        val json = geoJsonWriter.write(feature)

        // then
        assertNotNull(json)
        assertEquals(
            geoJsonReader.readFeature(json),
            feature
        )
    }

    @Test
    fun testWriteFeatureAsLineString() {
        // given a Feature as LineString
        val feature = Feature(
            "id",
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

        // when write this Feature as JSON string
        val json = geoJsonWriter.write(feature)

        // then
        assertNotNull(json)
        assertEquals(
            geoJsonReader.readFeature(json),
            feature
        )
    }

    @Test
    fun testWriteFeatureAsMultiLineString() {
        // given a Feature as MultiLineString
        val feature = Feature(
            "id1",
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
            )
        )

        // when write this Feature as JSON string
        val json = geoJsonWriter.write(feature)

        // then
        assertNotNull(json)
        assertEquals(
            geoJsonReader.readFeature(json),
            feature
        )
    }

    @Test
    fun testWriteFeatureAsSimplePolygon() {
        // given a Feature as simple Polygon
        val feature = Feature(
            "id1",
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

        // when write this Feature as JSON string
        val json = geoJsonWriter.write(feature)

        // then
        assertNotNull(json)
        assertEquals(
            geoJsonReader.readFeature(json),
            feature
        )
    }

    @Test
    fun testWriteFeatureAsPolygonWithHoles() {
        // given a Feature as Polygon with holes
        val feature = Feature(
            "id1",
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

        // when write this Feature as JSON string
        val json = geoJsonWriter.write(feature)

        // then
        assertNotNull(json)
        assertEquals(
            geoJsonReader.readFeature(json),
            feature
        )
    }

    @Test
    fun testWriteFeatureAsMultiPolygon() {
        // given a Feature as MultiPolygon
        val feature = Feature(
            "id",
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
            )
        )

        // when write this Feature as JSON string
        val json = geoJsonWriter.write(feature)

        // then
        assertNotNull(json)
        assertEquals(
            geoJsonReader.readFeature(json),
            feature
        )
    }

    @Test
    fun testWriteFeatureAsGeometryCollection() {
        // given a Feature as GeometryCollection
        val feature = Feature(
            "id",
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
            )
        )

        // when write this Feature as JSON string
        val json = geoJsonWriter.write(feature)

        // then
        assertNotNull(json)
        assertEquals(
            geoJsonReader.readFeature(json),
            feature
        )
    }

    @Test
    fun testWriteFeatureCollection() {
        // given Feature1 as Point
        val feature1 = Feature(
            "id1",
            createPoint(
                gf,
                47.2256258,
                -1.5545135
            )
        )

        // given Feature2 as MultiPoint
        val feature2 = Feature(
            "id2",
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
            )
        )

        // given Feature3 as LineString
        val feature3 = Feature(
            "id3",
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

        // given Feature4 as MultiLineString
        val feature4 = Feature(
            "id4",
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
            )
        )

        // given Feature5 as simple Polygon
        val feature5 = Feature(
            "id5",
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

        // given a FeatureCollection
        val featureCollection = FeatureCollection().apply {
            addAllFeatures(
                listOf(
                    feature1,
                    feature2,
                    feature3,
                    feature4,
                    feature5
                )
            )
        }

        // when write this FeatureCollection as JSON string
        val json = geoJsonWriter.write(featureCollection)

        // then
        assertNotNull(json)
        assertEquals(
            geoJsonReader.readFeatureCollection(json),
            featureCollection
        )
    }

    @Test
    fun testWriteEmptyFeatureCollection() {
        // given an empty FeatureCollection
        val featureCollection = FeatureCollection()

        // when write this FeatureCollection as JSON string
        val json = geoJsonWriter.write(featureCollection)

        // then
        assertNotNull(json)
        assertEquals(
            geoJsonReader.readFeatureCollection(json),
            featureCollection
        )
    }

    @Test
    fun testWriteInvalidFeatureCollection() {
        // given an invalid Feature (e.g. with no geometry defined)
        val feature = mockkClass(Feature::class)
        val geometry = mockkClass(Geometry::class)

        every { feature.id } returns "id1"
        every { feature.type } returns Geometry.TYPENAME_POINT
        every { feature.geometry } returns geometry
        every { geometry.geometryType } returns ""

        // and a FeatureCollection
        val featureCollection = FeatureCollection().apply {
            addFeature(feature)
        }

        // when write this FeatureCollection as JSON string
        val json = geoJsonWriter.write(featureCollection)

        // then
        assertNull(json)
    }
}
