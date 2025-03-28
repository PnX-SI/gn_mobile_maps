package fr.geonature.maps.settings

import android.graphics.Color
import android.os.Parcel
import androidx.core.graphics.ColorUtils
import kotlinx.parcelize.parcelableCreator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [LayerPropertiesSettings].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class LayerPropertiesSettingsTest {

    @Test
    fun testValidBuilder() {
        // given a layer properties settings instance from its builder
        val layerPropertiesSettings = LayerPropertiesSettings.Builder.newInstance()
            .minZoomLevel(2)
            .maxZoomLevel(19)
            .tileSizePixels(512)
            .tileMimeType("image/jpg")
            .attribution("Some attribution")
            .style(
                LayerStyleSettings.Builder.newInstance()
                    .stroke(true)
                    .color("#FF0000")
                    .weight(10)
                    .opacity(0.9f)
                    .fill(true)
                    .fillColor("#0000FF")
                    .fillOpacity(0.25f)
                    .build()
            )
            .build()

        // then
        assertEquals(
            LayerPropertiesSettings(
                shownByDefault = true,
                minZoomLevel = 2,
                maxZoomLevel = 19,
                tileSizePixels = 512,
                tileMimeType = "image/jpg",
                attribution = "Some attribution",
                style = LayerStyleSettings(
                    true,
                    ColorUtils.setAlphaComponent(
                        Color.RED,
                        (0.9 * 255).toInt()
                    ),
                    10,
                    true,
                    ColorUtils.setAlphaComponent(
                        Color.BLUE,
                        (0.25 * 255).toInt()
                    )
                )
            ),
            layerPropertiesSettings
        )
    }

    @Test
    fun testFromExistingLayerPropertiesSettings() {
        // given a layer properties settings instance from its builder
        val layerPropertiesSettings = LayerPropertiesSettings.Builder.newInstance()
            .minZoomLevel(2)
            .maxZoomLevel(19)
            .tileSizePixels(512)
            .tileMimeType("image/jpg")
            .attribution("Some attribution")
            .style(
                LayerStyleSettings.Builder.newInstance()
                    .stroke(true)
                    .color("#FF0000")
                    .weight(10)
                    .opacity(0.9f)
                    .fill(true)
                    .fillColor("#0000FF")
                    .fillOpacity(0.25f)
                    .build()
            )
            .build()

        // then
        assertEquals(
            layerPropertiesSettings,
            LayerPropertiesSettings.Builder.newInstance()
                .from(layerPropertiesSettings)
                .build()
        )

        // when applying new properties
        val fromExistingLayerPropertiesSettings = LayerPropertiesSettings.Builder.newInstance()
            .from(layerPropertiesSettings)
            .shownByDefault(true)
            .minZoomLevel(7)
            .maxZoomLevel(8)
            .tileSizePixels(256)
            .tileMimeType("image/png")
            .attribution("Some other attribution")
            .build()

        // then
        assertEquals(
            LayerPropertiesSettings(
                shownByDefault = true,
                minZoomLevel = 7,
                maxZoomLevel = 8,
                tileSizePixels = 256,
                tileMimeType = "image/png",
                attribution = "Some other attribution",
                style = LayerStyleSettings.Builder.newInstance()
                    .stroke(true)
                    .color("#FF0000")
                    .weight(10)
                    .opacity(0.9f)
                    .fill(true)
                    .fillColor("#0000FF")
                    .fillOpacity(0.25f)
                    .build()
            ),
            fromExistingLayerPropertiesSettings
        )
    }

    @Test
    fun testBuilderWithDefaultValues() {
        // given a default layer properties settings instance from its builder
        val layerPropertiesSettings = LayerPropertiesSettings.Builder.newInstance()
            .build()

        // then
        assertEquals(
            LayerPropertiesSettings(),
            layerPropertiesSettings
        )
    }

    @Test
    fun testBuilderWithDefaultLayerStyle() {
        // given a layer properties settings instance from its builder
        val layerPropertiesSettings = LayerPropertiesSettings.Builder.newInstance()
            .minZoomLevel(2)
            .maxZoomLevel(19)
            .tileSizePixels(512)
            .tileMimeType("image/jpg")
            .attribution("Some attribution")
            .build()

        // then
        assertEquals(
            LayerPropertiesSettings(
                shownByDefault = true,
                minZoomLevel = 2,
                maxZoomLevel = 19,
                tileSizePixels = 512,
                tileMimeType = "image/jpg",
                attribution = "Some attribution",
            ),
            layerPropertiesSettings
        )
        assertNull(layerPropertiesSettings.style)
    }

    @Test
    fun testBuilderWithMinMaxLevels() {
        assertEquals(
            LayerPropertiesSettings(),
            LayerPropertiesSettings.Builder.newInstance()
                .build()
        )

        assertEquals(
            LayerPropertiesSettings(
                shownByDefault = true,
                minZoomLevel = 0,
                maxZoomLevel = 19
            ),
            LayerPropertiesSettings.Builder.newInstance()
                .minZoomLevel(-2)
                .maxZoomLevel(23)
                .build()
        )

        assertEquals(
            LayerPropertiesSettings(
                shownByDefault = true,
                minZoomLevel = 8,
                maxZoomLevel = 9
            ),
            LayerPropertiesSettings.Builder.newInstance()
                .maxZoomLevel(7)
                .minZoomLevel(8)
                .build()
        )

        assertEquals(
            LayerPropertiesSettings(
                shownByDefault = true,
                minZoomLevel = 19,
                maxZoomLevel = 19
            ),
            LayerPropertiesSettings.Builder.newInstance()
                .maxZoomLevel(19)
                .minZoomLevel(23)
                .build()
        )

        assertEquals(
            LayerPropertiesSettings(
                shownByDefault = true,
                minZoomLevel = 7,
                maxZoomLevel = 19
            ),
            LayerPropertiesSettings.Builder.newInstance()
                .minZoomLevel(7)
                .build()
        )
    }

    @Test
    fun testParcelable() {
        // given a layer properties settings
        val layerPropertiesSettings = LayerPropertiesSettings(
            shownByDefault = false,
            minZoomLevel = 2,
            maxZoomLevel = 19,
            tileSizePixels = 512,
            tileMimeType = "image/jpg",
            attribution = "Some attribution",
            style = LayerStyleSettings(
                true,
                ColorUtils.setAlphaComponent(
                    Color.RED,
                    (0.9 * 255).toInt()
                ),
                10,
                true,
                ColorUtils.setAlphaComponent(
                    Color.BLUE,
                    (0.25 * 255).toInt()
                )
            )
        )

        // when we obtain a Parcel object to write the LayerPropertiesSettings instance to it
        val parcel = Parcel.obtain()
        layerPropertiesSettings.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            layerPropertiesSettings,
            parcelableCreator<LayerPropertiesSettings>().createFromParcel(parcel)
        )
    }
}