package fr.geonature.maps.ui.overlay.feature

import fr.geonature.maps.jts.geojson.Feature
import fr.geonature.maps.jts.geojson.FeatureCollection
import fr.geonature.maps.settings.LayerStyleSettings
import org.osmdroid.views.overlay.FolderOverlay

/**
 * Draws [FeatureCollection] on the map.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class FeatureCollectionOverlay : FolderOverlay() {

    fun setFeatureCollection(featureCollection: FeatureCollection,
                             layerStyle: LayerStyleSettings = LayerStyleSettings()) {
        setFeatures(featureCollection.getFeatures(),
                    layerStyle)
    }

    fun setFeatures(features: List<Feature>,
                    layerStyle: LayerStyleSettings = LayerStyleSettings()) {
        features.forEach {
            add(FeatureOverlay().apply {
                setFeature(it,
                           layerStyle)
            })
        }
    }
}