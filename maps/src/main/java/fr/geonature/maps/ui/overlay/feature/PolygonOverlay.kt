package fr.geonature.maps.ui.overlay.feature

import android.graphics.Color
import fr.geonature.maps.jts.geojson.GeometryUtils
import fr.geonature.maps.settings.LayerStyleSettings
import org.locationtech.jts.geom.Polygon

/**
 * Draws Polygon on the map.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class PolygonOverlay :
    AbstractGeometryOverlay<Polygon, org.osmdroid.views.overlay.Polygon>(org.osmdroid.views.overlay.Polygon()) {

    override fun applyGeometry(
        geometry: Polygon,
        layerStyle: LayerStyleSettings
    ) {
        backendOverlay.apply {
            points = GeometryUtils.fromCoordinateSequence(geometry.exteriorRing.coordinateSequence)

            if (geometry.numInteriorRing > 0) {
                holes = IntRange(
                    0,
                    geometry.numInteriorRing - 1
                ).map {
                    GeometryUtils.fromCoordinateSequence(geometry.getInteriorRingN(it).coordinateSequence)
                }
            }
        }
        setStyle(layerStyle)
    }

    override fun setStyle(layerStyle: LayerStyleSettings) {
        backendOverlay.apply {
            if (layerStyle.stroke) {
                strokeColor = layerStyle.color
                strokeWidth = layerStyle.weight.toFloat()
            } else {
                strokeColor = Color.TRANSPARENT
                strokeWidth = 0f
            }

            if (layerStyle.fill) {
                fillColor = layerStyle.fillColor
            }
        }
    }
}
