package fr.geonature.maps.jts.geojson.io

import fr.geonature.maps.FixtureHelper.getFixture
import fr.geonature.maps.MockitoKotlinHelper.any
import fr.geonature.maps.MockitoKotlinHelper.capture
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
import java.io.StringReader

/**
 * Unit tests about [WKTReader].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class WKTReaderTest {

    @Captor
    private lateinit var featureCollectionArgumentCaptor: ArgumentCaptor<FeatureCollection>

    @Mock
    private lateinit var onWKTReaderListener: WKTReader.OnWKTReaderListener

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun testReadFeaturesThroughCallback() {
        // given a WKT file to read
        val wkt = getFixture("features.wkt")

        // when parsing this file as WKT
        WKTReader().readFeatures(StringReader(wkt),
                                 onWKTReaderListener)

        // then
        verify(onWKTReaderListener,
               never()).onError(any(Throwable::class.java))
        verify(onWKTReaderListener,
               times(4)).onProgress(anyInt(),
                                    any(Feature::class.java))
        verify(onWKTReaderListener).onFinish(capture(featureCollectionArgumentCaptor))

        val featureCollection = featureCollectionArgumentCaptor.value
        assertNotNull(featureCollection)
        assertEquals(4,
                     featureCollection.getFeatures().size)
        assertNotNull(featureCollection.getFeature("1"))
        assertNotNull(featureCollection.getFeature("69"))
        assertNotNull(featureCollection.getFeature("19"))
        assertNotNull(featureCollection.getFeature("146"))
    }

    @Test
    fun testReadFeatures() {
        // given a WKT file to read
        val wkt = getFixture("features.wkt")

        // when parsing this file as WKT
        val features = WKTReader().readFeatures(StringReader(wkt))

        // then
        assertNotNull(features)
        assertEquals(4,
                     features.size)

        assertEquals("1",
                     features[0].id)
        assertNotNull(features[0].geometry)
        assertEquals("Point",
                     features[0].geometry.geometryType)

        assertEquals("69",
                     features[1].id)
        assertNotNull(features[1].geometry)
        assertEquals("Polygon",
                     features[1].geometry.geometryType)

        assertEquals("19",
                     features[2].id)
        assertNotNull(features[2].geometry)
        assertEquals("Polygon",
                     features[2].geometry.geometryType)

        assertEquals("146",
                     features[3].id)
        assertNotNull(features[3].geometry)
        assertEquals("Polygon",
                     features[3].geometry.geometryType)
    }

    @Test
    fun testReadFeatureCollection() {
        // given a WKT file to read
        val wkt = getFixture("features.wkt")

        // when parsing this file as WKT
        val featureCollection = WKTReader().readFeatureCollection(StringReader(wkt))

        // then
        assertNotNull(featureCollection)
        assertEquals(4,
                     featureCollection.getFeatures().size)
        assertNotNull(featureCollection.getFeature("1"))
        assertNotNull(featureCollection.getFeature("69"))
        assertNotNull(featureCollection.getFeature("19"))
        assertNotNull(featureCollection.getFeature("146"))
    }
}