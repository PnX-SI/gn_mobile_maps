package fr.geonature.maps.layer.tilesource

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import fr.geonature.maps.R
import fr.geonature.maps.layer.LayerException
import fr.geonature.maps.settings.LayerPropertiesSettings
import fr.geonature.maps.settings.LayerSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [OSMOnlineLayerTileSource].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
internal class OSMOnlineLayerTileSourceTest {

    private lateinit var application: Application

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `should build OSMOnlineLayerTileSource from valid layer settings`() {
        // given a valid layer settings
        val layerSettings = LayerSettings(
            label = "OSM",
            source = listOf("https://a.tile.openstreetmap.org"),
        )

        // when trying to build the corresponding tile source
        val onlineLayerTileSource = OSMOnlineLayerTileSource(
            application,
            layerSettings
        )

        // then
        assertEquals(
            layerSettings.label,
            onlineLayerTileSource.name()
        )
        assertTrue(onlineLayerTileSource.baseUrl.contains("tile.openstreetmap.org"))
        assertEquals(
            0,
            onlineLayerTileSource.minimumZoomLevel
        )
        assertEquals(
            19,
            onlineLayerTileSource.maximumZoomLevel
        )
        assertEquals(
            ".png",
            onlineLayerTileSource.imageFilenameEnding()
        )
        assertEquals(
            application.getString(R.string.layer_osm_attribution),
            onlineLayerTileSource.copyrightNotice
        )
        assertTrue(
            onlineLayerTileSource.getTileURLString(0)
                .contains("tile.openstreetmap.org/0/0/0.png")
        )
    }

    @Test
    fun `should set the right min and max zoom level from valid layer settings`() {
        // given a valid layer settings
        val layerSettings = LayerSettings(
            label = "OSM",
            source = listOf("https://a.tile.openstreetmap.org"),
            properties = LayerPropertiesSettings(
                minZoomLevel = 5,
                maxZoomLevel = 21
            )
        )

        // when trying to build the corresponding tile source
        val onlineLayerTileSource = OSMOnlineLayerTileSource(
            application,
            layerSettings
        )

        // then
        assertEquals(
            5,
            onlineLayerTileSource.minimumZoomLevel
        )
        assertEquals(
            19,
            onlineLayerTileSource.maximumZoomLevel
        )
    }

    @Test
    fun `should throw InvalidLayerException if the given layer URL is not eligible`() {
        // given an invalid layer settings
        val layerSettings = LayerSettings(
            label = "OTM",
            source = listOf("https://a.tile.opentopomap.org"),
        )

        assertThrows(LayerException.InvalidLayerException::class.java) {
            OSMOnlineLayerTileSource(
                application,
                layerSettings
            )
        }
    }
}