package fr.geonature.maps.ui.overlay.feature

import fr.geonature.maps.jts.geojson.Feature
import fr.geonature.maps.jts.geojson.FeatureCollection
import fr.geonature.maps.settings.LayerStyleSettings
import fr.geonature.maps.ui.overlay.feature.filter.IFeatureOverlayFilterVisitor
import org.osmdroid.views.overlay.FolderOverlay

/**
 * Draws [FeatureCollection] on the map.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class FeatureCollectionOverlay : FolderOverlay() {

    var layerStyle: LayerStyleSettings = LayerStyleSettings()
        private set

    fun setFeatureCollection(
        featureCollection: FeatureCollection,
        layerStyle: LayerStyleSettings = LayerStyleSettings()
    ) {
        setFeatures(
            featureCollection.getFeatures(),
            layerStyle
        )
    }

    fun setFeatures(
        features: List<Feature>,
        layerStyle: LayerStyleSettings = LayerStyleSettings()
    ) {
        this.layerStyle = layerStyle

        features.forEach {
            add(FeatureOverlay().apply {
                setFeature(
                    it,
                    layerStyle
                )
            })
        }
    }

    fun setStyle(layerStyle: LayerStyleSettings = LayerStyleSettings()) {
        this.layerStyle = layerStyle

        getFeatureOverlays().forEach {
            it.setStyle(layerStyle)
        }
    }

    fun getFeatureOverlays(filter: (overlay: FeatureOverlay) -> Boolean = { true }): List<FeatureOverlay> {
        return items.asSequence()
            .filterNotNull()
            .filter { it is FeatureOverlay }
            .map { it as FeatureOverlay }
            .filter(filter)
            .toList()
    }

    /**
     * Performs an operation on this [FeatureCollectionOverlay].
     *
     * @param filter the filter to apply
     */
    fun apply(filter: IFeatureOverlayFilterVisitor) {
        getFeatureOverlays().forEach { it.apply(filter) }
    }
}
