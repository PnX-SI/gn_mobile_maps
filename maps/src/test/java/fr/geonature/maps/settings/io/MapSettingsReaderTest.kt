package fr.geonature.maps.settings.io

import fr.geonature.maps.FixtureHelper.getFixture
import fr.geonature.maps.settings.LayerPropertiesSettings
import fr.geonature.maps.settings.LayerSettings
import fr.geonature.maps.settings.LayerStyleSettings
import fr.geonature.maps.settings.MapSettings
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
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class MapSettingsReaderTest {

    private lateinit var mapSettingsReader: MapSettingsReader

    @Before
    fun setUp() {
        mapSettingsReader = MapSettingsReader()
    }

    @Test
    fun `should read map settings from JSON string`() {
        // given a JSON settings
        val json = getFixture("map_settings.json")

        // when read the JSON as MapSettings
        val mapSettings = mapSettingsReader.read(json)

        // then
        assertNotNull(mapSettings)
        assertEquals(
            MapSettings(
                arrayListOf(
                    LayerSettings(
                        "Nantes (Base)",
                        listOf("nantes.mbtiles")
                    ),
                    LayerSettings(
                        "Nantes (Data)",
                        listOf("nantes.wkt"),
                        LayerPropertiesSettings(
                            style = LayerStyleSettings.Builder.newInstance()
                                .stroke(true)
                                .color("#FF0000")
                                .weight(
                                    8
                                )
                                .opacity(0.9f)
                                .fill(true)
                                .fillColor("#FF8000")
                                .fillOpacity(0.2f)
                                .build()
                        )
                    )
                ),
                "/mnt/sdcard/osmdroid",
                showScale = false,
                showCompass = false,
                showZoom = true,
                rotationGesture = true,
                zoom = 8.0,
                minZoomLevel = 7.0,
                maxZoomLevel = 12.0,
                minZoomEditing = 10.0,
                maxBounds = BoundingBox.fromGeoPoints(
                    arrayListOf(
                        GeoPoint(
                            47.253369,
                            -1.605721
                        ),
                        GeoPoint(
                            47.173845,
                            -1.482811
                        )
                    )
                ),
                center = GeoPoint(
                    47.225827,
                    -1.554470
                )
            ),
            mapSettings
        )
    }

    @Test
    fun `should read map settings from JSON string with invalid properties`() {
        // given a JSON settings with some invalid layers settings
        val json = getFixture("map_settings_with_invalid_properties.json")

        // when read the JSON as MapSettings
        val mapSettings = mapSettingsReader.read(json)

        // then
        assertNotNull(mapSettings)
        assertEquals(
            MapSettings(
                arrayListOf(
                    LayerSettings(
                        "Nantes (Data)",
                        listOf("nantes.wkt"),
                        LayerPropertiesSettings(
                            style = LayerStyleSettings.Builder.newInstance()
                                .stroke(true)
                                .color("#FF0000")
                                .weight(
                                    8
                                )
                                .opacity(0.9f)
                                .fill(true)
                                .fillColor("#FF8000")
                                .fillOpacity(0.2f)
                                .build()
                        )
                    )
                ),
                null,
                showScale = false,
                showCompass = false,
                showZoom = true,
                rotationGesture = false,
                zoom = 8.0,
                minZoomLevel = 7.0,
                maxZoomLevel = 12.0,
                minZoomEditing = 10.0,
                maxBounds = BoundingBox.fromGeoPoints(
                    arrayListOf(
                        GeoPoint(
                            47.253369,
                            -1.605721
                        ),
                        GeoPoint(
                            47.173845,
                            -1.482811
                        )
                    )
                ),
                center = GeoPoint(
                    47.225827,
                    -1.554470
                )
            ),
            mapSettings
        )
    }

    @Test
    fun `should read map settings from JSON string with invalid layers`() {
        // given a JSON settings with some invalid layers settings
        val json = getFixture("map_settings_with_invalid_layers.json")

        // when read the JSON as MapSettings
        val mapSettings = mapSettingsReader.read(json)

        // then
        assertNotNull(mapSettings)
        assertEquals(
            MapSettings(
                arrayListOf(
                    LayerSettings(
                        "Nantes",
                        listOf("nantes.mbtiles")
                    ),
                    LayerSettings(
                        "nantes.wkt",
                        listOf("nantes.wkt"),
                        LayerPropertiesSettings(
                            style = LayerStyleSettings()
                        )
                    )
                ),
                null,
                showScale = false,
                showCompass = false,
                showZoom = false,
                rotationGesture = false,
                zoom = 8.0,
                minZoomLevel = 7.0,
                maxZoomLevel = 12.0,
                minZoomEditing = 10.0,
                maxBounds = BoundingBox.fromGeoPoints(
                    arrayListOf(
                        GeoPoint(
                            47.253369,
                            -1.605721
                        ),
                        GeoPoint(
                            47.173845,
                            -1.482811
                        )
                    )
                ),
                center = GeoPoint(
                    47.225827,
                    -1.554470
                )
            ),
            mapSettings
        )
    }

    @Test
    fun `should read map settings from invalid JSON string`() {
        // when read an invalid JSON as MapSettings
        val mapSettings = mapSettingsReader.read("")

        // then
        assertNull(mapSettings)
    }

    @Test
    fun `should override existing map settings from partial JSON string`() {
        // given an existing MapSettings
        val existingMapSettings = MapSettings(
            arrayListOf(
                LayerSettings(
                    "Nantes (Base)",
                    listOf("nantes.mbtiles")
                ),
                LayerSettings(
                    "Nantes (Data)",
                    listOf("nantes.wkt"),
                    LayerPropertiesSettings(
                        style = LayerStyleSettings.Builder.newInstance()
                            .stroke(true)
                            .color("#FF0000")
                            .weight(
                                8
                            )
                            .opacity(0.9f)
                            .fill(true)
                            .fillColor("#FF8000")
                            .fillOpacity(0.2f)
                            .build()
                    )
                )
            ),
            "/mnt/sdcard/osmdroid",
            showScale = false,
            showCompass = false,
            showZoom = true,
            rotationGesture = true,
            zoom = 8.0,
            minZoomLevel = 7.0,
            maxZoomLevel = 12.0,
            minZoomEditing = 10.0,
            maxBounds = BoundingBox.fromGeoPoints(
                arrayListOf(
                    GeoPoint(
                        47.253369,
                        -1.605721
                    ),
                    GeoPoint(
                        47.173845,
                        -1.482811
                    )
                )
            ),
            center = GeoPoint(
                47.225827,
                -1.554470
            )
        )

        // and a partial JSON settings
        val json = getFixture("map_settings_partial_layers.json")

        // when read the JSON as MapSettings
        val mapSettings = MapSettingsReader(existingMapSettings).read(json)

        // then
        assertNotNull(mapSettings)
        assertEquals(
            MapSettings(
                arrayListOf(
                    LayerSettings.Builder.newInstance()
                        .label("OSM")
                        .addSource("https://a.tile.openstreetmap.org")
                        .build()
                ),
                "/mnt/sdcard/osmdroid",
                showScale = false,
                showCompass = false,
                showZoom = true,
                rotationGesture = true,
                zoom = 8.0,
                minZoomLevel = 7.0,
                maxZoomLevel = 12.0,
                minZoomEditing = 10.0,
                maxBounds = BoundingBox.fromGeoPoints(
                    arrayListOf(
                        GeoPoint(
                            47.253369,
                            -1.605721
                        ),
                        GeoPoint(
                            47.173845,
                            -1.482811
                        )
                    )
                ),
                center = GeoPoint(
                    47.225827,
                    -1.554470
                )
            ),
            mapSettings
        )
    }
}
