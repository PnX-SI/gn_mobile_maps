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
 * Unit tests about [OpenTopoMapOnlineLayerTileSource].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
internal class OpenTopoMapOnlineLayerTileSourceTest {

    private lateinit var application: Application

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `should build OpenTopoMapOnlineLayerTileSource from valid layer settings`() {
        // given a valid layer settings
        val layerSettings = LayerSettings(
            label = "OSM",
            source = listOf("https://a.tile.opentopomap.org"),
        )

        // when trying to build the corresponding tile source
        val onlineLayerTileSource = OpenTopoMapOnlineLayerTileSource(
            application,
            layerSettings
        )

        // then
        assertEquals(
            layerSettings.label,
            onlineLayerTileSource.name()
        )
        assertTrue(onlineLayerTileSource.baseUrl.contains("tile.opentopomap.org"))
        assertEquals(
            0,
            onlineLayerTileSource.minimumZoomLevel
        )
        assertEquals(
            17,
            onlineLayerTileSource.maximumZoomLevel
        )
        assertEquals(
            ".png",
            onlineLayerTileSource.imageFilenameEnding()
        )
        assertEquals(
            application.getString(R.string.layer_otm_attribution),
            onlineLayerTileSource.copyrightNotice
        )
        assertTrue(
            onlineLayerTileSource.getTileURLString(0)
                .contains("tile.opentopomap.org/0/0/0.png")
        )
    }

    @Test
    fun `should set the right min and max zoom level from valid layer settings`() {
        // given a valid layer settings
        val layerSettings = LayerSettings(
            label = "OSM",
            source = listOf("https://a.tile.opentopomap.org"),
            properties = LayerPropertiesSettings(
                minZoomLevel = 5,
                maxZoomLevel = 21
            )
        )

        // when trying to build the corresponding tile source
        val onlineLayerTileSource = OpenTopoMapOnlineLayerTileSource(
            application,
            layerSettings
        )

        // then
        assertEquals(
            5,
            onlineLayerTileSource.minimumZoomLevel
        )
        assertEquals(
            17,
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

        assertThrows(LayerException.InvalidLayerException::class.java) {
            OpenTopoMapOnlineLayerTileSource(
                application,
                layerSettings
            )
        }
    }
}