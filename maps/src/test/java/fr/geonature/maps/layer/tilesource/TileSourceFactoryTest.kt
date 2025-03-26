package fr.geonature.maps.layer.tilesource

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import fr.geonature.maps.layer.error.LayerException
import fr.geonature.maps.settings.LayerSettings
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [TileSourceFactory].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
internal class TileSourceFactoryTest {

    private lateinit var application: Application

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `should get online tile source from valid layer settings`() {
        assertTrue(
            TileSourceFactory.getOnlineTileSource(
                application,
                LayerSettings(
                    label = "IGN: Plan v2",
                    source = listOf("https://data.geopf.fr/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&STYLE=normal&TILEMATRIXSET=PM&FORMAT=image/png&LAYER=GEOGRAPHICALGRIDSYSTEMS.PLANIGNV2&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}"),
                )
            ) is GeoportailWMTSOnlineLayerTileSource
        )

        assertTrue(
            TileSourceFactory.getOnlineTileSource(
                application,
                LayerSettings(
                    label = "OSM",
                    source = listOf("https://a.tile.opentopomap.org"),
                )
            ) is OpenTopoMapOnlineLayerTileSource
        )

        assertTrue(
            TileSourceFactory.getOnlineTileSource(
                application,
                LayerSettings(
                    label = "OSM",
                    source = listOf("https://a.tile.openstreetmap.org"),
                )
            ) is OSMOnlineLayerTileSource
        )

        assertTrue(
            TileSourceFactory.getOnlineTileSource(
                application,
                LayerSettings(
                    label = "Wikimedia",
                    source = listOf("https://maps.wikimedia.org/osm-intl"),
                )
            ) is WikimediaOnlineLayerTileSource
        )
    }

    @Test
    fun `should throw InvalidOnlineLayerException if the given layer settings is not an online source`() {
        assertThrows(LayerException.InvalidOnlineLayerException::class.java) {
            TileSourceFactory.getOnlineTileSource(
                application,
                LayerSettings(
                    "Nantes",
                    listOf("nantes.wkt")
                )
            )
        }
    }

    @Test
    fun `should throw NotFoundException if the given layer settings is not eligible`() {
        assertThrows(LayerException.NotSupportedException::class.java) {
            TileSourceFactory.getOnlineTileSource(
                application,
                LayerSettings(
                    "CloudMade",
                    listOf("http://a.tile.cloudmade.com")
                )
            )
        }
    }
}