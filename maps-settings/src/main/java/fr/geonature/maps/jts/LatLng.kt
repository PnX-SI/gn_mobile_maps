package fr.geonature.maps.jts

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * An object representing a latitude/longitude pair. This is expressed as a pair of doubles
 * representing degrees latitude and degrees longitude.
 * Unless specified otherwise, this must conform to the [WGS84](http://www.unoosa.org/pdf/icg/2012/template/WGS_84.pdf)
 * standard.
 * Values must be within normalized ranges.
 *
 * @author S. Grimault
 */
@Parcelize
data class LatLng(
    val latitude: Double,
    val longitude: Double
) : Parcelable

