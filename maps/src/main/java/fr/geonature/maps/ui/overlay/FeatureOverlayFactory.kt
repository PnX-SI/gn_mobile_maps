package fr.geonature.maps.ui.overlay

import fr.geonature.maps.jts.geojson.Feature
import fr.geonature.maps.jts.geojson.FeatureCollection
import fr.geonature.maps.jts.geojson.GeometryUtils.fromCoordinateSequence
import fr.geonature.maps.jts.geojson.GeometryUtils.fromPoint
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryCollection
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.Polygon
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.Polygon as PolygonOverlay

/**
 * Creates Overlay from [Feature].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
object FeatureOverlayFactory {

    fun createOverlay(featureCollection: FeatureCollection): Overlay {
        return createOverlay(featureCollection.getFeatures())
    }

    fun createOverlay(features: List<Feature>): Overlay {
        return FolderOverlay().also { folderOverlay ->
            features.forEach { folderOverlay.add(createOverlay(it)) }
        }
    }

    fun createOverlay(feature: Feature): Overlay? {
        return createOverlay(feature.geometry)
    }

    fun createOverlay(geometry: Geometry): Overlay? {
        return when (geometry) {
            is Point -> createOverlay(geometry)
            is LineString -> createOverlay(geometry)
            is Polygon -> createOverlay(geometry)
            is GeometryCollection -> createOverlay(geometry)
            else -> null
        }
    }

    fun createOverlay(geometryCollection: GeometryCollection): Overlay {
        return FolderOverlay().also { folderOverlay ->
            if (geometryCollection.numGeometries > 0) {
                IntRange(0,
                         geometryCollection.numGeometries - 1).map { createOverlay(geometryCollection.getGeometryN(it)) }
                        .forEach { folderOverlay.add(it) }
            }
        }
    }

    fun createOverlay(point: Point): Overlay {
        return PolygonOverlay().apply {
            points = PolygonOverlay.pointsAsCircle(fromPoint(point),
                                                   20.0)
        }
    }

    fun createOverlay(lineString: LineString): Overlay {
        return Polyline().apply {
            setPoints(fromCoordinateSequence(lineString.coordinateSequence))
        }
    }

    fun createOverlay(polygon: Polygon): Overlay {
        return PolygonOverlay().apply {
            points = fromCoordinateSequence(polygon.exteriorRing.coordinateSequence)

            if (polygon.numInteriorRing > 0) {
                holes = IntRange(0,
                                 polygon.numInteriorRing - 1).map {
                    fromCoordinateSequence(polygon.getInteriorRingN(it).coordinateSequence)
                }
            }
        }
    }
}