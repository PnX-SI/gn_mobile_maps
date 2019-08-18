package fr.geonature.maps.ui.overlay.feature

import fr.geonature.maps.jts.geojson.GeometryUtils
import fr.geonature.maps.settings.LayerStyleSettings
import org.locationtech.jts.geom.LineString
import org.osmdroid.views.overlay.Polyline

/**
 * Draws LineString on the map.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class LineStringOverlay : AbstractGeometryOverlay<LineString, Polyline>(Polyline()) {

    override fun applyGeometry(geometry: LineString,
                               layerStyle: LayerStyleSettings) {
        backendOverlay.setPoints(GeometryUtils.fromCoordinateSequence(geometry.coordinateSequence))
        setStyle(layerStyle)
    }

    override fun setStyle(layerStyle: LayerStyleSettings) {
        backendOverlay.apply {
            color = layerStyle.color
            width = layerStyle.weight.toFloat()
        }
    }
}