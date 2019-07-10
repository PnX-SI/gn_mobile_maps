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
        // given tile source settings instance from its builder
        val layerSettings = LayerSettings.Builder.newInstance()
            .label("Nantes")
            .source("nantes.mbtiles")
            .build()

        assertEquals(
            LayerSettings(
                "Nantes",
                "nantes.mbtiles"
            ),
            layerSettings
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
    fun testParcelable() {
        // given tile source settings
        val tileSourceSettings = LayerSettings(
            "Nantes",
            "nantes.mbtiles"
        )

        // when we obtain a Parcel object to write the TileSourceSettings instance to it
        val parcel = Parcel.obtain()
        tileSourceSettings.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            LayerSettings(
                "Nantes",
                "nantes.mbtiles"
            ),
            LayerSettings.createFromParcel(parcel)
        )
    }
}