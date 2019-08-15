package fr.geonature.maps.settings

import android.os.Parcel
import org.junit.Assert.assertEquals
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

        assertEquals(LayerSettings("Nantes",
                                   "nantes.mbtiles"),
                     layerSettings)
    }

    @Test
    fun testBuilderWithDefaultLayerStyle() {
        // given a layer settings instance from its builder
        val layerSettings = LayerSettings.Builder.newInstance()
                .label("Nantes")
                .source("nantes.wkt")
                .style(LayerStyleSettings.Builder.newInstance().build())
                .build()

        assertEquals(LayerSettings("Nantes",
                                   "nantes.wkt",
                                   LayerStyleSettings()),
                     layerSettings)
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
    fun testParcelable() {
        // given a layer settings
        val layerSettings = LayerSettings("Nantes",
                                          "nantes.mbtiles")

        // when we obtain a Parcel object to write the TileSourceSettings instance to it
        val parcel = Parcel.obtain()
        layerSettings.writeToParcel(parcel,
                                    0)

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(layerSettings,
                     LayerSettings.createFromParcel(parcel))
    }
}