package fr.geonature.maps.jts.geojson.io

import fr.geonature.maps.FixtureHelper.getFixture
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
 * Unit tests about [WKTReader].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class WKTReaderTest {

    @RelaxedMockK
    private lateinit var onWKTReaderListener: WKTReader.OnWKTReaderListener

    @Before
    fun setUp() {
        init(this)
    }

    @Test
    fun testReadFeaturesThroughCallback() {
        // given a WKT file to read
        val wkt = getFixture("features.wkt")

        // when parsing this file as WKT
        WKTReader().readFeatures(
            wkt.reader(),
            onWKTReaderListener
        )

        val capturingSlotFeatureCollection = slot<FeatureCollection>()

        // then
        verify(inverse = true) { onWKTReaderListener.onError(any()) }
        verify(exactly = 4) { onWKTReaderListener.onProgress(any(), any()) }
        verify { onWKTReaderListener.onFinish(capture(capturingSlotFeatureCollection)) }

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

    @Test
    fun testReadFeatures() {
        // given a WKT file to read
        val wkt = getFixture("features.wkt")

        // when parsing this file as WKT
        val features = WKTReader().readFeatures(wkt.reader())

        // then
        assertNotNull(features)
        assertEquals(
            4,
            features.size
        )

        assertEquals(
            "1",
            features[0].id
        )
        assertNotNull(features[0].geometry)
        assertEquals(
            "Point",
            features[0].geometry.geometryType
        )

        assertEquals(
            "69",
            features[1].id
        )
        assertNotNull(features[1].geometry)
        assertEquals(
            "Polygon",
            features[1].geometry.geometryType
        )

        assertEquals(
            "19",
            features[2].id
        )
        assertNotNull(features[2].geometry)
        assertEquals(
            "Polygon",
            features[2].geometry.geometryType
        )

        assertEquals(
            "146",
            features[3].id
        )
        assertNotNull(features[3].geometry)
        assertEquals(
            "Polygon",
            features[3].geometry.geometryType
        )
    }

    @Test
    fun testReadFeatureCollection() {
        // given a WKT file to read
        val wkt = getFixture("features.wkt")

        // when parsing this file as WKT
        val featureCollection = WKTReader().readFeatureCollection(wkt.reader())

        // then
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
