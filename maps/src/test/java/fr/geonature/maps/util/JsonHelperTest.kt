package fr.geonature.maps.util

import android.util.JsonReader
import fr.geonature.maps.FixtureHelper.getFixture
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.StringReader

/**
 * Unit test for `JsonHelper`.
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class JsonHelperTest {

    @Test
    fun `should read object as Map from JSON simple point with properties`() {
        // given a JSON Feature as simple Polygon
        val json = getFixture("feature_point_id.json")
        val jsonReader = JsonReader(StringReader(json))
        val asObject = jsonReader.readObject()

        assertEquals(
            hashMapOf(
                "id" to "id1",
                "type" to "Feature",
                "geometry" to hashMapOf(
                    "type" to "Point",
                    "coordinates" to listOf(
                        -1.5545135,
                        47.2256258
                    )
                ),
                "properties" to hashMapOf(
                    "id" to "id2",
                    "name" to "Ile de Versailles",
                    "year" to 1831L,
                    "double_attribute" to 3.14,
                    "boolean_attribute_false" to false,
                    "boolean_attribute_true" to true
                )
            ),
            asObject
        )
    }

    @Test
    fun `should read object as Map from JSON simple polygon`() {
        // given a JSON Feature as simple Polygon
        val json = getFixture("feature_polygon_simple.json")
        val jsonReader = JsonReader(StringReader(json))
        val asObject = jsonReader.readObject()

        assertEquals(
            hashMapOf(
                "id" to "id1",
                "type" to "Feature",
                "geometry" to hashMapOf(
                    "type" to "Polygon",
                    "coordinates" to listOf<Any>(
                        listOf<Any>(
                            listOf(
                                -1.55443,
                                47.226219
                            ),
                            listOf(
                                -1.554261,
                                47.226237
                            ),
                            listOf(
                                -1.554245,
                                47.226122
                            ),
                            listOf(
                                -1.554411,
                                47.226106
                            ),
                            listOf(
                                -1.55443,
                                47.226219
                            )
                        )
                    )
                ),
                "properties" to hashMapOf<String, Any?>()
            ),
            asObject
        )
    }
}