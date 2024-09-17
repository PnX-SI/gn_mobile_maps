package fr.geonature.maps.jts.geojson.io

import fr.geonature.maps.FixtureHelper.getFixtureAsFile
import fr.geonature.maps.jts.geojson.FeatureCollection
import io.mockk.MockKAnnotations.init
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [WKTFileReader].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class WKTFileReaderTest {

    @RelaxedMockK
    private lateinit var onWKTFileReaderListener: WKTFileReader.OnWKTFileReaderListener

    @Before
    fun setUp() {
        init(this)
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

        val capturingSlotFeatureCollection = slot<FeatureCollection>()

        // then
        verify { onWKTFileReaderListener.onStart(4) }
        verify(inverse = true) { onWKTFileReaderListener.onError(any()) }
        verify(exactly = 4) {
            onWKTFileReaderListener.onProgress(
                any(),
                4,
                any()
            )
        }
        verify { onWKTFileReaderListener.onFinish(capture(capturingSlotFeatureCollection)) }

        val featureCollection = capturingSlotFeatureCollection.captured
        assertNotNull(featureCollection)
        assertEquals(
            4,
            featureCollection.getFeatures().size
        )
        assertNotNull(featureCollection.getFeature("1"))
        assertNotNull(featureCollection.getFeature("69"))
        assertNotNull(featureCollection.getFeature("19"))
        assertNotNull(featureCollection.getFeature("146"))
    }
}
