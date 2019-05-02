package fr.geonature.maps.jts.geojson.io

import fr.geonature.maps.FixtureHelper.getFixtureAsFile
import fr.geonature.maps.MockitoKotlinHelper.any
import fr.geonature.maps.MockitoKotlinHelper.capture
import fr.geonature.maps.MockitoKotlinHelper.eq
import fr.geonature.maps.jts.geojson.Feature
import fr.geonature.maps.jts.geojson.FeatureCollection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests about [WKTFileReader].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class WKTFileReaderTest {

    @Captor
    private lateinit var featureCollectionArgumentCaptor: ArgumentCaptor<FeatureCollection>

    @Mock
    private lateinit var onWKTFileReaderListener: WKTFileReader.OnWKTFileReaderListener

    @Before
    @Throws(Exception::class)
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun testReadFeatures() {
        // given a WKT file to read
        val wkt = getFixtureAsFile("features.wkt")

        // when parsing this file as WKT
        WKTFileReader().readFeatures(
            wkt,
            onWKTFileReaderListener
        )

        // then
        verify(onWKTFileReaderListener).onStart(3)
        verify(
            onWKTFileReaderListener,
            never()
        ).onError(any(Throwable::class.java))
        verify(
            onWKTFileReaderListener,
            times(3)
        ).onProgress(
            anyInt(),
            eq(3),
            any(Feature::class.java)
        )
        verify(onWKTFileReaderListener).onFinish(capture(featureCollectionArgumentCaptor))

        val featureCollection = featureCollectionArgumentCaptor.value
        assertNotNull(featureCollection)
        assertEquals(
            3,
            featureCollection.getFeatures().size
        )
        assertNotNull(featureCollection.getFeature("69"))
        assertNotNull(featureCollection.getFeature("19"))
        assertNotNull(featureCollection.getFeature("146"))
    }
}