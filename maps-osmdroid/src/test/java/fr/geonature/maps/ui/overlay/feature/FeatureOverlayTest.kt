package fr.geonature.maps.ui.overlay.feature

import fr.geonature.maps.FixtureHelper.getFixture
import fr.geonature.maps.jts.geojson.io.GeoJsonReader
import fr.geonature.maps.settings.LayerStyleSettings
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.locationtech.jts.geom.Geometry
import org.osmdroid.views.overlay.Overlay
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [FeatureOverlay].
 *
 * @author S. Grimault
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
        val json = getFixture("feature_point.json")

        // when read the JSON as Feature
        val feature = geoJsonReader.readFeature(json)

        // then
        assertNotNull(feature)

        feature!!

        // when create Overlay from Feature
        val featureOverlay = FeatureOverlay().apply { setFeature(feature) }

        // then
        assertEquals(
            "id1",
            featureOverlay.id
        )
        assertNotNull(featureOverlay.backendOverlay)
        assertEquals(
            feature.geometry,
            (featureOverlay.backendOverlay as CirclePointOverlay).geometry
        )
    }

    @Test
    fun testCreateOverlayFromFeatureLineString() {
        // given a JSON Feature as Point
        val json = getFixture("feature_linestring.json")

        // when read the JSON as Feature
        val feature = geoJsonReader.readFeature(json)

        // then
        assertNotNull(feature)

        feature!!

        // when create Overlay from Feature
        val featureOverlay = FeatureOverlay().apply { setFeature(feature) }

        // then
        assertEquals(
            "id1",
            featureOverlay.id
        )
        assertNotNull(featureOverlay.backendOverlay)
        assertEquals(
            feature.geometry,
            (featureOverlay.backendOverlay as LineStringOverlay).geometry
        )
    }

    @Test
    fun testCreateOverlayFromFeaturePolygon() {
        // given a JSON Feature as Point
        val json = getFixture("feature_polygon_simple.json")

        // when read the JSON as Feature
        val feature = geoJsonReader.readFeature(json)

        // then
        assertNotNull(feature)

        feature!!

        // when create Overlay from Feature
        val featureOverlay = FeatureOverlay().apply { setFeature(feature) }

        // then
        assertEquals(
            "id1",
            featureOverlay.id
        )
        assertNotNull(featureOverlay.backendOverlay)
        assertEquals(
            feature.geometry,
            (featureOverlay.backendOverlay as PolygonOverlay).geometry
        )
    }

    @Test
    fun testCreateOverlayFromFeatureGeometryCollection() {
        // given a JSON Feature as GeometryCollection
        val json = getFixture("feature_geometrycollection.json")

        // when read the JSON as Feature
        val feature = geoJsonReader.readFeature(json)

        // then
        assertNotNull(feature)

        // when create Overlay from Feature
        val featureOverlay = FeatureOverlay().apply { setFeature(feature!!) }

        // then
        assertEquals(
            "id1",
            featureOverlay.id
        )
        assertNotNull(featureOverlay.backendOverlay)
        assertEquals(
            5,
            (featureOverlay.backendOverlay as GeometryCollectionOverlay).backendOverlay.items.size
        )
        assertTrue((featureOverlay.backendOverlay as GeometryCollectionOverlay).backendOverlay.items[0] is CirclePointOverlay)
        assertTrue((featureOverlay.backendOverlay as GeometryCollectionOverlay).backendOverlay.items[1] is GeometryCollectionOverlay)
        assertEquals(
            2,
            ((featureOverlay.backendOverlay as GeometryCollectionOverlay).backendOverlay.items[1] as GeometryCollectionOverlay).backendOverlay.items.size
        )
        assertTrue((featureOverlay.backendOverlay as GeometryCollectionOverlay).backendOverlay.items[2] is LineStringOverlay)
        assertTrue((featureOverlay.backendOverlay as GeometryCollectionOverlay).backendOverlay.items[3] is GeometryCollectionOverlay)
        assertEquals(
            1,
            ((featureOverlay.backendOverlay as GeometryCollectionOverlay).backendOverlay.items[3] as GeometryCollectionOverlay).backendOverlay.items.size
        )
        assertTrue((featureOverlay.backendOverlay as GeometryCollectionOverlay).backendOverlay.items[4] is PolygonOverlay)
    }

    @Test
    fun testSetStyleIfOverlayWasSet() {
        val backendOverlay = mockkClass(AbstractGeometryOverlay::class)
        every { backendOverlay.setStyle(any()) } returns Unit

        // given FeatureOverlay with its Overlay
        @Suppress("UNCHECKED_CAST") val featureOverlay = FeatureOverlay().also {
            it.backendOverlay = backendOverlay as AbstractGeometryOverlay<Geometry, Overlay>
        }

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
        assertEquals(
            layerStyle,
            featureOverlay.layerStyle
        )
        verify { backendOverlay.setStyle(featureOverlay.layerStyle) }
    }

    @Test
    fun testDoNotSetStyleIfOverlayWasNotSet() {
        // given an empty FeatureOverlay
        val featureOverlay = spyk(FeatureOverlay())

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
        assertEquals(
            LayerStyleSettings(),
            featureOverlay.layerStyle
        )
        verify(inverse = true) { featureOverlay.backendOverlay?.setStyle(any()) }
    }
}
