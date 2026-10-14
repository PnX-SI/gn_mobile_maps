package fr.geonature.maps.ui.overlay.feature

import fr.geonature.maps.settings.LayerStyleSettings
import fr.geonature.maps.util.toGeoPoints
import org.locationtech.jts.geom.LineString
import org.osmdroid.views.overlay.Polyline

/**
 * Draws LineString on the map.
 *
 * @author S. Grimault
 */
class LineStringOverlay : AbstractGeometryOverlay<LineString, Polyline>(Polyline()) {

    override fun applyGeometry(
        geometry: LineString,
        layerStyle: LayerStyleSettings
    ) {
        backendOverlay.setPoints(geometry.coordinateSequence.toGeoPoints())
        setStyle(layerStyle)
    }

    override fun setStyle(layerStyle: LayerStyleSettings) {
        backendOverlay.apply {
            with(outlinePaint) {
                color = layerStyle.color
                strokeWidth = layerStyle.weight.toFloat()
            }
        }
    }
}
