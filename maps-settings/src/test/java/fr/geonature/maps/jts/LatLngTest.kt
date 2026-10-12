package fr.geonature.maps.jts

import android.os.Parcel
import kotlinx.parcelize.parcelableCreator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [LatLng].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class LatLngTest {

    @Test
    fun `should expose latitude and longitude`() {
        val latLng = LatLng(
            47.225827,
            -1.55447
        )
        assertEquals(
            47.225827,
            latLng.latitude,
            0.0
        )
        assertEquals(
            -1.55447,
            latLng.longitude,
            0.0
        )
    }

    @Test
    fun `should be equal to another instance with same coordinates`() {
        assertEquals(
            LatLng(
                1.0,
                2.0
            ),
            LatLng(
                1.0,
                2.0
            )
        )
        assertEquals(
            LatLng(
                1.0,
                2.0
            ).hashCode(),
            LatLng(
                1.0,
                2.0
            ).hashCode()
        )
        assertNotEquals(
            LatLng(
                1.0,
                2.0
            ),
            LatLng(
                2.0,
                1.0
            )
        )
    }

    @Test
    fun `should be parcelable`() {
        val latLng = LatLng(
            47.225827,
            -1.55447
        )
        val parcel = Parcel.obtain()
        latLng.writeToParcel(
            parcel,
            0
        )
        parcel.setDataPosition(0)
        assertEquals(
            latLng,
            parcelableCreator<LatLng>().createFromParcel(parcel)
        )
        parcel.recycle()
    }
}
