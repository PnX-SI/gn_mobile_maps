package fr.geonature.maps.ui.overlay.feature

import fr.geonature.maps.FixtureHelper.getFixture
import fr.geonature.maps.MockitoKotlinHelper.any
import fr.geonature.maps.jts.geojson.io.GeoJsonReader
import fr.geonature.maps.settings.LayerStyleSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.locationtech.jts.geom.Geometry
import org.mockito.Mockito.atMostOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.osmdroid.views.overlay.Overlay
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

    @Test
    fun testSetStyleIfOverlayWasSet() {
        val backendOverlay = mock(AbstractGeometryOverlay::class.java)

        // given FeatureOverlay with its Overlay
        @Suppress("UNCHECKED_CAST")
        val featureOverlay = FeatureOverlay().also { it.backendOverlay = backendOverlay as AbstractGeometryOverlay<Geometry, Overlay> }

        // when set new style
        val layerStyle = LayerStyleSettings.Builder.newInstance()
                .stroke(true)
                .color("#FF0000")
                .weight(10)
                .opacity(0.9f)
                .fill(true)
                .fillColor("#0000FF")
                .fillOpacity(0.25f)
                .build()
        featureOverlay.setStyle(layerStyle)

        // then
        assertEquals(layerStyle,
                     featureOverlay.layerStyle)
        verify(backendOverlay,
               atMostOnce()).setStyle(featureOverlay.layerStyle)
    }

    @Test
    fun testDoNotSetStyleIfOverlayWasNotSet() {
        // given an empty FeatureOverlay
        val featureOverlay = spy(FeatureOverlay())

        // when set new style
        val layerStyle = LayerStyleSettings.Builder.newInstance()
                .stroke(true)
                .color("#FF0000")
                .weight(10)
                .opacity(0.9f)
                .fill(true)
                .fillColor("#0000FF")
                .fillOpacity(0.25f)
                .build()
        featureOverlay.setStyle(layerStyle)

        // then
        assertEquals(LayerStyleSettings(),
                     featureOverlay.layerStyle)
        verify(featureOverlay,
               never()).backendOverlay?.setStyle(any(LayerStyleSettings::class.java))
    }
}