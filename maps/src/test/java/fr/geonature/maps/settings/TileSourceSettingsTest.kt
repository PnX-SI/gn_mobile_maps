package fr.geonature.maps.settings

import android.os.Parcel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [TileSourceSettings].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class TileSourceSettingsTest {

    @Test
    fun testValidBuilder() {
        // given tile source settings instance from its builder
        val tileSourceSettings = TileSourceSettings.Builder.newInstance()
            .name("nantes.mbtiles")
            .label("Nantes")
            .minZoomLevel(8.0)
            .maxZoomLevel(15.0)
            .build()

        assertEquals(
            TileSourceSettings(
                "nantes.mbtiles",
                "Nantes",
                8.0,
                15.0),
            tileSourceSettings)

        assertNotEquals(
            TileSourceSettings(
                "nantes.mbtiles",
                "nantes",
                8.0,
                15.0),
            tileSourceSettings)
    }

    @Test
    fun testBuilderFromExistingTileSource() {
        // given tile source settings instance from its builder
        val existingTileSourceSettings = TileSourceSettings.Builder.newInstance()
            .name("nantes.mbtiles")
            .label("Nantes")
            .minZoomLevel(8.0)
            .maxZoomLevel(15.0)
            .build()

        val fromExistingTileSourceSettings = TileSourceSettings.Builder.newInstance()
            .from(existingTileSourceSettings)
            .name("nantes.wkt")
            .build()

        assertEquals(
            TileSourceSettings(
                "nantes.wkt",
                "Nantes",
                8.0,
                15.0),
            fromExistingTileSourceSettings)
    }

    @Test
    fun testBuilderWithUndefinedTileSourceLabel() {
        // given tile source settings instance from its builder
        val tileSourceSettings = TileSourceSettings.Builder.newInstance()
            .name("nantes.mbtiles")
            .minZoomLevel(8.0)
            .maxZoomLevel(15.0)
            .build()

        assertEquals(
            TileSourceSettings(
                "nantes.mbtiles",
                "nantes.mbtiles",
                8.0,
                15.0),
            tileSourceSettings)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testBuilderFromUndefinedTileSourceName() {
        TileSourceSettings.Builder.newInstance()
            .label("Nantes")
            .minZoomLevel(8.0)
            .maxZoomLevel(15.0)
            .build()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testBuilderFromEmptyTileSourceName() {
        TileSourceSettings.Builder.newInstance()
            .name("")
            .label("Nantes")
            .minZoomLevel(8.0)
            .maxZoomLevel(15.0)
            .build()
    }

    @Test
    fun testParcelable() {
        // given tile source settings
        val tileSourceSettings = TileSourceSettings(
            "nantes.mbtiles",
            "Nantes",
            8.0,
            15.0,
            0,
            null)

        // when we obtain a Parcel object to write the TileSourceSettings instance to it
        val parcel = Parcel.obtain()
        tileSourceSettings.writeToParcel(
            parcel,
            0)

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            TileSourceSettings(
                "nantes.mbtiles",
                "Nantes",
                8.0,
                15.0),
            TileSourceSettings.CREATOR.createFromParcel(parcel))
    }
}