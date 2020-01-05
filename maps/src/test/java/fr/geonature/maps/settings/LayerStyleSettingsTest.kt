package fr.geonature.maps.settings

import android.graphics.Color
import android.os.Parcel
import androidx.core.graphics.ColorUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [LayerStyleSettings].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class LayerStyleSettingsTest {

    @Test
    fun testValidBuilder() {
        // given a layer style settings instance from its builder
        val layerStyleSettings = LayerStyleSettings.Builder.newInstance()
            .stroke(true)
            .color("#FF0000")
            .weight(10)
            .opacity(0.9f)
            .fill(true)
            .fillColor("#0000FF")
            .fillOpacity(0.25f)
            .build()

        // then
        assertEquals(
            LayerStyleSettings(
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
            ),
            layerStyleSettings
        )
    }

    @Test
    fun testFromExistingLayerStyleSettings() {
        // given a layer style settings instance from its builder
        val layerStyleSettings = LayerStyleSettings.Builder.newInstance()
            .stroke(true)
            .color("#FF0000")
            .weight(10)
            .opacity(0.9f)
            .fill(true)
            .fillColor("#0000FF")
            .fillOpacity(0.25f)
            .build()

        // then
        assertEquals(
            layerStyleSettings,
            LayerStyleSettings.Builder.newInstance().from(layerStyleSettings).build()
        )

        // when applying new style
        val fromExistingLayerStyleSettings = LayerStyleSettings.Builder.newInstance()
            .from(layerStyleSettings)
            .color("#FFFF00")
            .build()

        // then
        assertEquals(
            LayerStyleSettings(
                true,
                ColorUtils.setAlphaComponent(
                    Color.parseColor("#FFFF00"),
                    (0.9 * 255).toInt()
                ),
                10,
                true,
                ColorUtils.setAlphaComponent(
                    Color.BLUE,
                    (0.25 * 255).toInt()
                )
            ),
            fromExistingLayerStyleSettings
        )
    }

    @Test
    fun testStrokeColorWithOpacity() {
        // given a layer style settings instance from its builder
        val layerStyleSettings = LayerStyleSettings.Builder.newInstance()
            .stroke(true)
            .opacity(0.5f)
            .color("#FF0000")
            .build()

        // then
        assertEquals(
            LayerStyleSettings(
                true,
                ColorUtils.setAlphaComponent(
                    Color.RED,
                    (0.5 * 255).toInt()
                )
            ),
            layerStyleSettings
        )
    }

    @Test
    fun testOpacityToStrokeColor() {
        // given a layer style settings instance from its builder
        val layerStyleSettings = LayerStyleSettings.Builder.newInstance()
            .stroke(true)
            .color("#FF0000")
            .opacity(0.5f)
            .build()

        // then
        assertEquals(
            LayerStyleSettings(
                true,
                ColorUtils.setAlphaComponent(
                    Color.RED,
                    (0.5 * 255).toInt()
                )
            ),
            layerStyleSettings
        )
    }

    @Test
    fun testStrokeColorWithAlphaChannel() {
        // given a layer style settings instance from its builder
        val layerStyleSettings = LayerStyleSettings.Builder.newInstance()
            .stroke(true)
            .opacity(0.5f)
            .color("#99FF0000")
            .build()

        // then
        assertEquals(
            LayerStyleSettings(
                true,
                ColorUtils.setAlphaComponent(
                    Color.RED,
                    (0.6 * 255).toInt()
                )
            ),
            layerStyleSettings
        )
    }

    @Test
    fun testOpacityToStrokeColorWithAlphaChannel() {
        // given a layer style settings instance from its builder
        val layerStyleSettings = LayerStyleSettings.Builder.newInstance()
            .stroke(true)
            .color("#99FF0000")
            .opacity(0.5f)
            .build()

        // then
        assertEquals(
            LayerStyleSettings(
                true,
                ColorUtils.setAlphaComponent(
                    Color.RED,
                    (0.5 * 255).toInt()
                )
            ),
            layerStyleSettings
        )
    }

    @Test
    fun testFillColorWithOpacity() {
        // given a layer style settings instance from its builder
        val layerStyleSettings = LayerStyleSettings.Builder.newInstance()
            .fill(true)
            .fillOpacity(0.5f)
            .fillColor("#0000FF")
            .build()

        // then
        assertEquals(
            LayerStyleSettings(
                true,
                Color.DKGRAY,
                8,
                true,
                ColorUtils.setAlphaComponent(
                    Color.BLUE,
                    (0.5 * 255).toInt()
                )
            ),
            layerStyleSettings
        )
    }

    @Test
    fun testOpacityToFillColor() {
        // given a layer style settings instance from its builder
        val layerStyleSettings = LayerStyleSettings.Builder.newInstance()
            .fill(true)
            .fillColor("#0000FF")
            .fillOpacity(0.5f)
            .build()

        // then
        assertEquals(
            LayerStyleSettings(
                true,
                Color.DKGRAY,
                8,
                true,
                ColorUtils.setAlphaComponent(
                    Color.BLUE,
                    (0.5 * 255).toInt()
                )
            ),
            layerStyleSettings
        )
    }

    @Test
    fun testFillColorWithAlphaChannel() {
        // given a layer style settings instance from its builder
        val layerStyleSettings = LayerStyleSettings.Builder.newInstance()
            .fill(true)
            .fillOpacity(0.5f)
            .fillColor("#990000FF")
            .build()

        // then
        assertEquals(
            LayerStyleSettings(
                true,
                Color.DKGRAY,
                8,
                true,
                ColorUtils.setAlphaComponent(
                    Color.BLUE,
                    (0.6 * 255).toInt()
                )
            ),
            layerStyleSettings
        )
    }

    @Test
    fun testOpacityToFillColorWithAlphaChannel() {
        // given a layer style settings instance from its builder
        val layerStyleSettings = LayerStyleSettings.Builder.newInstance()
            .fill(true)
            .fillColor("#990000FF")
            .fillOpacity(0.5f)
            .build()

        // then
        assertEquals(
            LayerStyleSettings(
                true,
                Color.DKGRAY,
                8,
                true,
                ColorUtils.setAlphaComponent(
                    Color.BLUE,
                    (0.5 * 255).toInt()
                )
            ),
            layerStyleSettings
        )
    }

    @Test
    fun testParcelable() {
        // given a layer style settings
        val layerStyleSettings = LayerStyleSettings(
            true,
            ColorUtils.setAlphaComponent(
                Color.RED,
                (0.9 * 255).toInt()
            ),
            10,
            true,
            ColorUtils.setAlphaComponent(
                Color.RED,
                (0.25 * 255).toInt()
            )
        )

        // when we obtain a Parcel object to write the LayerStyleSettings instance to it
        val parcel = Parcel.obtain()
        layerStyleSettings.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            layerStyleSettings,
            LayerStyleSettings.createFromParcel(parcel)
        )
    }
}
