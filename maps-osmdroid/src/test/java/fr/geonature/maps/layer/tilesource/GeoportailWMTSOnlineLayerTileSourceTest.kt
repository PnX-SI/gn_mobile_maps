package fr.geonature.maps.layer.tilesource

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import fr.geonature.maps.R
import fr.geonature.maps.layer.error.LayerException
import fr.geonature.maps.settings.LayerPropertiesSettings
import fr.geonature.maps.settings.LayerSettings
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [GeoportailWMTSOnlineLayerTileSource].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
internal class GeoportailWMTSOnlineLayerTileSourceTest {

    private lateinit var application: Application

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `should build GeoportailWMTSOnlineLayerTileSource from valid layer settings`() {
        // given a valid layer settings
        val layerSettings = LayerSettings(
            label = "IGN: Plan v2",
            source = listOf("https://data.geopf.fr/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&STYLE=normal&TILEMATRIXSET=PM&FORMAT=image/png&LAYER=GEOGRAPHICALGRIDSYSTEMS.PLANIGNV2&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}"),
        )

        // when trying to build the corresponding tile source
        val onlineLayerTileSource = GeoportailWMTSOnlineLayerTileSource(
            application,
            layerSettings
        )

        // then
        assertEquals(
            layerSettings.label,
            onlineLayerTileSource.name()
        )
        assertEquals(
            layerSettings.getPrimarySource(),
            onlineLayerTileSource.baseUrl
        )
        assertEquals(
            0,
            onlineLayerTileSource.minimumZoomLevel
        )
        assertEquals(
            21,
            onlineLayerTileSource.maximumZoomLevel
        )
        assertEquals(
            ".png",
            onlineLayerTileSource.imageFilenameEnding()
        )
        assertEquals(
            application.getString(R.string.layer_geoportail_attribution),
            onlineLayerTileSource.copyrightNotice
        )
        assertEquals(
            "https://data.geopf.fr/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&STYLE=normal&TILEMATRIXSET=PM&FORMAT=image%2Fpng&LAYER=GEOGRAPHICALGRIDSYSTEMS.PLANIGNV2&TILEMATRIX=0&TILEROW=0&TILECOL=0",
            onlineLayerTileSource.getTileURLString(0)
        )
    }

    @Test
    fun `should set the right min and max zoom level from valid layer settings`() {
        // given a valid layer settings
        val layerSettings = LayerSettings(
            label = "IGN: Plan v2",
            source = listOf("https://data.geopf.fr/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&STYLE=normal&TILEMATRIXSET=PM&FORMAT=image/png&LAYER=GEOGRAPHICALGRIDSYSTEMS.PLANIGNV2&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}"),
            properties = LayerPropertiesSettings(
                minZoomLevel = 5,
                maxZoomLevel = 22
            )
        )

        // when trying to build the corresponding tile source
        val onlineLayerTileSource = GeoportailWMTSOnlineLayerTileSource(
            application,
            layerSettings
        )

        // then
        assertEquals(
            5,
            onlineLayerTileSource.minimumZoomLevel
        )
        assertEquals(
            21,
            onlineLayerTileSource.maximumZoomLevel
        )
    }

    @Test
    fun `should throw InvalidLayerException if the given layer URL is not eligible`() {
        // given an invalid layer settings
        val layerSettings = LayerSettings(
            label = "OSM",
            source = listOf("https://a.tile.openstreetmap.org"),
        )

        Assert.assertThrows(LayerException.InvalidLayerException::class.java) {
            GeoportailWMTSOnlineLayerTileSource(
                application,
                layerSettings
            )
        }
    }
}