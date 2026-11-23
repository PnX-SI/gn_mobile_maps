package fr.geonature.maps.maplibre.layer

import fr.geonature.maps.settings.LayerPropertiesSettings
import fr.geonature.maps.settings.LayerSettings
import fr.geonature.maps.settings.MapSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

/**
 * Unit tests about [BaseLayerSelector] and [TileUrlTemplates].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class BaseLayerSelectorTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val osm = LayerSettings.Builder.newInstance()
        .label("OSM")
        .addSource("https://a.tile.openstreetmap.org")
        .addSource("https://b.tile.openstreetmap.org")
        .build()

    private val ignOnline = LayerSettings.Builder.newInstance()
        .label("IGN")
        .addSource("https://data.geopf.fr/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&STYLE=normal&TILEMATRIXSET=PM&FORMAT=image/png&LAYER=GEOGRAPHICALGRIDSYSTEMS.PLANIGNV2")
        .build()

    @Test
    fun `should select the first online layer if no local layer is defined`() {
        val mapSettings = MapSettings.Builder()
            .addLayer(ignOnline)
            .addLayer(osm)
            .build()

        val baseLayer = BaseLayerSelector.select(mapSettings) { emptyList() }

        assertTrue(baseLayer is BaseLayer.Online)
        assertEquals(
            ignOnline.label,
            baseLayer?.settings?.label
        )
    }

    @Test
    fun `should prefer the first embedded layer over online layers`() {
        val mbtiles = temporaryFolder.newFile("nantes.mbtiles")
        val pmtiles = temporaryFolder.newFile("rennes.pmtiles")
        val mbtilesLayer = LayerSettings.Builder.newInstance()
            .label("Nantes")
            .addSource("nantes.mbtiles")
            .build()
        val pmtilesLayer = LayerSettings.Builder.newInstance()
            .label("Rennes")
            .addSource("rennes.pmtiles")
            .build()
        val mapSettings = MapSettings.Builder()
            .addLayer(osm)
            .addLayer(mbtilesLayer)
            .addLayer(pmtilesLayer)
            .build()

        val baseLayer = BaseLayerSelector.select(mapSettings) {
            when (it.label) {
                "Nantes" -> listOf(mbtiles)
                "Rennes" -> listOf(pmtiles)
                else -> emptyList()
            }
        }

        assertTrue(baseLayer is BaseLayer.Local)
        assertEquals(
            mbtiles,
            (baseLayer as BaseLayer.Local).file
        )
    }

    @Test
    fun `should skip embedded layers not found and fallback to online layer`() {
        val mapSettings = MapSettings.Builder()
            .addLayer(osm)
            .addLayer(
                LayerSettings.Builder.newInstance()
                    .label("Nantes")
                    .addSource("nantes.mbtiles")
                    .build()
            )
            .build()

        val baseLayer = BaseLayerSelector.select(mapSettings) { listOf(File("/does/not/exist.mbtiles")) }

        assertTrue(baseLayer is BaseLayer.Online)
        assertEquals(
            "OSM",
            baseLayer?.settings?.label
        )
    }

    @Test
    fun `should prefer embedded layers shown by default`() {
        val first = temporaryFolder.newFile("first.pmtiles")
        val second = temporaryFolder.newFile("second.pmtiles")
        val mapSettings = MapSettings.Builder()
            .addLayer(
                LayerSettings.Builder.newInstance()
                    .label("First")
                    .addSource("first.pmtiles")
                    .properties(
                        LayerPropertiesSettings.Builder.newInstance()
                            .shownByDefault(false)
                            .build()
                    )
                    .build()
            )
            .addLayer(
                LayerSettings.Builder.newInstance()
                    .label("Second")
                    .addSource("second.pmtiles")
                    .build()
            )
            .build()

        val baseLayer = BaseLayerSelector.select(mapSettings) {
            listOf(if (it.label == "First") first else second)
        }

        assertEquals(
            second,
            (baseLayer as BaseLayer.Local).file
        )
    }

    @Test
    fun `should not select any online layer if online layers are disabled`() {
        val mapSettings = MapSettings.Builder()
            .useOnlineLayers(false)
            .addLayer(osm)
            .build()

        assertNull(BaseLayerSelector.select(mapSettings) { emptyList() })
    }

    @Test
    fun `should build ZXY tile URL templates`() {
        assertEquals(
            listOf(
                "https://a.tile.openstreetmap.org/{z}/{x}/{y}.png",
                "https://b.tile.openstreetmap.org/{z}/{x}/{y}.png"
            ),
            TileUrlTemplates.fromLayerSettings(osm)
        )
    }

    @Test
    fun `should build WMTS tile URL templates`() {
        val templates = TileUrlTemplates.fromLayerSettings(ignOnline)

        assertEquals(
            listOf("https://data.geopf.fr/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&STYLE=normal&TILEMATRIXSET=PM&FORMAT=image/png&LAYER=GEOGRAPHICALGRIDSYSTEMS.PLANIGNV2&TILEMATRIX={z}&TILECOL={x}&TILEROW={y}"),
            templates
        )
        assertNotNull(TileUrlTemplates.defaultAttribution(templates.first()))
    }
}

