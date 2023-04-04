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
 * Unit tests about [WikimediaOnlineLayerTileSource].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
internal class WikimediaOnlineLayerTileSourceTest {

    private lateinit var application: Application

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `should build WikimediaOnlineLayerTileSource from valid layer settings`() {
        // given a valid layer settings
        val layerSettings = LayerSettings(
            label = "Wikimedia",
            source = listOf("https://maps.wikimedia.org/osm-intl"),
        )

        // when trying to build the corresponding tile source
        val onlineLayerTileSource = WikimediaOnlineLayerTileSource(
            application,
            layerSettings
        )

        // then
        assertEquals(
            layerSettings.label,
            onlineLayerTileSource.name()
        )
        assertTrue(layerSettings.source.contains(onlineLayerTileSource.baseUrl))
        assertEquals(
            1,
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
            application.getString(R.string.layer_wikimedia_attribution),
            onlineLayerTileSource.copyrightNotice
        )
        assertEquals(
            "https://maps.wikimedia.org/osm-intl/0/0/0.png",
            onlineLayerTileSource.getTileURLString(0)
        )
    }

    @Test
    fun `should set the right min and max zoom level from valid layer settings`() {
        // given a valid layer settings
        val layerSettings = LayerSettings(
            label = "Wikimedia",
            source = listOf("https://maps.wikimedia.org/osm-intl"),
            properties = LayerPropertiesSettings(
                minZoomLevel = 5,
                maxZoomLevel = 21
            )
        )

        // when trying to build the corresponding tile source
        val onlineLayerTileSource = WikimediaOnlineLayerTileSource(
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
            WikimediaOnlineLayerTileSource(
                application,
                layerSettings
            )
        }
    }
}