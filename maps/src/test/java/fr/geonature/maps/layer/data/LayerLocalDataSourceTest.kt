package fr.geonature.maps.layer.data

import android.app.Application
import android.net.Uri
import android.os.Environment
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import fr.geonature.maps.CoroutineTestRule
import fr.geonature.maps.layer.domain.LayerState
import fr.geonature.maps.layer.error.LayerException
import fr.geonature.maps.settings.LayerSettings
import fr.geonature.mountpoint.util.FileUtils.getExternalStorageDirectory
import fr.geonature.mountpoint.util.MountPointUtils
import fr.geonature.mountpoint.util.getFile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowEnvironment
import java.io.File

/**
 * Unit tests about [ILayerLocalDataSource].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
internal class LayerLocalDataSourceTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var application: Application
    private lateinit var localLayerDataSource: ILayerLocalDataSource

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        ShadowEnvironment.setExternalStorageState(
            File("/"),
            Environment.MEDIA_MOUNTED
        )
        localLayerDataSource = LayerLocalDataSourceImpl(
            application,
            coroutineTestRule.testDispatcher
        )
    }

    @Test
    fun `should resolves local layer from layer settings from external storage`() = runTest {
        // given some existing valid file from external storage
        val externalRootPath = getExternalStorageDirectory(application).apply {
            getFile(
                "Downloads",
                "osmdroid"
            ).mkdirs()
        }
        val expectedLocalFile = externalRootPath.getFile(
            "Downloads",
            "osmdroid",
            "nantes_pois.geojson"
        )
            .apply {
                createNewFile()
                setReadable(true)
            }

        // and some layer settings
        val layerSettings = LayerSettings.Builder()
            .label("Nantes POIs")
            .addSource("osmdroid/nantes_pois.geojson")
            .build()

        // when trying to resolves local layer
        val uris = localLayerDataSource.resolvesLocalLayerFromLayerSettings(
            layerSettings,
            externalRootPath.absolutePath
        )

        // then
        assertEquals(
            listOf(Uri.parse("file://${expectedLocalFile.absolutePath}")),
            uris
        )
    }

    @Test
    fun `should resolves local layer from layer settings from internal storage as fallback`() =
        runTest {
            // given some existing valid file from internal storage
            val externalRootPath = getExternalStorageDirectory(application).apply {
                getFile(
                    "Downloads",
                    "osmdroid"
                ).mkdirs()
            }
            val internalRootPath = MountPointUtils.getInternalStorage(application).mountPath.apply {
                getFile(
                    "Downloads",
                    "osmdroid"
                ).mkdirs()
            }
            val expectedLocalFile = internalRootPath.getFile(
                "Downloads",
                "osmdroid",
                "nantes_pois.geojson"
            )
                .apply {
                    createNewFile()
                    setReadable(true)
                }

            // and some layer settings
            val layerSettings = LayerSettings.Builder()
                .label("Nantes POIs")
                .addSource("osmdroid/nantes_pois.geojson")
                .build()

            // when trying to resolves local layer using external storage
            val uris = localLayerDataSource.resolvesLocalLayerFromLayerSettings(
                layerSettings,
                externalRootPath.absolutePath
            )

            // then
            assertEquals(
                listOf(Uri.parse("file://${expectedLocalFile.absolutePath}")),
                uris
            )
        }

    @Test(expected = LayerException.NotSupportedException::class)
    fun `should throw NotSupportedException if trying to resolve local layer from unknown source`() =
        runTest {
            // when trying to build the corresponding layer from invalid URI
            localLayerDataSource.resolvesLocalLayerFromLayerSettings(
                LayerSettings.Builder()
                    .label("some layer")
                    .addSource("unknown.source")
                    .build()
            )
        }

    @Test(expected = LayerException.InvalidFileLayerException::class)
    fun `should throw InvalidFileLayerException if trying to resolve local layer from online source`() =
        runTest {
            // when trying to build the corresponding layer from invalid URI
            localLayerDataSource.resolvesLocalLayerFromLayerSettings(
                LayerSettings.Builder()
                    .label("OSM")
                    .addSource("https://a.tile.openstreetmap.org")
                    .build()
            )
        }

    @Test(expected = LayerException.NotFoundException::class)
    fun `should throw NotFoundException if trying to resolve layer settings with no local file found locally`() =
        runTest {
            // given no local file found from from storage
            val externalRootPath = getExternalStorageDirectory(application).apply {
                getFile(
                    "Downloads",
                    "osmdroid"
                ).mkdirs()
            }
            MountPointUtils.getInternalStorage(application).mountPath.apply {
                getFile(
                    "Downloads",
                    "osmdroid"
                ).mkdirs()
            }

            // when trying to resolves local layer using external storage
            localLayerDataSource.resolvesLocalLayerFromLayerSettings(
                LayerSettings.Builder()
                    .label("Nantes POIs")
                    .addSource("osmdroid/nantes_pois.geojson")
                    .build(),
                externalRootPath.absolutePath
            )
        }

    @Test
    fun `should create layer settings from valid content URI`() = runTest {
        // given some existing valid file from external storage
        val externalRootPath = getExternalStorageDirectory(application).getFile("osmdroid")
            .apply { mkdirs() }
        val expectedLocalFile = externalRootPath.getFile(
            "osmdroid",
            "nantes_pois.geojson"
        )
            .apply {
                createNewFile()
                setReadable(true)
            }

        // when trying to build the corresponding layer from URI
        val layerFromUri =
            localLayerDataSource.buildLocalLayerFromUri(Uri.parse("content://com.android.externalstorage.documents/document/${externalRootPath.name}%3Aosmdroid%2Fnantes_pois.geojson"))

        // then
        assertEquals(
            LayerState.Layer(
                LayerSettings.Builder()
                    .label("Nantes pois")
                    .addSource(
                        expectedLocalFile.toUri()
                            .toString()
                    )
                    .build(),
                listOf(expectedLocalFile.toUri())
            ),
            layerFromUri
        )
    }

    @Test
    fun `should create layer settings from valid file URI`() = runTest {
        // given some existing valid file from external storage
        val externalRootPath = getExternalStorageDirectory(application).apply {
            getFile(
                "osmdroid"
            ).mkdirs()

        }
        val expectedLocalFile = externalRootPath.getFile(
            "osmdroid",
            "nantes_pois.geojson"
        )
            .apply {
                createNewFile()
                setReadable(true)
            }

        // when trying to build the corresponding layer from URI
        val layerFromUri =
            localLayerDataSource.buildLocalLayerFromUri(Uri.parse("file://${expectedLocalFile.absolutePath}"))

        // then
        assertEquals(
            LayerState.Layer(
                LayerSettings.Builder()
                    .label("Nantes pois")
                    .addSource(
                        expectedLocalFile.toUri()
                            .toString()
                    )
                    .build(),
                listOf(expectedLocalFile.toUri())
            ),
            layerFromUri
        )
    }

    fun `should get InvalidFileLayerException if URI scheme is not specified`() = runTest {
        // when trying to build the corresponding layer from invalid URI
        assertEquals(
            LayerState.Error(
                LayerException.InvalidFileLayerException(
                    LayerSettings(
                        label = "Nantes pois",
                        source = listOf("osmdroid/nantes_pois.geojson")
                    )
                )
            ),
            localLayerDataSource.buildLocalLayerFromUri(Uri.parse("osmdroid/nantes_pois.geojson"))
        )
    }

    fun `should get InvalidFileLayerException if URI is an URL`() = runTest {
        // when trying to build the corresponding layer from invalid URI
        assertEquals(
            LayerState.Error(
                LayerException.InvalidFileLayerException(
                    LayerSettings(
                        label = "Openstreetmap",
                        source = listOf("https://a.tile.openstreetmap.org")
                    )
                )
            ),
            localLayerDataSource.buildLocalLayerFromUri(Uri.parse("https://a.tile.openstreetmap.org"))
        )
    }

    fun `should get NotSupportedException if local file is not valid or not supported`() = runTest {
        // given some existing valid file from external storage
        val externalRootPath = getExternalStorageDirectory(application).apply {
            getFile(
                "osmdroid"
            ).mkdirs()

        }
        val expectedLocalFile = externalRootPath.getFile(
            "osmdroid",
            "nantes_pois.xml"
        )
            .apply {
                createNewFile()
                setReadable(true)
            }

        // when trying to build the corresponding layer from URI
        assertEquals(
            LayerState.Error(
                LayerException.NotSupportedException(
                    LayerSettings(
                        label = "Nantes pois",
                        source = listOf("file://${expectedLocalFile.absolutePath}")
                    )
                )
            ),
            localLayerDataSource.buildLocalLayerFromUri(Uri.parse("file://${expectedLocalFile.absolutePath}"))
        )
    }

    fun `should get NotFoundException if local file was not found`() = runTest {
        // given some non existing file from external storage
        val externalRootPath = getExternalStorageDirectory(application).apply {
            getFile(
                "osmdroid"
            ).mkdirs()
        }

        // when trying to build the corresponding layer from URI
        assertEquals(
            LayerState.Error(
                LayerException.NotFoundException(
                    LayerSettings(
                        label = "No such file",
                        source = listOf(
                            "file://${
                                externalRootPath.getFile(
                                    "osmdroid",
                                    "no_such_file.json"
                                )
                            }"
                        )
                    )
                )
            ),
            localLayerDataSource.buildLocalLayerFromUri(
                Uri.parse(
                    "file://${
                        externalRootPath.getFile(
                            "osmdroid",
                            "no_such_file.json"
                        )
                    }"
                )
            )
        )
    }
}