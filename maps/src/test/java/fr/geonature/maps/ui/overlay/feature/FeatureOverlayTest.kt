package fr.geonature.maps.ui.overlay.feature

import fr.geonature.maps.FixtureHelper.getFixture
import fr.geonature.maps.jts.geojson.io.GeoJsonReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.StringReader

/**
 * Unit tests about [FeatureOverlay].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class FeatureOverlayTest {

    private lateinit var geoJsonReader: GeoJsonReader

    @Before
    fun setUp() {
        geoJsonReader = GeoJsonReader()
    }

    @Test
    fun testCreateOverlayFromFeaturePoint() {
        // given a JSON Feature as Point
        val reader = StringReader(getFixture("feature_point.json"))

        // when read the JSON as Feature
        val feature = geoJsonReader.readFeature(reader)

        // then
        assertNotNull(feature)

        // when create Overlay from Feature
        val featureOverlay = FeatureOverlay().apply { setFeature(feature) }

        // then
        assertEquals("id1",
                     featureOverlay.id)
        assertNotNull(featureOverlay.backendOverlay)
        assertEquals(feature.geometry,
                     (featureOverlay.backendOverlay as CirclePointOverlay).geometry)
    }

    @Test
    fun testCreateOverlayFromFeatureLineString() {
        // given a JSON Feature as Point
        val reader = StringReader(getFixture("feature_linestring.json"))

        // when read the JSON as Feature
        val feature = geoJsonReader.readFeature(reader)

        // then
        assertNotNull(feature)

        // when create Overlay from Feature
        val featureOverlay = FeatureOverlay().apply { setFeature(feature) }

        // then
        assertEquals("id1",
                     featureOverlay.id)
        assertNotNull(featureOverlay.backendOverlay)
        assertEquals(feature.geometry,
                     (featureOverlay.backendOverlay as LineStringOverlay).geometry)
    }

    @Test
    fun testCreateOverlayFromFeaturePolygon() {
        // given a JSON Feature as Point
        val reader = StringReader(getFixture("feature_polygon_simple.json"))

        // when read the JSON as Feature
        val feature = geoJsonReader.readFeature(reader)

        // then
        assertNotNull(feature)

        // when create Overlay from Feature
        val featureOverlay = FeatureOverlay().apply { setFeature(feature) }

        // then
        assertEquals("id1",
                     featureOverlay.id)
        assertNotNull(featureOverlay.backendOverlay)
        assertEquals(feature.geometry,
                     (featureOverlay.backendOverlay as PolygonOverlay).geometry)
    }

    @Test
    fun testCreateOverlayFromFeatureGeometryCollection() {
        // given a JSON Feature as GeometryCollection
        val reader = StringReader(getFixture("feature_geometrycollection.json"))

        // when read the JSON as Feature
        val feature = geoJsonReader.readFeature(reader)

        // then
        assertNotNull(feature)

        // when create Overlay from Feature
        val featureOverlay = FeatureOverlay().apply { setFeature(feature) }

        // then
        assertEquals("id1",
                     featureOverlay.id)
        assertNotNull(featureOverlay.backendOverlay)
        assertEquals(5,
                     (featureOverlay.backendOverlay as GeometryCollectionOverlay).backendOverlay.items.size)
        assertTrue((featureOverlay.backendOverlay as GeometryCollectionOverlay).backendOverlay.items[0] is CirclePointOverlay)
        assertTrue((featureOverlay.backendOverlay as GeometryCollectionOverlay).backendOverlay.items[1] is GeometryCollectionOverlay)
        assertEquals(2,
                     ((featureOverlay.backendOverlay as GeometryCollectionOverlay).backendOverlay.items[1] as GeometryCollectionOverlay).backendOverlay.items.size)
        assertTrue((featureOverlay.backendOverlay as GeometryCollectionOverlay).backendOverlay.items[2] is LineStringOverlay)
        assertTrue((featureOverlay.backendOverlay as GeometryCollectionOverlay).backendOverlay.items[3] is GeometryCollectionOverlay)
        assertEquals(1,
                     ((featureOverlay.backendOverlay as GeometryCollectionOverlay).backendOverlay.items[3] as GeometryCollectionOverlay).backendOverlay.items.size)
        assertTrue((featureOverlay.backendOverlay as GeometryCollectionOverlay).backendOverlay.items[4] is PolygonOverlay)
    }
}