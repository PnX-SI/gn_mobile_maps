package fr.geonature.maps.ui.overlay.feature

import android.graphics.Canvas
import fr.geonature.maps.jts.geojson.Feature
import fr.geonature.maps.settings.LayerStyleSettings
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryCollection
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.Polygon
import org.osmdroid.views.Projection
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.OverlayWithIW

/**
 * Draws [Feature] on the map.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class FeatureOverlay : OverlayWithIW() {

    internal var backendOverlay: AbstractGeometryOverlay<Geometry, Overlay>? = null
    protected var feature: Feature? = null

    @Suppress("UNCHECKED_CAST")
    fun setFeature(feature: Feature,
                   layerStyle: LayerStyleSettings = LayerStyleSettings()) {
        id = feature.id
        this.feature = feature

        backendOverlay = when (feature.geometry) {
            is Point -> CirclePointOverlay()
            is LineString -> LineStringOverlay()
            is Polygon -> PolygonOverlay()
            is GeometryCollection -> GeometryCollectionOverlay()
            else -> null
        } as AbstractGeometryOverlay<Geometry, Overlay>?

        backendOverlay?.setGeometry(feature.geometry,
                                    layerStyle)
    }

    override fun draw(pCanvas: Canvas?,
                      pProjection: Projection?) {
        super.draw(pCanvas,
                   pProjection)

        backendOverlay?.draw(pCanvas,
                             pProjection)
    }
}