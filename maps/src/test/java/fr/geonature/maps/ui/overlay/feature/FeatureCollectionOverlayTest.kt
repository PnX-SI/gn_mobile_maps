package fr.geonature.maps.ui.overlay.feature

import android.graphics.Color
import fr.geonature.maps.FixtureHelper.getFixture
import fr.geonature.maps.jts.geojson.io.GeoJsonReader
import fr.geonature.maps.settings.LayerStyleSettings
import fr.geonature.maps.ui.overlay.feature.filter.ContainsFeaturesFilter
import java.io.StringReader
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.util.GeoPoint
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [FeatureCollectionOverlay].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class FeatureCollectionOverlayTest {

    private lateinit var geoJsonReader: GeoJsonReader

    @Before
    fun setUp() {
        geoJsonReader = GeoJsonReader()
    }

    @Test
    fun testCreateOverlayFromFeatureCollection() {
        // given a JSON FeatureCollection
        val json = StringReader(getFixture("featurecollection.json"))

        // when read the JSON as FeatureCollection
        val featureCollection = geoJsonReader.readFeatureCollection(json)

        // then
        assertNotNull(featureCollection)

        // when create Overlay from FeatureCollection
        val featureCollectionOverlay =
            FeatureCollectionOverlay().apply { setFeatureCollection(featureCollection) }

        // then
        assertEquals(
            5,
            featureCollectionOverlay.getFeatureOverlays().size
        )
        assertArrayEquals(arrayOf(
            "id1",
            "id2",
            "id3",
            "id4",
            "id5"
        ),
            featureCollectionOverlay.getFeatureOverlays().map { it.id }.sorted().toTypedArray()
        )
    }

    @Test
    fun testGetFeatureOverlaysFromFeatureCollection() {
        // given a JSON FeatureCollection
        val json = StringReader(getFixture("featurecollection.json"))

        // when read the JSON as FeatureCollection
        val featureCollection = geoJsonReader.readFeatureCollection(json)

        // then
        assertNotNull(featureCollection)

        // when create Overlay from FeatureCollection
        val featureCollectionOverlay =
            FeatureCollectionOverlay().apply { setFeatureCollection(featureCollection) }

        // then
        assertEquals(
            5,
            featureCollectionOverlay.getFeatureOverlays().size
        )
        assertArrayEquals(arrayOf(
            "id1",
            "id3"
        ),
            featureCollectionOverlay.getFeatureOverlays {
                it.id in arrayOf(
                    "id1",
                    "id3"
                )
            }.map { it.id }.sorted().toTypedArray()
        )
    }

    @Test
    fun testCreateOverlayFromEmptyFeatureCollection() {
        // given a JSON empty FeatureCollection
        val reader = StringReader(getFixture("featurecollection_empty.json"))

        // when read the JSON as FeatureCollection
        val featureCollection = geoJsonReader.readFeatureCollection(reader)

        // then
        assertNotNull(featureCollection)

        // when create Overlay from FeatureCollection
        val featureCollectionOverlay =
            FeatureCollectionOverlay().apply { setFeatureCollection(featureCollection) }

        // then
        assertTrue(featureCollectionOverlay.items.isEmpty())
    }

    @Test
    fun testApplyContainsFeatureFilter() {
        // given a JSON FeatureCollection
        val reader = StringReader(getFixture("featurecollection_polygons.json"))

        // when read the JSON as FeatureCollection
        val featureCollection = geoJsonReader.readFeatureCollection(reader)

        // then
        assertNotNull(featureCollection)

        // when create Overlay from FeatureCollection
        val featureCollectionOverlay =
            FeatureCollectionOverlay().apply { setFeatureCollection(featureCollection) }
        // and apply filter on this overlay inside a polygon with holes
        var containsFeaturesFilter = ContainsFeaturesFilter(
            GeoPoint(
                47.226171477212425,
                -1.554340124130249
            ),
            LayerStyleSettings(),
            LayerStyleSettings.Builder.newInstance().color(Color.RED).build()
        )
        featureCollectionOverlay.apply(containsFeaturesFilter)

        // then
        assertEquals(
            1,
            containsFeaturesFilter.getSelectedFeatures().size
        )

        var matchedOverlays = featureCollectionOverlay.getFeatureOverlays {
            containsFeaturesFilter.getSelectedFeatures()
                .any { feature -> feature.id == it.id }
        }

        assertEquals(
            1,
            matchedOverlays.size
        )
        matchedOverlays.forEach {
            assertEquals(
                Color.RED,
                (it.backendOverlay as PolygonOverlay).backendOverlay.strokeColor
            )
        }

        // when apply filter on this overlay to match 2 polygons
        containsFeaturesFilter = ContainsFeaturesFilter(
            GeoPoint(
                47.226171477212425,
                -1.5544822812080383
            ),
            LayerStyleSettings(),
            LayerStyleSettings.Builder.newInstance().color(Color.RED).build()
        )
        featureCollectionOverlay.apply(containsFeaturesFilter)

        // then
        assertEquals(
            2,
            containsFeaturesFilter.getSelectedFeatures().size
        )

        matchedOverlays = featureCollectionOverlay.getFeatureOverlays {
            containsFeaturesFilter.getSelectedFeatures()
                .any { feature -> feature.id == it.id }
        }

        assertEquals(
            2,
            matchedOverlays.size
        )
        matchedOverlays.forEach {
            assertEquals(
                Color.RED,
                (it.backendOverlay as PolygonOverlay).backendOverlay.strokeColor
            )
        }
    }
}
