package fr.geonature.maps.settings

import android.net.Uri
import android.os.Parcel
import kotlinx.parcelize.parcelableCreator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [LayerSettings].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class LayerSettingsTest {

    @Test
    fun `should instantiate layer settings from builder`() {
        // given a layer settings instance from its builder
        val layerSettings = LayerSettings.Builder.newInstance()
            .label("Nantes")
            .addSource("nantes.mbtiles")
            .build()

        // then
        assertEquals(
            LayerSettings(
                "Nantes",
                listOf("nantes.mbtiles")
            ),
            layerSettings
        )
    }

    @Test
    fun `should instantiate layer settings from builder with default properties`() {
        assertEquals(
            LayerSettings(
                "Nantes",
                listOf("nantes.unknown")
            ),
            LayerSettings.Builder.newInstance()
                .label("Nantes")
                .addSource("nantes.unknown")
                .build()
        )

        assertEquals(
            LayerSettings(
                "OSM",
                listOf("https://a.tile.openstreetmap.org"),
                LayerPropertiesSettings(
                    minZoomLevel = 0,
                    maxZoomLevel = 19,
                    tileSizePixels = 256,
                    tileMimeType = "image/png",
                    attribution = null,
                    style = null
                )
            ),
            LayerSettings.Builder.newInstance()
                .label("OSM")
                .addSource("https://a.tile.openstreetmap.org")
                .build()
        )

        assertEquals(
            LayerSettings(
                "Nantes",
                listOf("nantes.wkt"),
                LayerPropertiesSettings(
                    style = LayerStyleSettings()
                )
            ),
            LayerSettings.Builder.newInstance()
                .label("Nantes")
                .addSource("nantes.wkt")
                .build()
        )

        assertEquals(
            LayerSettings(
                "Nantes",
                listOf("nantes.geojson"),
                LayerPropertiesSettings(
                    attribution = "Some attribution",
                    style = LayerStyleSettings()
                )
            ),
            LayerSettings.Builder.newInstance()
                .label("Nantes")
                .properties(
                    LayerPropertiesSettings.Builder.newInstance()
                        .attribution("Some attribution")
                        .build()
                )
                .addSource("nantes.geojson")
                .build()
        )
    }

    @Test
    fun `should instantiate a new layer settings from existing instance`() {
        // given a layer settings instance
        val layerSettings = LayerSettings(
            "OSM",
            listOf("https://a.tile.openstreetmap.org"),
            LayerPropertiesSettings(
                minZoomLevel = 0,
                maxZoomLevel = 19,
                tileSizePixels = 256,
                tileMimeType = "image/png",
                attribution = null,
                style = null
            )
        )

        // then
        assertEquals(
            layerSettings,
            LayerSettings.Builder.newInstance()
                .from(layerSettings)
                .build()
        )

        // when applying new properties
        val fromExistingLayerSettings = LayerSettings.Builder.newInstance()
            .from(layerSettings)
            .addSource("nantes.wkt")
            .build()

        // then
        assertEquals(
            LayerSettings(
                "OSM",
                listOf("https://a.tile.openstreetmap.org"),
                LayerPropertiesSettings(
                    minZoomLevel = 0,
                    maxZoomLevel = 19,
                    tileSizePixels = 256,
                    tileMimeType = "image/png",
                    attribution = null,
                    style = null
                )
            ),
            fromExistingLayerSettings
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should throw IllegalArgumentException if trying to build layer settings with undefined label`() {
        LayerSettings.Builder.newInstance()
            .addSource("nantes.mbtiles")
            .build()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should throw IllegalArgumentException if trying to build layer settings with undefined source`() {
        LayerSettings.Builder.newInstance()
            .label("Nantes")
            .build()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should throw IllegalArgumentException if trying to build layer settings with invalid source`() {
        LayerSettings.Builder.newInstance()
            .label("Nantes")
            .addSource("")
            .build()
    }

    @Test
    fun `should get the layer type`() {
        assertEquals(
            LayerType.TILES,
            LayerSettings(
                "OSM",
                listOf("https://a.tile.openstreetmap.org")
            ).getType()
        )

        assertEquals(
            LayerType.TILES,
            LayerSettings(
                "Nantes",
                listOf("nantes.mbtiles")
            ).getType()
        )

        assertEquals(
            LayerType.VECTOR,
            LayerSettings(
                "Nantes",
                listOf("nantes.wkt")
            ).getType()
        )

        assertEquals(
            LayerType.VECTOR,
            LayerSettings(
                "Nantes",
                listOf("nantes.json")
            ).getType()
        )

        assertEquals(
            LayerType.VECTOR,
            LayerSettings(
                "Nantes",
                listOf("nantes.geojson")
            ).getType()
        )

        assertEquals(
            LayerType.NOT_IMPLEMENTED,
            LayerSettings(
                "Nantes",
                listOf("nantes.unknown")
            ).getType()
        )
    }

    @Test
    fun `should activate or not layer`() {
        assertTrue(
            LayerSettings.Builder.newInstance()
                .label("Nantes")
                .addSource("nantes.mbtiles")
                .build().properties.active
        )

        assertTrue(
            LayerSettings.Builder.newInstance()
                .label("Nantes")
                .addSource("nantes.mbtiles")
                .properties(
                    LayerPropertiesSettings.Builder.newInstance()
                        .active()
                        .build()
                )
                .build().properties.active
        )

        assertFalse(
            LayerSettings.Builder.newInstance()
                .label("Nantes")
                .addSource("nantes.mbtiles")
                .properties(
                    LayerPropertiesSettings.Builder.newInstance()
                        .active(false)
                        .build()
                )
                .build().properties.active
        )

        assertNotEquals(
            LayerSettings.Builder.newInstance()
                .label("Nantes")
                .addSource("nantes.mbtiles")
                .properties(
                    LayerPropertiesSettings.Builder.newInstance()
                        .active()
                        .build()
                )
                .build(),
            LayerSettings.Builder.newInstance()
                .label("Nantes")
                .addSource("nantes.mbtiles")
                .properties(
                    LayerPropertiesSettings.Builder.newInstance()
                        .active(false)
                        .build()
                )
                .build()
        )
    }

    @Test
    fun `should get corresponding URIs from layer sources`() {
        // from source with only relative path
        assertTrue(
            LayerSettings(
                "Nantes",
                listOf("nantes.mbtiles")
            ).getSourcesAsUri()
                .isEmpty()
        )

        // from valid local source
        assertEquals(
            listOf(Uri.parse("file:///some/path/to/nantes.mbtiles")),
            LayerSettings(
                "Nantes",
                listOf("file:///some/path/to/nantes.mbtiles")
            ).getSourcesAsUri()
        )

        // from only valid local sources
        assertEquals(
            listOf(
                Uri.parse("file:///some/path/to/nantes_areas.wkt"),
                Uri.parse("file:///some/path/to/nantes_pois.json")
            ),
            LayerSettings(
                "Nantes",
                listOf(
                    "file:///some/path/to/nantes_areas.wkt",
                    "to/nantes_ways.wkt",
                    "file:///some/path/to/nantes_pois.json",
                    "https://a.tile.openstreetmap.org"
                )
            ).getSourcesAsUri()
        )

        // from only valid online sources
        assertEquals(
            listOf(
                Uri.parse("https://a.tile.openstreetmap.org"),
                Uri.parse("https://c.tile.openstreetmap.org")
            ),
            LayerSettings(
                "OSM",
                listOf(
                    "https://a.tile.openstreetmap.org",
                    "b.tile.openstreetmap.org",
                    "https://c.tile.openstreetmap.org",
                    "file:///some/path/to/nantes_pois.json"
                )
            ).getSourcesAsUri()
        )
    }

    @Test
    fun `should obtain layer settings instance from parcelable`() {
        // given a layer settings
        val layerSettings = LayerSettings(
            "Nantes",
            listOf("nantes.mbtiles")
        )

        // when we obtain a Parcel object to write the LayerPropertiesSettings instance to it
        val parcel = Parcel.obtain()
        layerSettings.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            layerSettings,
            parcelableCreator<LayerSettings>().createFromParcel(parcel)
        )
    }

    @Test
    fun `should compare two layers`() {
        assertTrue(
            LayerSettings(
                "OSM",
                listOf("https://a.tile.openstreetmap.org")
            ) < LayerSettings(
                "Nantes",
                listOf("nantes.mbtiles")
            )
        )
        assertTrue(
            LayerSettings(
                "OSM",
                listOf("https://a.tile.openstreetmap.org")
            ) == LayerSettings(
                "OSM",
                listOf("https://a.tile.openstreetmap.org")
            )
        )
        assertTrue(
            LayerSettings(
                "OSM",
                listOf("https://a.tile.openstreetmap.org")
            ) < LayerSettings(
                "OSM #1",
                listOf("https://a.tile.openstreetmap.org")
            )
        )
        assertTrue(
            LayerSettings(
                "OSM",
                listOf("https://a.tile.openstreetmap.org")
            ) < LayerSettings(
                "OSM",
                listOf("https://b.tile.openstreetmap.org")
            )
        )
        assertTrue(
            LayerSettings(
                "Nantes",
                listOf("nantes.mbtiles")
            ) < LayerSettings(
                "Nantes #1",
                listOf("nantes2.mbtiles")
            )
        )
        assertTrue(
            LayerSettings(
                "Nantes",
                listOf("nantes.mbtiles")
            ) < LayerSettings(
                "Nantes",
                listOf("nantes2.mbtiles")
            )
        )
        assertTrue(
            LayerSettings(
                "Nantes",
                listOf("nantes.mbtiles")
            ) == LayerSettings(
                "Nantes",
                listOf("nantes.mbtiles")
            )
        )
        assertTrue(
            LayerSettings(
                "Nantes",
                listOf("nantes.mbtiles"),
                LayerPropertiesSettings(active = false)
            ) < LayerSettings(
                "Nantes",
                listOf("nantes.mbtiles"),
                LayerPropertiesSettings(active = true)
            )
        )
        assertTrue(
            LayerSettings(
                "Nantes",
                listOf("nantes.mbtiles")
            ) < LayerSettings(
                "Nantes",
                listOf("nantes.unknown")
            )
        )

        assertEquals(
            listOf(
                LayerSettings.Builder.newInstance()
                    .label("Nantes")
                    .addSource("nantes.mbtiles")
                    .properties(
                        LayerPropertiesSettings.Builder.newInstance()
                            .active(false)
                            .build()
                    )
                    .build(),
                LayerSettings.Builder.newInstance()
                    .label("OSM")
                    .addSource("https://a.tile.openstreetmap.org")
                    .build(),
                LayerSettings.Builder.newInstance()
                    .label("Nantes (WKT)")
                    .addSource("nantes.wkt")
                    .build(),
                LayerSettings.Builder.newInstance()
                    .label("OTM")
                    .addSource("https://a.tile.opentopomap.org")
                    .build()
            ).sorted(),
            listOf(
                LayerSettings.Builder.newInstance()
                    .label("OSM")
                    .addSource("https://a.tile.openstreetmap.org")
                    .build(),
                LayerSettings.Builder.newInstance()
                    .label("OTM")
                    .addSource("https://a.tile.opentopomap.org")
                    .build(),
                LayerSettings.Builder.newInstance()
                    .label("Nantes")
                    .addSource("nantes.mbtiles")
                    .properties(
                        LayerPropertiesSettings.Builder.newInstance()
                            .active(false)
                            .build()
                    )
                    .build(),
                LayerSettings.Builder.newInstance()
                    .label("Nantes (WKT)")
                    .addSource("nantes.wkt")
                    .build()
            )
        )
    }
}
