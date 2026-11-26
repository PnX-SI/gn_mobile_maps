package fr.geonature.maps.maplibre.ui.overlay

import android.location.Location
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [MyLocationOverlay].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class MyLocationOverlayTest {

    @Test
    fun `should convert accuracy in meters to pixels`() {
        // at the equator and zoom 0, a pixel is about 78271.5 meters wide
        assertEquals(
            1.0,
            MyLocationOverlay.metersToPixels(
                78271.516f,
                0.0,
                0.0
            ),
            1e-3
        )
        assertEquals(
            2.0,
            MyLocationOverlay.metersToPixels(
                78271.516f,
                0.0,
                1.0
            ),
            1e-3
        )
    }

    @Test
    fun `should give a larger radius for a lower accuracy`() {
        val accurate = MyLocationOverlay.metersToPixels(
            5f,
            45.0,
            16.0
        )
        val inaccurate = MyLocationOverlay.metersToPixels(
            50f,
            45.0,
            16.0
        )

        assertTrue(inaccurate > accurate)
    }

    @Test
    fun `should serialize location as GeoJSON feature with its bearing`() {
        val location = Location("test").apply {
            latitude = 45.5
            longitude = 5.25
        }

        assertEquals(
            """{"type":"Feature","properties":{"bearing":90.0},"geometry":{"type":"Point","coordinates":[5.25000000,45.50000000]}}""",
            MyLocationOverlay.toFeature(
                location,
                90f
            )
        )
    }

    @Test
    fun `should use the back of the device as heading when held upright`() {
        // upright device whose back faces east: screen normal (device Z) points west
        val matrix = floatArrayOf(
            0f, 0f, -1f,
            0f, 1f, 0f,
            1f, 0f, 0f
        )

        assertEquals(
            90f,
            MyLocationOverlay.headingFromRotationMatrix(matrix),
            0.01f
        )
    }

    @Test
    fun `should use the top of the device as heading when held flat`() {
        // flat device whose top edge points east
        val matrix = floatArrayOf(
            0f, 1f, 0f,
            -1f, 0f, 0f,
            0f, 0f, 1f
        )

        assertEquals(
            90f,
            MyLocationOverlay.headingFromRotationMatrix(matrix),
            0.01f
        )
    }

    @Test
    fun `should compute angular distances across north`() {
        assertEquals(
            20f,
            MyLocationOverlay.angularDistance(
                350f,
                10f
            ),
            0f
        )
        assertEquals(
            350f,
            MyLocationOverlay.normalize(-10f),
            0f
        )
    }
}
