package fr.geonature.maps.ui.overlay.feature

import android.graphics.Color
import fr.geonature.maps.jts.geojson.GeometryUtils.fromPoint
import fr.geonature.maps.settings.LayerStyleSettings
import org.locationtech.jts.geom.Point
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polygon.pointsAsCircle

/**
 * Draws Point as circle on the map.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class CirclePointOverlay(private var radiusInMeters: Double = 10.0) :
    AbstractGeometryOverlay<Point, Polygon>(Polygon()) {

    override fun applyGeometry(
        geometry: Point,
        layerStyle: LayerStyleSettings
    ) {
        backendOverlay.points = pointsAsCircle(
            fromPoint(geometry),
            radiusInMeters
        )
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
