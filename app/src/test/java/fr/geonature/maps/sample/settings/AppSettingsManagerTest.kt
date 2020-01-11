package fr.geonature.maps.sample.settings

import android.app.Application
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import fr.geonature.maps.sample.FixtureHelper
import fr.geonature.maps.sample.settings.io.OnAppSettingsJsonReaderListenerImpl
import fr.geonature.maps.settings.LayerSettings
import fr.geonature.maps.settings.MapSettings
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.doReturn
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.robolectric.RobolectricTestRunner
import java.io.File

/**
 * Unit tests about [AppSettingsManager].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class AppSettingsManagerTest {
    private lateinit var appSettingsManager: AppSettingsManager<AppSettings>

    @Before
    fun setUp() {
        val application = getApplicationContext<Application>()
        appSettingsManager = Mockito.spy(
            AppSettingsManager(
                application,
                OnAppSettingsJsonReaderListenerImpl()
            )
        )
    }

    @Test
    fun testGetAppSettingsFilename() {
        // when getting the app settings filename
        val appSettingsFilename = appSettingsManager.getAppSettingsFilename()

        // then
        assertNotNull(appSettingsFilename)
        assertEquals(
            "settings_sample.json",
            appSettingsFilename
        )
    }

    @Test
    fun testReadUndefinedAppSettings() {
        // when reading undefined AppSettings
        var noSuchAppSettings = runBlocking { appSettingsManager.loadAppSettings() }

        // then
        assertNull(noSuchAppSettings)

        // given non existing app settings JSON file
        doReturn(
            File(
                "/mnt/sdcard",
                "no_such_file.json"
            )
        )
            .`when`(appSettingsManager)
            .getAppSettingsAsFile()

        // when reading this file
        noSuchAppSettings = runBlocking { appSettingsManager.loadAppSettings() }

        // then
        assertNull(noSuchAppSettings)
    }

    @Test
    fun testReadAppSettings() {
        // given app settings to read
        doReturn(FixtureHelper.getFixtureAsFile("settings_sample.json"))
            .`when`(appSettingsManager)
            .getAppSettingsAsFile()

        // when reading this file
        val appSettings = runBlocking { appSettingsManager.loadAppSettings() }

        // then
        assertNotNull(appSettings)
        assertEquals(
            AppSettings(
                MapSettings(
                    arrayListOf(
                        LayerSettings(
                            "Nantes",
                            "nantes.mbtiles"
                        )
                    ),
                    null,
                    showScale = true,
                    showCompass = true,
                    zoom = 10.0,
                    minZoomLevel = 8.0,
                    maxZoomLevel = 19.0,
                    minZoomEditing = 12.0,
                    maxBounds = BoundingBox.fromGeoPoints(
                        arrayListOf(
                            GeoPoint(
                                47.253369,
                                -1.605721
                            ),
                            GeoPoint(
                                47.173845,
                                -1.482811
                            )
                        )
                    ),
                    center = GeoPoint(
                        47.225827,
                        -1.554470
                    )
                )
            ),
            appSettings
        )
    }
}
