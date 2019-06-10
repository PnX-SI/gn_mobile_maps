package fr.geonature.maps.settings

import android.os.Parcel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests about [MapSettings].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class MapSettingsTest {

    @Test
    fun testBuilder() {
        val nwGeoPoint = GeoPoint(
            47.253369,
            -1.605721)
        val seGeoPoint = GeoPoint(
            47.173845,
            -1.482811)

        // given map settings instance from its builder
        val mapSettings = MapSettings.Builder.newInstance()
            .baseTilesPath("/mnt/sdcard")
            .showScale(false)
            .showCompass(false)
            .zoom(8.0)
            .minZoomLevel(7.0)
            .maxZoomLevel(12.0)
            .minZoomEditing(10.0)
            .maxBounds(
                arrayListOf(
                    nwGeoPoint,
                    seGeoPoint))
            .center(
                GeoPoint.fromCenterBetween(
                    nwGeoPoint,
                    seGeoPoint))
            .addTileSource(
                "nantes.mbtiles",
                "Nantes")
            // with identical tile source
            .addTileSource(
                "nantes.mbtiles",
                "Nantes 2")
            .build()

        // then
        assertNotNull(mapSettings)
        assertEquals(
            MapSettings(
                arrayListOf(
                    TileSourceSettings(
                        "nantes.mbtiles",
                        "Nantes")),
                "/mnt/sdcard",
                false,
                false,
                8.0,
                7.0,
                12.0,
                10.0,
                BoundingBox.fromGeoPoints(
                    arrayListOf(
                        nwGeoPoint,
                        seGeoPoint)),
                GeoPoint.fromCenterBetween(
                    nwGeoPoint,
                    seGeoPoint)),
            mapSettings)
    }

    @Test
    fun testParcelable() {
        val nwGeoPoint = GeoPoint(
            47.253369,
            -1.605721)
        val seGeoPoint = GeoPoint(
            47.173845,
            -1.482811)

        // given map settings instance from its builder
        val mapSettings = MapSettings.Builder.newInstance()
            .baseTilesPath("/mnt/sdcard")
            .showScale(false)
            .showCompass(false)
            .zoom(8.0)
            .minZoomLevel(7.0)
            .maxZoomLevel(12.0)
            .minZoomEditing(10.0)
            .maxBounds(
                arrayListOf(
                    nwGeoPoint,
                    seGeoPoint))
            .center(
                GeoPoint.fromCenterBetween(
                    nwGeoPoint,
                    seGeoPoint))
            .addTileSource(
                "nantes.mbtiles",
                "Nantes")
            .build()

        // when we obtain a Parcel object to write the MapSettings instance to it
        val parcel = Parcel.obtain()
        mapSettings.writeToParcel(
            parcel,
            0)

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            mapSettings,
            MapSettings.CREATOR.createFromParcel(parcel))
    }
}