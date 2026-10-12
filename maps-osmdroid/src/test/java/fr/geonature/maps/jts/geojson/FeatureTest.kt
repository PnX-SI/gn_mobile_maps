package fr.geonature.maps.jts.geojson

import android.os.Parcel
import fr.geonature.maps.jts.geojson.JTSTestHelper.createPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.locationtech.jts.geom.GeometryFactory
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [Feature].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class FeatureTest {

    private lateinit var gf: GeometryFactory

    @Before
    fun setUp() {
        gf = GeometryFactory()
    }

    @Test
    fun `should have the right type`() {
        // given a Feature
        val feature = Feature(
            "1234",
            createPoint(
                gf,
                47.225782,
                -1.554476
            )
        ).apply {
            properties["name"] = "feature1"
        }

        // then
        assertEquals(
            "Feature",
            feature.type
        )
    }

    @Test
    fun `should be the same`() {
        assertEquals(Feature(
            "1234",
            createPoint(
                gf,
                47.225782,
                -1.554476
            )
        ).apply {
            properties["name"] = "feature1"
        },
            Feature(
                "1234",
                createPoint(
                    gf,
                    47.225782,
                    -1.554476
                )
            ).apply {
                properties["name"] = "feature1"
            })

        assertNotEquals(Feature(
            "1234",
            createPoint(
                gf,
                47.225782,
                -1.554476
            )
        ).apply {
            properties["name"] = "feature1"
        },
            Feature(
                "1234",
                createPoint(
                    gf,
                    47.225782,
                    -1.554476
                )
            ).apply {
                properties["name"] = "feature1"
                properties["some_boolean_attribute"] = true
            })

        assertNotEquals(Feature(
            "1234",
            createPoint(
                gf,
                47.225782,
                -1.554476
            )
        ),
            Feature(
                "1234",
                createPoint(
                    gf,
                    47.225782,
                    -1.554476
                )
            ).apply {
                properties["name"] = "feature1"
            })
    }

    @Test
    fun `should obtain feature instance from parcelable`() {
        // given a Feature
        val feature = Feature(
            "1234",
            createPoint(
                gf,
                47.225782,
                -1.554476
            ),
            hashMapOf("name" to "feature1")
        )

        // when we obtain a Parcel object to write the Feature instance to it
        val parcel = Parcel.obtain()
        feature.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            feature,
            Feature.createFromParcel(parcel)
        )
    }
}
