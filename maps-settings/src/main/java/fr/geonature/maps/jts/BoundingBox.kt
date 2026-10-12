package fr.geonature.maps.jts

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * A bounding box represents an immutable set of two latitude and two longitude coordinates.
 *
 * @author S. Grimault
 * @see LatLng
 */
@Parcelize
data class BoundingBox(
    val latNorth: Double,
    val lonEast: Double,
    val latSouth: Double,
    val lonWest: Double
) : Parcelable {

    companion object {

        /**
         * Builds the smallest [BoundingBox] containing all given [LatLng].
         */
        fun fromLatLng(vararg latLng: LatLng): BoundingBox {
            require(latLng.isNotEmpty()) { "At least one coordinate is required" }

            return BoundingBox(
                latNorth = latLng.maxOf { it.latitude },
                lonEast = latLng.maxOf { it.longitude },
                latSouth = latLng.minOf { it.latitude },
                lonWest = latLng.minOf { it.longitude })
        }
    }
}
