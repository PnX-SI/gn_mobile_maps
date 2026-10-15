package fr.geonature.maps.osmdroid.ui.overlay.feature

import android.graphics.Color
import fr.geonature.maps.settings.LayerStyleSettings
import fr.geonature.maps.osmdroid.util.toGeoPoints
import org.locationtech.jts.geom.Polygon

/**
 * Draws Polygon on the map.
 *
 * @author S. Grimault
 */
class PolygonOverlay :
    AbstractGeometryOverlay<Polygon, org.osmdroid.views.overlay.Polygon>(org.osmdroid.views.overlay.Polygon()) {

    override fun applyGeometry(
        geometry: Polygon,
        layerStyle: LayerStyleSettings
    ) {
        backendOverlay.apply {
            points = geometry.exteriorRing.coordinateSequence.toGeoPoints()

            if (geometry.numInteriorRing > 0) {
                holes = IntRange(
                    0,
                    geometry.numInteriorRing - 1
                ).map {
                    geometry.getInteriorRingN(it).coordinateSequence.toGeoPoints()
                }
            }
        }
        setStyle(layerStyle)
    }

    override fun setStyle(layerStyle: LayerStyleSettings) {
        backendOverlay.apply {
            with(outlinePaint) {
                color = if (layerStyle.stroke) layerStyle.color else Color.TRANSPARENT
                strokeWidth = if (layerStyle.stroke) layerStyle.weight.toFloat() else 0f
            }

            if (layerStyle.fill) {
                fillPaint.color = layerStyle.fillColor
            }
        }
    }
}
