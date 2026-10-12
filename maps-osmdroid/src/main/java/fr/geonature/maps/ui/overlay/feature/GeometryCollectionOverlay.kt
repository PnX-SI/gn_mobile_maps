package fr.geonature.maps.ui.overlay.feature

import fr.geonature.maps.settings.LayerStyleSettings
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryCollection
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.Polygon
import org.osmdroid.views.overlay.FolderOverlay

/**
 * Draws GeometryCollection on the map.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class GeometryCollectionOverlay :
    AbstractGeometryOverlay<GeometryCollection, FolderOverlay>(FolderOverlay()) {

    override fun applyGeometry(
        geometry: GeometryCollection,
        layerStyle: LayerStyleSettings
    ) {
        if (geometry.numGeometries > 0) {
            IntRange(
                0,
                geometry.numGeometries - 1
            ).forEach {
                val geometryN = geometry.getGeometryN(it)

                val overlay = when (geometryN) {
                    is Point -> CirclePointOverlay()
                    is LineString -> LineStringOverlay()
                    is Polygon -> PolygonOverlay()
                    is GeometryCollection -> GeometryCollectionOverlay()
                    else -> null
                }

                if (overlay != null) {
                    @Suppress("UNCHECKED_CAST") (overlay as AbstractGeometryOverlay<Geometry, *>).setGeometry(
                        geometryN,
                        layerStyle
                    )
                    backendOverlay.add(overlay)
                }
            }
        }
    }

    override fun setStyle(layerStyle: LayerStyleSettings) {
        backendOverlay.items.forEach {
            when (it) {
                is CirclePointOverlay -> it.setStyle(layerStyle)
                is LineStringOverlay -> it.setStyle(layerStyle)
                is PolygonOverlay -> it.setStyle(layerStyle)
                is GeometryCollectionOverlay -> it.setStyle(layerStyle)
            }
        }
    }
}
