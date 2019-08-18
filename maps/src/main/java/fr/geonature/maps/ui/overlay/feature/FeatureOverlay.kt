package fr.geonature.maps.ui.overlay.feature

import android.graphics.Canvas
import fr.geonature.maps.jts.geojson.Feature
import fr.geonature.maps.settings.LayerStyleSettings
import fr.geonature.maps.ui.overlay.feature.filter.IFeatureOverlayFilterVisitor
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
    internal var feature: Feature? = null

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

    /**
     * Performs an operation on this [FeatureOverlay].
     *
     * @param filter the filter to apply
     */
    fun apply(filter: IFeatureOverlayFilterVisitor) {
        val feature = this.feature ?: return

        val matches = filter.filter(feature)
        backendOverlay?.setStyle(filter.getStyle(feature,
                                                 matches))
    }

    override fun draw(pCanvas: Canvas?,
                      pProjection: Projection?) {
        super.draw(pCanvas,
                   pProjection)

        backendOverlay?.draw(pCanvas,
                             pProjection)
    }
}