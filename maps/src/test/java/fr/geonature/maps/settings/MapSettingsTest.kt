package fr.geonature.maps.settings

import android.os.Parcel
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.robolectric.RobolectricTestRunner

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
            -1.605721
        )
        val seGeoPoint = GeoPoint(
            47.173845,
            -1.482811
        )

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
                    seGeoPoint
                )
            )
            .center(
                GeoPoint.fromCenterBetween(
                    nwGeoPoint,
                    seGeoPoint
                )
            )
            .addLayer(
                "Nantes",
                "nantes.mbtiles"
            )
            // with identical tile source
            .addLayer(
                "Nantes 2",
                "nantes.mbtiles"
            )
            .build()

        // then
        assertNotNull(mapSettings)
        assertEquals(
            MapSettings(
                arrayListOf(
                    LayerSettings(
                        "Nantes",
                        "nantes.mbtiles"
                    )
                ),
                "/mnt/sdcard",
                showScale = false,
                showCompass = false,
                zoom = 8.0,
                minZoomLevel = 7.0,
                maxZoomLevel = 12.0,
                minZoomEditing = 10.0,
                maxBounds = BoundingBox.fromGeoPoints(
                    arrayListOf(
                        nwGeoPoint,
                        seGeoPoint
                    )
                ),
                center = GeoPoint.fromCenterBetween(
                    nwGeoPoint,
                    seGeoPoint
                )
            ),
            mapSettings
        )
    }

    @Test
    fun testFromExistingMapSettings() {
        val nwGeoPoint = GeoPoint(
                47.253369,
                -1.605721
        )
        val seGeoPoint = GeoPoint(
                47.173845,
                -1.482811
        )

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
                                seGeoPoint
                        )
                )
                .center(
                        GeoPoint.fromCenterBetween(
                                nwGeoPoint,
                                seGeoPoint
                        )
                )
                .addLayer(
                        "Nantes",
                        "nantes.mbtiles"
                )
                // with identical tile source
                .addLayer(
                        "Nantes 2",
                        "nantes.mbtiles"
                )
                .build()

        // then
        assertEquals(mapSettings,
                     MapSettings.Builder.newInstance().from(mapSettings).build())
    }

    @Test
    fun testGetLayersAsTileSources() {
        val nwGeoPoint = GeoPoint(
            47.253369,
            -1.605721
        )
        val seGeoPoint = GeoPoint(
            47.173845,
            -1.482811
        )

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
                    seGeoPoint
                )
            )
            .center(
                GeoPoint.fromCenterBetween(
                    nwGeoPoint,
                    seGeoPoint
                )
            )
            .addLayer(
                "Nantes",
                "nantes.mbtiles"
            )
            .addLayer(
                "nantes.wkt",
                "nantes.wkt"
            )
            .build()

        // then
        assertNotNull(mapSettings)
        assertArrayEquals(
            arrayOf("nantes.mbtiles"),
            mapSettings.getLayersAsTileSources().toTypedArray()
        )
    }

    @Test
    fun testGetVectorLayers() {
        val nwGeoPoint = GeoPoint(
            47.253369,
            -1.605721
        )
        val seGeoPoint = GeoPoint(
            47.173845,
            -1.482811
        )

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
                    seGeoPoint
                )
            )
            .center(
                GeoPoint.fromCenterBetween(
                    nwGeoPoint,
                    seGeoPoint
                )
            )
            .addLayer(
                "Nantes",
                "nantes.mbtiles"
            )
            .addLayer(
                "nantes.wkt",
                "nantes.wkt"
            )
            .addLayer(
                "nantes.json",
                "nantes.json"
            )
            .addLayer(
                "nantes.geojson",
                "nantes.geojson"
            )
            .build()

        // then
        assertNotNull(mapSettings)
        assertArrayEquals(
            arrayOf(
                LayerSettings(
                    "nantes.wkt",
                    "nantes.wkt"
                ),
                LayerSettings(
                    "nantes.json",
                    "nantes.json"
                ),
                LayerSettings(
                    "nantes.geojson",
                    "nantes.geojson"
                )
            ),
            mapSettings.getVectorLayers().toTypedArray()
        )
    }

    @Test
    fun testParcelable() {
        val nwGeoPoint = GeoPoint(
            47.253369,
            -1.605721
        )
        val seGeoPoint = GeoPoint(
            47.173845,
            -1.482811
        )

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
                    seGeoPoint
                )
            )
            .center(
                GeoPoint.fromCenterBetween(
                    nwGeoPoint,
                    seGeoPoint
                )
            )
            .addLayer(
                "nantes.mbtiles",
                "Nantes"
            )
            .build()

        // when we obtain a Parcel object to write the MapSettings instance to it
        val parcel = Parcel.obtain()
        mapSettings.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            mapSettings,
            MapSettings.createFromParcel(parcel)
        )
    }
}