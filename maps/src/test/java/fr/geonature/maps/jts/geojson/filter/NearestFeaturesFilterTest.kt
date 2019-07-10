package fr.geonature.maps.jts.geojson.filter

import fr.geonature.maps.jts.geojson.Feature
import fr.geonature.maps.jts.geojson.FeatureCollection
import fr.geonature.maps.jts.geojson.JTSTestHelper.createPoint
import fr.geonature.maps.jts.geojson.filter.NearestFeaturesFilter.Companion.getFilteredFeatures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.locationtech.jts.geom.GeometryFactory
import org.osmdroid.util.GeoPoint
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [NearestFeaturesFilter].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class NearestFeaturesFilterTest {

    private lateinit var gf: GeometryFactory

    @Before
    fun setUp() {
        gf = GeometryFactory()
    }

    @Test
    fun testNearestFeaturesFromFeatures() {
        // given a GeoPoint
        val geoPoint = GeoPoint(
            47.225782,
            -1.554476
        )
        // and a list of Features to check
        val features = listOf(
            Feature(
                "id1",
                createPoint(
                    gf,
                    47.226468,
                    -1.554996
                )
            ),
            Feature(
                "id2",
                createPoint(
                    gf,
                    47.226126,
                    -1.554381
                )
            )
        )

        // when applying filter
        val filteredFeatures = getFilteredFeatures(
            geoPoint,
            45.0,
            features
        )

        // then
        assertNotNull(filteredFeatures)
        assertEquals(
            1,
            filteredFeatures.size
        )
        assertEquals(
            features[1],
            filteredFeatures[0]
        )
    }

    @Test
    fun testNearestFeaturesFromFeatureCollection() {
        // given a GeoPoint
        val geoPoint = GeoPoint(
            47.225782,
            -1.554476
        )

        // and a FeatureCollection to check
        val featureCollection = FeatureCollection().apply {
            addFeature(
                Feature(
                    "id1",
                    createPoint(
                        gf,
                        47.226468,
                        -1.554996
                    )
                )
            )
            addFeature(
                Feature(
                    "id2",
                    createPoint(
                        gf,
                        47.226126,
                        -1.554381
                    )
                )
            )
        }

        // when applying filter
        val filteredFeatures = getFilteredFeatures(
            geoPoint,
            45.0,
            featureCollection
        )

        // then
        assertNotNull(filteredFeatures)
        assertEquals(
            1,
            filteredFeatures.size
        )
        assertEquals(
            featureCollection.getFeature("id2"),
            filteredFeatures[0]
        )
    }
}