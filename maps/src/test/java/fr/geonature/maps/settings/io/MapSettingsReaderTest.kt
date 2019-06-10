package fr.geonature.maps.settings.io

import fr.geonature.maps.FixtureHelper.getFixture
import fr.geonature.maps.settings.MapSettings
import fr.geonature.maps.settings.TileSourceSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [MapSettingsReader].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class MapSettingsReaderTest {

    lateinit var mapSettingsReader: MapSettingsReader

    @Before
    fun setUp() {
        mapSettingsReader = MapSettingsReader()
    }

    @Test
    fun testReadMapSettingsFromJsonString() {
        // given a JSON settings
        val json = getFixture("map_settings.json")

        // when read the JSON as MapSettings
        val mapSettings = mapSettingsReader.read(json)

        // then
        assertNotNull(mapSettings)
        assertEquals(
            MapSettings(
                arrayListOf(
                    TileSourceSettings(
                        "nantes.mbtiles",
                        "Nantes")),
                null,
                false,
                false,
                8.0,
                7.0,
                12.0,
                10.0,
                BoundingBox.fromGeoPoints(
                    arrayListOf(
                        GeoPoint(
                            47.253369,
                            -1.605721),
                        GeoPoint(
                            47.173845,
                            -1.482811))),
                GeoPoint(
                    47.225827,
                    -1.554470)),
            mapSettings)
    }

    @Test
    fun testReadMapSettingsFromJsonStringWithInvalidLayers() {
        // given a JSON settings with some invalid layers settings
        val json = getFixture("map_settings_with_invalid_layers.json")

        // when read the JSON as MapSettings
        val mapSettings = mapSettingsReader.read(json)

        // then
        assertNotNull(mapSettings)
        assertEquals(
            MapSettings(
                arrayListOf(
                    TileSourceSettings(
                        "nantes.mbtiles",
                        "Nantes",
                        7.0,
                        12.0,
                        512,
                        "jpg"),
                    TileSourceSettings(
                        "nantes.wkt",
                        "nantes.wkt")),
                null,
                false,
                false,
                8.0,
                7.0,
                12.0,
                10.0,
                BoundingBox.fromGeoPoints(
                    arrayListOf(
                        GeoPoint(
                            47.253369,
                            -1.605721),
                        GeoPoint(
                            47.173845,
                            -1.482811))),
                GeoPoint(
                    47.225827,
                    -1.554470)),
            mapSettings)
    }

    @Test
    fun testReadMapSettingsFromInvalidJsonString() {
        // when read an invalid JSON as MapSettings
        val mapSettings = mapSettingsReader.read("")

        // then
        assertNull(mapSettings)
    }
}