package fr.geonature.maps.ui.overlay.feature.filter

import fr.geonature.maps.jts.geojson.Feature
import fr.geonature.maps.jts.geojson.filter.IFeatureFilterVisitor
import fr.geonature.maps.settings.LayerStyleSettings
import fr.geonature.maps.ui.overlay.feature.FeatureOverlay

/**
 * Visitor pattern to apply a concrete filter to a given [FeatureOverlay] [Feature].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
interface IFeatureOverlayFilterVisitor : IFeatureFilterVisitor {

    /**
     * Returns the corresponding [LayerStyleSettings] style if the given [Feature] matches the filter or not
     */
    fun getStyle(
        feature: Feature,
        selected: Boolean
    ): LayerStyleSettings
}
