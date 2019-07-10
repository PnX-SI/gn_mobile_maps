package fr.geonature.maps.jts.geojson

import android.os.Parcel
import android.os.Parcelable
import java.util.ArrayList
import java.util.HashMap

/**
 * Describes a [FeatureCollection] object as a `List` of [Feature].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class FeatureCollection : AbstractGeoJson, Parcelable {

    private val features = HashMap<String, Feature>()

    constructor() : super()

    private constructor(source: Parcel) {
        val features = ArrayList<Feature>()
        source.readTypedList(
            features,
            Feature
        )
        addAllFeatures(features)
    }

    fun getFeatures(): List<Feature> {
        return ArrayList(features.values)
    }

    fun getFeature(featureId: String): Feature? {
        return features[featureId]
    }

    fun hasFeature(featureId: String): Boolean {
        return features.containsKey(featureId)
    }

    fun addFeature(feature: Feature) {
        features[feature.id!!] = feature
    }

    fun addAllFeatures(features: List<Feature>) {
        for (feature in features) {
            this.features[feature.id!!] = feature
        }
    }

    fun removeFeature(featureId: String) {
        features.remove(featureId)
    }

    fun clearAllFeatures() {
        features.clear()
    }

    fun isEmpty(): Boolean {
        return features.isEmpty()
    }

    /**
     * Performs an operation on all [Feature]s of this collection.
     *
     * @param filter the filter to apply to all these [Feature]s
     */
    fun apply(filter: IFeatureFilterVisitor) {
        val features = getFeatures()

        for (feature in features) {
            feature.apply(filter)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(
        dest: Parcel,
        flags: Int
    ) {
        dest.writeTypedList(ArrayList(features.values))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FeatureCollection

        if (features.keys != other.features.keys) return false
        if (!features.keys.all { key -> features[key] == other.features[key] }) return false

        return true
    }

    override fun hashCode(): Int {
        return features.hashCode()
    }

    companion object CREATOR : Parcelable.Creator<FeatureCollection> {
        override fun createFromParcel(parcel: Parcel): FeatureCollection {
            return FeatureCollection(parcel)
        }

        override fun newArray(size: Int): Array<FeatureCollection?> {
            return arrayOfNulls(size)
        }
    }
}
