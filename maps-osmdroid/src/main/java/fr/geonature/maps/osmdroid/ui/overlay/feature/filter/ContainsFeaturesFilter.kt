package fr.geonature.maps.osmdroid.ui.overlay.feature.filter

import fr.geonature.maps.jts.geojson.Feature
import fr.geonature.maps.settings.LayerStyleSettings
import fr.geonature.maps.osmdroid.util.toPoint
import org.osmdroid.util.GeoPoint

/**
 * Gets a `List` of [Feature]s which contains a given [GeoPoint].
 *
 * @author S. Grimault
 */
class ContainsFeaturesFilter(
    private val geoPoint: GeoPoint,
    private val defaultStyleSettings: LayerStyleSettings,
    private val selectedStyleSettings: LayerStyleSettings
) :
    IFeatureOverlayFilterVisitor {

    private val features: MutableList<Feature> = mutableListOf()

    fun getSelectedFeatures(): List<Feature> {
        return features.toList()
    }

    override fun filter(feature: Feature): Boolean {
        val matches = feature.geometry.contains(geoPoint.toPoint())

        if (matches) {
            features.add(feature)
        }

        return matches
    }

    override fun getStyle(
        feature: Feature,
        selected: Boolean
    ): LayerStyleSettings {
        return if (selected) selectedStyleSettings else defaultStyleSettings
    }
}
