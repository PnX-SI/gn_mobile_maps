package fr.geonature.maps.settings

import android.os.Parcel
import android.os.Parcelable

/**
 * Default settings for a given tile source.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class TileSourceSettings(var name: String,
                              var label: String) : Parcelable {

    private constructor(source: Parcel) : this(
        source.readString() ?: "",
        source.readString() ?: "")

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?,
                               flags: Int) {
        dest?.writeString(name)
        dest?.writeString(label)
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<TileSourceSettings> =
            object : Parcelable.Creator<TileSourceSettings> {
                override fun createFromParcel(source: Parcel): TileSourceSettings {
                    return TileSourceSettings(source)
                }

                override fun newArray(size: Int): Array<TileSourceSettings?> {
                    return arrayOfNulls(size)
                }
            }
    }
}