package fr.geonature.maps.layer.repository

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import fr.geonature.maps.CoroutineTestRule
import fr.geonature.maps.layer.data.ILayerLocalDataSource
import fr.geonature.maps.layer.data.ISelectedLayersLocalDataSource
import fr.geonature.maps.layer.domain.LayerState
import fr.geonature.maps.layer.error.LayerException
import fr.geonature.maps.layer.tilesource.TileSourceFactory
import fr.geonature.maps.settings.LayerSettings
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [LayerRepositoryImpl].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
internal class LayerRepositoryImplTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var application: Application
    private lateinit var localLayerDataSource: ILayerLocalDataSource
    private lateinit var selectedLayersLocalDataSource: ISelectedLayersLocalDataSource
    private lateinit var repository: LayerRepositoryImpl

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        localLayerDataSource = mockk()
        selectedLayersLocalDataSource = mockk()
        repository = LayerRepositoryImpl(
            context = application,
            localLayerDataSource = localLayerDataSource,
            selectedLayersLocalDataSource = selectedLayersLocalDataSource
        )
        mockkObject(TileSourceFactory)
    }

    @After
    fun tearDown() {
        unmockkObject(TileSourceFactory)
        clearAllMocks()
    }

    @Test
    fun `prepareLayers() should return LayerState Layer for each online layer`() = runTest {
        // given sone online layer
        val onlineLayerSettings = LayerSettings.Builder.newInstance()
            .label("OSM")
            .addSource("https://a.tile.openstreetmap.org/{z}/{x}/{y}.png")
            .build()

        // when
        val result = repository.prepareLayers(listOf(onlineLayerSettings))

        // then
        assertEquals(1, result.size)
        val layerState = result.first()
        assertTrue(layerState is LayerState.Layer)
        assertEquals(onlineLayerSettings, (layerState as LayerState.Layer).settings)
        assertEquals(
            onlineLayerSettings.getSourcesAsUri(),
            layerState.source
        )
    }

    @Test
    fun `prepareLayers() should return LayerState Layer for each successfully resolved local layer`() =
        runTest {
            // given some local layer
            val localLayerSettings = LayerSettings.Builder.newInstance()
                .label("Nantes")
                .addSource("osmdroid/nantes.mbtiles")
                .build()
            val expectedUris = listOf(Uri.parse("file:///storage/emulated/0/Android/data/fr.geonature.maps/files/osmdroid/nantes.mbtiles"))

            coEvery {
                localLayerDataSource.resolvesLocalLayerFromLayerSettings(
                    localLayerSettings,
                    null
                )
            } returns expectedUris

            // when
            val result = repository.prepareLayers(listOf(localLayerSettings))

            // then
            assertEquals(1, result.size)
            val layerState = result.first()
            assertTrue(layerState is LayerState.Layer)
            assertEquals(localLayerSettings, (layerState as LayerState.Layer).settings)
            assertEquals(expectedUris, layerState.source)
        }

    @Test
    fun `prepareLayers() should return LayerState Error when local layer resolution throws LayerException`() =
        runTest {
            // given some not found local layer
            val localLayerSettings = LayerSettings.Builder.newInstance()
                .label("Missing layer")
                .addSource("osmdroid/missing.mbtiles")
                .build()
            val expectedException = LayerException.NotFoundException(localLayerSettings)

            coEvery {
                localLayerDataSource.resolvesLocalLayerFromLayerSettings(
                    localLayerSettings,
                    null
                )
            } throws expectedException

            // when
            val result = repository.prepareLayers(listOf(localLayerSettings))

            // then
            assertEquals(1, result.size)
            val layerState = result.first()
            assertTrue(layerState is LayerState.Error)
            assertEquals(expectedException, (layerState as LayerState.Error).error)
        }

    @Test
    fun `prepareLayers() should return LayerState Error with NotSupportedException when local layer resolution throws unexpected exception`() =
        runTest {
            // given some local layer with invalid source
            val localLayerSettings = LayerSettings.Builder.newInstance()
                .label("Nantes POIs")
                .addSource("osmdroid/nantes_pois.geojson")
                .build()

            coEvery {
                localLayerDataSource.resolvesLocalLayerFromLayerSettings(
                    localLayerSettings,
                    any()
                )
            } throws RuntimeException("Unexpected error")

            // when
            val result = repository.prepareLayers(listOf(localLayerSettings))

            // then
            assertEquals(1, result.size)
            val layerState = result.first()
            assertTrue(layerState is LayerState.Error)
            assertTrue((layerState as LayerState.Error).error is LayerException.NotSupportedException)
            assertEquals(localLayerSettings, layerState.error.layerSettings)
        }

    @Test
    fun `prepareLayers() should handle mixed online and local layers`() = runTest {
        // given some layers
        val onlineLayerSettings = LayerSettings.Builder.newInstance()
            .label("OSM")
            .addSource("https://a.tile.openstreetmap.org/{z}/{x}/{y}.png")
            .build()
        val localLayerSettings = LayerSettings.Builder.newInstance()
            .label("Nantes")
            .addSource("osmdroid/nantes.mbtiles")
            .build()
        val expectedUris = listOf(Uri.parse("file:///storage/emulated/0/Android/data/fr.geonature.maps/files/osmdroid/nantes.mbtiles"))

        coEvery {
            localLayerDataSource.resolvesLocalLayerFromLayerSettings(
                localLayerSettings,
                any()
            )
        } returns expectedUris

        // when
        val result = repository.prepareLayers(
            listOf(onlineLayerSettings, localLayerSettings),
            "/storage/emulated/0"
        )

        // then
        assertEquals(2, result.size)
        assertTrue(result[0] is LayerState.Layer)
        assertTrue(result[1] is LayerState.Layer)
        assertEquals(onlineLayerSettings, (result[0] as LayerState.Layer).settings)
        assertEquals(localLayerSettings, (result[1] as LayerState.Layer).settings)
    }

    @Test
    fun `prepareLayers() should replace previous layers in internal state`() = runTest {
        // given - first call
        val firstLayer = LayerSettings.Builder.newInstance()
            .label("Layer 1")
            .addSource("https://a.tile.openstreetmap.org/{z}/{x}/{y}.png")
            .build()
        repository.prepareLayers(listOf(firstLayer))

        // and second call with a different list
        val secondLayer = LayerSettings.Builder.newInstance()
            .label("Layer 2")
            .addSource("https://b.tile.openstreetmap.org/{z}/{x}/{y}.png")
            .build()

        // when
        repository.prepareLayers(listOf(secondLayer))

        // then
        val allLayers = repository.getAllLayers()
        assertEquals(1, allLayers.size)
        assertEquals(secondLayer, (allLayers.first() as LayerState.Layer).settings)
    }

    @Test
    fun `prepareLayerFromSettings() should return LayerState Layer for valid online layer`() =
        runTest {
            // given some online layer
            val onlineLayerSettings = LayerSettings.Builder.newInstance()
                .label("OSM")
                .addSource("https://a.tile.openstreetmap.org/{z}/{x}/{y}.png")
                .build()
            val mockTileSource = mockk<fr.geonature.maps.layer.tilesource.AbstractOnlineLayerTileSource>()
            every {
                TileSourceFactory.getOnlineTileSource(any(), onlineLayerSettings)
            } returns mockTileSource

            // when
            val result = repository.prepareLayerFromSettings(onlineLayerSettings)

            // then
            assertTrue(result is LayerState.Layer)
            assertEquals(onlineLayerSettings, (result as LayerState.Layer).settings)
            assertEquals(onlineLayerSettings.getSourcesAsUri(), result.source)
        }

    @Test
    fun `prepareLayerFromSettings() should return LayerState Error when online tile source throws LayerException`() =
        runTest {
            // given some not supported online layer
            val onlineLayerSettings = LayerSettings.Builder.newInstance()
                .label("Invalid online layer")
                .addSource("https://unsupported.tiles.com/tiles/{z}/{x}/{y}.png")
                .build()
            val expectedException = LayerException.NotSupportedException(onlineLayerSettings)
            every {
                TileSourceFactory.getOnlineTileSource(any(), onlineLayerSettings)
            } throws expectedException

            // when
            val result = repository.prepareLayerFromSettings(onlineLayerSettings)

            // then
            assertTrue(result is LayerState.Error)
            assertEquals(expectedException, (result as LayerState.Error).error)
        }

    @Test
    fun `prepareLayerFromSettings() should return LayerState Error with InvalidOnlineLayerException when online tile source throws unexpected exception`() =
        runTest {
            // given
            val onlineLayerSettings = LayerSettings.Builder.newInstance()
                .label("Failing online layer")
                .addSource("https://fail.tiles.com/tiles/{z}/{x}/{y}.png")
                .build()
            every {
                TileSourceFactory.getOnlineTileSource(any(), onlineLayerSettings)
            } throws RuntimeException("Network error")

            // when
            val result = repository.prepareLayerFromSettings(onlineLayerSettings)

            // then
            assertTrue(result is LayerState.Error)
            assertTrue((result as LayerState.Error).error is LayerException.InvalidOnlineLayerException)
            assertEquals(onlineLayerSettings, result.error.layerSettings)
        }

    @Test
    fun `prepareLayerFromSettings() should return LayerState Layer for valid local layer`() =
        runTest {
            // given some local layer
            val localLayerSettings = LayerSettings.Builder.newInstance()
                .label("Nantes")
                .addSource("osmdroid/nantes.mbtiles")
                .build()
            val expectedUris = listOf(Uri.parse("file:///storage/emulated/0/osmdroid/nantes.mbtiles"))

            coEvery {
                localLayerDataSource.resolvesLocalLayerFromLayerSettings(
                    localLayerSettings,
                    any()
                )
            } returns expectedUris

            // when
            val result = repository.prepareLayerFromSettings(localLayerSettings, "/storage/emulated/0")

            // then
            assertTrue(result is LayerState.Layer)
            assertEquals(localLayerSettings, (result as LayerState.Layer).settings)
            assertEquals(expectedUris, result.source)
        }

    @Test
    fun `prepareLayerFromSettings() should return LayerState Error when local layer resolution throws LayerException`() =
        runTest {
            // given some not found local layer
            val localLayerSettings = LayerSettings.Builder.newInstance()
                .label("Missing layer")
                .addSource("osmdroid/missing.mbtiles")
                .build()
            val expectedException = LayerException.NotFoundException(localLayerSettings)

            coEvery {
                localLayerDataSource.resolvesLocalLayerFromLayerSettings(
                    localLayerSettings,
                    any()
                )
            } throws expectedException

            // when
            val result = repository.prepareLayerFromSettings(localLayerSettings)

            // then
            assertTrue(result is LayerState.Error)
            assertEquals(expectedException, (result as LayerState.Error).error)
        }

    @Test
    fun `prepareLayerFromSettings() should return LayerState Error with NotSupportedException when local layer resolution throws unexpected exception`() =
        runTest {
            // given some local layer with invalid source
            val localLayerSettings = LayerSettings.Builder.newInstance()
                .label("Nantes POIs")
                .addSource("osmdroid/nantes_pois.geojson")
                .build()

            coEvery {
                localLayerDataSource.resolvesLocalLayerFromLayerSettings(
                    localLayerSettings,
                    any()
                )
            } throws IllegalStateException("Unexpected error")

            // when
            val result = repository.prepareLayerFromSettings(localLayerSettings)

            // then
            assertTrue(result is LayerState.Error)
            assertTrue((result as LayerState.Error).error is LayerException.NotSupportedException)
            assertEquals(localLayerSettings, result.error.layerSettings)
        }

    @Test
    fun `prepareLayerFromSettings() should replace existing layer with the same settings in internal state`() =
        runTest {
            // given - first preparation of a local layer (success)
            val localLayerSettings = LayerSettings.Builder.newInstance()
                .label("Nantes")
                .addSource("osmdroid/nantes.mbtiles")
                .build()
            val firstUris = listOf(Uri.parse("file:///storage/emulated/0/osmdroid/nantes.mbtiles"))
            val secondUris = listOf(Uri.parse("file:///sdcard/osmdroid/nantes.mbtiles"))

            coEvery {
                localLayerDataSource.resolvesLocalLayerFromLayerSettings(
                    localLayerSettings,
                    "/storage/emulated/0"
                )
            } returns firstUris
            coEvery {
                localLayerDataSource.resolvesLocalLayerFromLayerSettings(
                    localLayerSettings,
                    "/sdcard"
                )
            } returns secondUris

            repository.prepareLayerFromSettings(localLayerSettings, "/storage/emulated/0")

            // when - second preparation of the same layer with a different path
            val result = repository.prepareLayerFromSettings(localLayerSettings, "/sdcard")

            // then - only one instance remains in internal state
            val allLayers = repository.getAllLayers()
            assertEquals(1, allLayers.size)
            assertTrue(result is LayerState.Layer)
            assertEquals(secondUris, (result as LayerState.Layer).source)
        }

    @Test
    fun `getAllLayers() should return empty list initially`() = runTest {
        // when
        val result = repository.getAllLayers()

        // then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAllLayers() should return all layers after prepareLayers`() = runTest {
        // given some layers
        val layer1 = LayerSettings.Builder.newInstance()
            .label("OSM")
            .addSource("https://a.tile.openstreetmap.org/{z}/{x}/{y}.png")
            .build()
        val layer2 = LayerSettings.Builder.newInstance()
            .label("Nantes")
            .addSource("osmdroid/nantes.mbtiles")
            .build()
        val expectedUris = listOf(Uri.parse("file:///storage/emulated/0/osmdroid/nantes.mbtiles"))

        coEvery {
            localLayerDataSource.resolvesLocalLayerFromLayerSettings(layer2, null)
        } returns expectedUris

        repository.prepareLayers(listOf(layer1, layer2))

        // when
        val result = repository.getAllLayers()

        // then
        assertEquals(2, result.size)
    }

    @Test
    fun `addLayerFromURI() should add LayerState Layer to internal state on success`() = runTest {
        // given some URI to parse and add as layer
        val uri = "file:///storage/emulated/0/osmdroid/nantes.mbtiles".toUri()
        val expectedLayer = LayerState.Layer(
            settings = LayerSettings.Builder.newInstance()
                .label("Nantes")
                .addSource("osmdroid/nantes.mbtiles")
                .build(),
            source = listOf(uri)
        )

        coEvery { localLayerDataSource.buildLocalLayerFromUri(uri) } returns expectedLayer

        // when
        val result = repository.addLayerFromURI(uri)

        // then
        assertTrue(result is LayerState.Layer)
        assertEquals(expectedLayer, result)

        val allLayers = repository.getAllLayers()
        assertTrue(allLayers.contains(expectedLayer))
    }

    @Test
    fun `addLayerFromURI() should not add to internal state when it returns LayerState Error`() =
        runTest {
            // given
            val uri = "file:///storage/emulated/0/osmdroid/invalid.xyz".toUri()
            val layerSettings = LayerSettings.Builder.newInstance()
                .label("Invalid")
                .addSource("osmdroid/invalid.xyz")
                .build()
            val error = LayerState.Error(LayerException.NotSupportedException(layerSettings))

            coEvery { localLayerDataSource.buildLocalLayerFromUri(uri) } returns error

            // when
            val result = repository.addLayerFromURI(uri)

            // then
            assertTrue(result is LayerState.Error)

            val allLayers = repository.getAllLayers()
            assertFalse(allLayers.any { it is LayerState.Error })
        }

    @Test
    fun `getSelectedLayers() should return empty list when no selection is stored`() = runTest {
        // given
        coEvery { selectedLayersLocalDataSource.getSelectedLayers() } returns emptySet()

        // when
        val result = repository.getSelectedLayers()

        // then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getSelectedLayers() should return SelectedLayer for each matching loaded layer`() = runTest {
        // given - prepare layers
        val uri = Uri.parse("file:///storage/emulated/0/osmdroid/nantes.mbtiles")
        val localLayerSettings = LayerSettings.Builder.newInstance()
            .label("Nantes")
            .addSource("osmdroid/nantes.mbtiles")
            .build()
        val expectedUris = listOf(uri)

        coEvery {
            localLayerDataSource.resolvesLocalLayerFromLayerSettings(localLayerSettings, null)
        } returns expectedUris

        repository.prepareLayers(listOf(localLayerSettings))

        // and selected layer is stored
        coEvery { selectedLayersLocalDataSource.getSelectedLayers() } returns setOf(uri)

        // when
        val result = repository.getSelectedLayers()

        // then
        assertEquals(1, result.size)
        assertEquals(localLayerSettings, result.first().settings)
        assertEquals(expectedUris, result.first().source)
    }

    @Test
    fun `getSelectedLayers() should ignore URIs that do not match any loaded layer`() = runTest {
        // given - no layers loaded
        val unknownUri = Uri.parse("file:///storage/emulated/0/osmdroid/unknown.mbtiles")
        coEvery { selectedLayersLocalDataSource.getSelectedLayers() } returns setOf(unknownUri)

        // when
        val result = repository.getSelectedLayers()

        // then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getSelectedLayers() should return only layers that match stored URIs`() = runTest {
        // given - prepare two layers
        val uri1 = Uri.parse("file:///storage/emulated/0/osmdroid/layer1.mbtiles")
        val uri2 = Uri.parse("file:///storage/emulated/0/osmdroid/layer2.mbtiles")

        val layerSettings1 = LayerSettings.Builder.newInstance()
            .label("Layer 1")
            .addSource("osmdroid/layer1.mbtiles")
            .build()
        val layerSettings2 = LayerSettings.Builder.newInstance()
            .label("Layer 2")
            .addSource("osmdroid/layer2.mbtiles")
            .build()

        coEvery {
            localLayerDataSource.resolvesLocalLayerFromLayerSettings(layerSettings1, null)
        } returns listOf(uri1)
        coEvery {
            localLayerDataSource.resolvesLocalLayerFromLayerSettings(layerSettings2, null)
        } returns listOf(uri2)

        repository.prepareLayers(listOf(layerSettings1, layerSettings2))

        // and only layer 1 is selected
        coEvery { selectedLayersLocalDataSource.getSelectedLayers() } returns setOf(uri1)

        // when
        val result = repository.getSelectedLayers()

        // then
        assertEquals(1, result.size)
        assertEquals(layerSettings1, result.first().settings)
    }

    @Test
    fun `setSelectedLayers() should delegate to data source with primary source of each selected layer`() =
        runTest {
            // given
            val uri1 = Uri.parse("file:///storage/emulated/0/osmdroid/layer1.mbtiles")
            val uri2 = Uri.parse("file:///storage/emulated/0/osmdroid/layer2.mbtiles")

            val selectedLayer1 = LayerState.SelectedLayer(
                settings = LayerSettings.Builder.newInstance()
                    .label("Layer 1")
                    .addSource("osmdroid/layer1.mbtiles")
                    .build(),
                source = listOf(uri1)
            )
            val selectedLayer2 = LayerState.SelectedLayer(
                settings = LayerSettings.Builder.newInstance()
                    .label("Layer 2")
                    .addSource("osmdroid/layer2.mbtiles")
                    .build(),
                source = listOf(uri2)
            )

            coEvery { selectedLayersLocalDataSource.setSelectedLayers(any()) } returns Unit

            // when
            repository.setSelectedLayers(listOf(selectedLayer1, selectedLayer2))

            // then
            coVerify {
                selectedLayersLocalDataSource.setSelectedLayers(setOf(uri1, uri2))
            }
        }

    @Test
    fun `setSelectedLayers() should pass empty set when given empty list`() = runTest {
        // given
        coEvery { selectedLayersLocalDataSource.setSelectedLayers(any()) } returns Unit

        // when
        repository.setSelectedLayers(emptyList())

        // then
        coVerify { selectedLayersLocalDataSource.setSelectedLayers(emptySet()) }
    }

    @Test
    fun `setSelectedLayers() should pass only primary source URIs`() = runTest {
        // given - a selected layer with multiple sources
        val primaryUri = Uri.parse("file:///storage/emulated/0/osmdroid/nantes.mbtiles")
        val secondaryUri = Uri.parse("file:///sdcard/osmdroid/nantes.mbtiles")

        val selectedLayer = LayerState.SelectedLayer(
            settings = LayerSettings.Builder.newInstance()
                .label("Nantes")
                .addSource("osmdroid/nantes.mbtiles")
                .build(),
            source = listOf(primaryUri, secondaryUri)
        )

        coEvery { selectedLayersLocalDataSource.setSelectedLayers(any()) } returns Unit

        // when
        repository.setSelectedLayers(listOf(selectedLayer))

        // then - only the first (primary) URI is passed
        coVerify { selectedLayersLocalDataSource.setSelectedLayers(setOf(primaryUri)) }
    }
}

