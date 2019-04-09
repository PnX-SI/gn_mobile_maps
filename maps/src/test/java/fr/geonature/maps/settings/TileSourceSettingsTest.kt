package fr.geonature.maps.settings

import android.os.Parcel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests about [TileSourceSettings].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TileSourceSettingsTest {

    @Test
    fun testEquals() {
        assertEquals(
            TileSourceSettings(
                "nantes.mbtiles",
                "Nantes"),
            TileSourceSettings(
                "nantes.mbtiles",
                "Nantes"))
        assertNotEquals(
            TileSourceSettings(
                "nantes.mbtiles",
                "Nantes"),
            TileSourceSettings(
                "nantes.mbtiles",
                "nantes"))
    }

    @Test
    fun testParcelable() {
        // given tile source settings
        val tileSourceSettings = TileSourceSettings(
            "nantes.mbtiles",
            "Nantes")

        // when we obtain a Parcel object to write the TileSourceSettings instance to it
        val parcel = Parcel.obtain()
        tileSourceSettings.writeToParcel(
            parcel,
            0)

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            tileSourceSettings,
            TileSourceSettings.CREATOR.createFromParcel(parcel))
    }
}