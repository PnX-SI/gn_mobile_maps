package fr.geonature.maps.jts.geojson

import android.os.Parcel
import fr.geonature.maps.jts.geojson.JTSTestHelper.createPoint
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.locationtech.jts.geom.GeometryFactory
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [FeatureCollection].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class FeatureCollectionTest {

    private lateinit var gf: GeometryFactory

    @Before
    fun setUp() {
        gf = GeometryFactory()
    }

    @Test
    fun testType() {
        // given a FeatureCollection
        val featureCollection = FeatureCollection()

        // then
        assertEquals(
            "FeatureCollection",
            featureCollection.type
        )
    }

    @Test
    fun testParcelable() {
        // given a FeatureCollection
        val featureCollection = FeatureCollection().apply {
            addFeature(Feature(
                "1234",
                createPoint(
                    gf,
                    47.225782,
                    -1.554476
                )
            ).apply {
                properties.putString(
                    "name",
                    "feature1"
                )
            })
            addFeature(
                Feature(
                    "1235",
                    createPoint(
                        gf,
                        47.226468,
                        -1.554996
                    )
                )
            )
        }

        // when we obtain a Parcel object to write the FeatureCollection instance to it
        val parcel = Parcel.obtain()
        featureCollection.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            featureCollection,
            FeatureCollection.createFromParcel(parcel)
        )
    }
}