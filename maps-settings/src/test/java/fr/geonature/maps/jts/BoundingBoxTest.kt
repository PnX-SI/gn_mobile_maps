package fr.geonature.maps.jts

import android.os.Parcel
import kotlinx.parcelize.parcelableCreator
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [BoundingBox].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class BoundingBoxTest {

    @Test
    fun `should build bounding box from a list of coordinates`() {
        val boundingBox = BoundingBox.fromLatLng(
            LatLng(
                47.253369,
                -1.605721
            ),
            LatLng(
                47.173845,
                -1.482811
            ),
            LatLng(
                47.2,
                -1.5
            )
        )
        assertEquals(
            BoundingBox(
                latNorth = 47.253369,
                lonEast = -1.482811,
                latSouth = 47.173845,
                lonWest = -1.605721
            ),
            boundingBox
        )
    }

    @Test
    fun `should build bounding box from a single coordinate`() {
        val latLng = LatLng(
            47.0,
            -1.0
        )
        assertEquals(
            BoundingBox(
                latNorth = 47.0,
                lonEast = -1.0,
                latSouth = 47.0,
                lonWest = -1.0
            ),
            BoundingBox.fromLatLng(latLng)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should fail to build bounding box from an empty list`() {
        BoundingBox.fromLatLng()
    }

    @Test
    fun `should be parcelable`() {
        val boundingBox = BoundingBox(
            latNorth = 47.253369,
            lonEast = -1.482811,
            latSouth = 47.173845,
            lonWest = -1.605721
        )
        val parcel = Parcel.obtain()
        boundingBox.writeToParcel(
            parcel,
            0
        )
        parcel.setDataPosition(0)
        assertEquals(
            boundingBox,
            parcelableCreator<BoundingBox>().createFromParcel(parcel)
        )
        parcel.recycle()
    }
}
