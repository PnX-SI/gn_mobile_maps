package fr.geonature.maps.ui.overlay.feature

import fr.geonature.maps.FixtureHelper.getFixture
import fr.geonature.maps.jts.geojson.io.GeoJsonReader
import org.junit.Assert
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.StringReader

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
        Assert.assertNotNull(featureCollection)

        // when create Overlay from FeatureCollection
        val featureCollectionOverlay = FeatureCollectionOverlay().apply { setFeatureCollection(featureCollection) }

        // then
        assertEquals(5,
                     featureCollectionOverlay.items.size)
        assertArrayEquals(arrayOf("id1",
                                  "id2",
                                  "id3",
                                  "id4",
                                  "id5"),
                          featureCollectionOverlay.items.asSequence().map {
                              when (it) {
                                  is FeatureOverlay -> it.id
                                  else -> null
                              }
                          }.filterNotNull().toList().sorted().toTypedArray())
    }

    @Test
    fun testCreateOverlayFromEmptyFeatureCollection() {
        // given a JSON empty FeatureCollection
        val reader = StringReader(getFixture("featurecollection_empty.json"))

        // when read the JSON as FeatureCollection
        val featureCollection = geoJsonReader.readFeatureCollection(reader)

        // then
        Assert.assertNotNull(featureCollection)

        // when create Overlay from FeatureCollection
        val featureCollectionOverlay = FeatureCollectionOverlay().apply { setFeatureCollection(featureCollection) }

        // then
        assertTrue(featureCollectionOverlay.items.isEmpty())
    }
}