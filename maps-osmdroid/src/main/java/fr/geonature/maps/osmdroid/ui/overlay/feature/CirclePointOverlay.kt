package fr.geonature.maps.osmdroid.ui.overlay.feature

import android.graphics.Color
import fr.geonature.maps.settings.LayerStyleSettings
import fr.geonature.maps.osmdroid.util.toGeoPoint
import org.locationtech.jts.geom.Point
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polygon.pointsAsCircle

/**
 * Draws Point as circle on the map.
 *
 * @author S. Grimault
 */
class CirclePointOverlay(private var radiusInMeters: Double = 10.0) :
    AbstractGeometryOverlay<Point, Polygon>(Polygon()) {

    override fun applyGeometry(
        geometry: Point,
        layerStyle: LayerStyleSettings
    ) {
        backendOverlay.points = pointsAsCircle(
            geometry.toGeoPoint(),
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
