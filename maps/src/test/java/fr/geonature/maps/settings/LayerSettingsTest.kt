package fr.geonature.maps.settings

import android.os.Parcel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [LayerSettings].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class LayerSettingsTest {

    @Test
    fun testValidBuilder() {
        // given a layer settings instance from its builder
        val layerSettings = LayerSettings.Builder.newInstance()
            .label("Nantes")
            .source("nantes.mbtiles")
            .build()

        // then
        assertEquals(
            LayerSettings(
                "Nantes",
                "nantes.mbtiles"
            ),
            layerSettings
        )
    }

    @Test
    fun testBuilderWithDefaultProperties() {
        assertEquals(
            LayerSettings(
                "Nantes",
                "nantes.unknown"
            ),
            LayerSettings.Builder.newInstance()
                .label("Nantes")
                .source("nantes.unknown")
                .build()
        )

        assertEquals(
            LayerSettings(
                "OSM",
                "https://a.tile.openstreetmap.org",
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
                .source("https://a.tile.openstreetmap.org")
                .build()
        )

        assertEquals(
            LayerSettings(
                "Nantes",
                "nantes.wkt",
                LayerPropertiesSettings(
                    style = LayerStyleSettings()
                )
            ),
            LayerSettings.Builder.newInstance()
                .label("Nantes")
                .source("nantes.wkt")
                .build()
        )

        assertEquals(
            LayerSettings(
                "Nantes",
                "nantes.geojson",
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
                .source("nantes.geojson")
                .build()
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun testBuilderWithUndefinedLayerSourceLabel() {
        LayerSettings.Builder.newInstance()
            .source("nantes.mbtiles")
            .build()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testBuilderFromUndefinedLayerSourceName() {
        LayerSettings.Builder.newInstance()
            .label("Nantes")
            .build()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testBuilderFromEmptyLayerSourceName() {
        LayerSettings.Builder.newInstance()
            .label("Nantes")
            .source("")
            .build()
    }

    @Test
    fun testGetType() {
        assertEquals(
            LayerType.TILES,
            LayerSettings(
                "OSM",
                "https://a.tile.openstreetmap.org"
            ).getType()
        )

        assertEquals(
            LayerType.TILES,
            LayerSettings(
                "Nantes",
                "nantes.mbtiles"
            ).getType()
        )

        assertEquals(
            LayerType.VECTOR,
            LayerSettings(
                "Nantes",
                "nantes.wkt"
            ).getType()
        )

        assertEquals(
            LayerType.VECTOR,
            LayerSettings(
                "Nantes",
                "nantes.json"
            ).getType()
        )

        assertEquals(
            LayerType.VECTOR,
            LayerSettings(
                "Nantes",
                "nantes.geojson"
            ).getType()
        )

        assertEquals(
            LayerType.NOT_IMPLEMENTED,
            LayerSettings(
                "Nantes",
                "nantes.unknown"
            ).getType()
        )
    }

    @Test
    fun testActiveLayer() {
        assertTrue(
            LayerSettings.Builder.newInstance()
                .label("Nantes")
                .source("nantes.mbtiles")
                .build().properties.active
        )

        assertTrue(
            LayerSettings.Builder.newInstance()
                .label("Nantes")
                .source("nantes.mbtiles")
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
                .source("nantes.mbtiles")
                .properties(
                    LayerPropertiesSettings.Builder.newInstance()
                        .active(false)
                        .build()
                )
                .build().properties.active
        )
    }

    @Test
    fun testParcelable() {
        // given a layer settings
        val layerSettings = LayerSettings(
            "Nantes",
            "nantes.mbtiles"
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
            LayerSettings.createFromParcel(parcel)
        )
    }

    @Test
    fun testComparable() {
        assertTrue(
            LayerSettings(
                "OSM",
                "https://a.tile.openstreetmap.org"
            ) < LayerSettings(
                "Nantes",
                "nantes.mbtiles"
            )
        )
        assertTrue(
            LayerSettings(
                "OSM",
                "https://a.tile.openstreetmap.org"
            ) == LayerSettings(
                "OSM",
                "https://a.tile.openstreetmap.org"
            )
        )
        assertTrue(
            LayerSettings(
                "OSM",
                "https://a.tile.openstreetmap.org"
            ) < LayerSettings(
                "OSM #1",
                "https://a.tile.openstreetmap.org"
            )
        )
        assertTrue(
            LayerSettings(
                "OSM",
                "https://a.tile.openstreetmap.org"
            ) < LayerSettings(
                "OSM",
                "https://b.tile.openstreetmap.org"
            )
        )
        assertTrue(
            LayerSettings(
                "Nantes",
                "nantes.mbtiles"
            ) < LayerSettings(
                "Nantes #1",
                "nantes2.mbtiles"
            )
        )
        assertTrue(
            LayerSettings(
                "Nantes",
                "nantes.mbtiles"
            ) < LayerSettings(
                "Nantes",
                "nantes2.mbtiles"
            )
        )
        assertTrue(
            LayerSettings(
                "Nantes",
                "nantes.mbtiles"
            ) == LayerSettings(
                "Nantes",
                "nantes.mbtiles"
            )
        )
        assertTrue(
            LayerSettings(
                "Nantes",
                "nantes.mbtiles"
            ) < LayerSettings(
                "Nantes",
                "nantes.unknown"
            )
        )
    }
}
