package fr.geonature.maps.ui.overlay

import android.graphics.Color
import fr.geonature.maps.jts.geojson.Feature
import fr.geonature.maps.jts.geojson.FeatureCollection
import fr.geonature.maps.jts.geojson.GeometryUtils.fromCoordinateSequence
import fr.geonature.maps.jts.geojson.GeometryUtils.fromPoint
import fr.geonature.maps.settings.LayerStyleSettings
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryCollection
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.Polygon
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.OverlayWithIW
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.Polygon as PolygonOverlay

/**
 * Creates Overlay from [Feature].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
object FeatureOverlayFactory {

    fun createOverlay(featureCollection: FeatureCollection,
                      layerStyle: LayerStyleSettings = LayerStyleSettings()): Overlay {
        return createOverlay(featureCollection.getFeatures(),
                             layerStyle)
    }

    fun createOverlay(features: List<Feature>,
                      layerStyle: LayerStyleSettings = LayerStyleSettings()): Overlay {
        return FeaturesOverlay().also { featuresOverlay ->
            features.forEach {
                featuresOverlay.add(createOverlay(it,
                                                  layerStyle))
            }
        }
    }

    fun createOverlay(feature: Feature,
                      layerStyle: LayerStyleSettings = LayerStyleSettings()): Overlay? {
        return createOverlay(feature.geometry,
                             layerStyle,
                             feature.id)
    }

    fun createOverlay(geometry: Geometry,
                      layerStyle: LayerStyleSettings = LayerStyleSettings(),
                      id: String? = null): Overlay? {
        return when (geometry) {
            is Point -> createOverlay(geometry,
                                      layerStyle)
            is LineString -> createOverlay(geometry,
                                           layerStyle)
            is Polygon -> createOverlay(geometry,
                                        layerStyle)
            is GeometryCollection -> createOverlay(geometry,
                                                   layerStyle)
            else -> null
        }.also {
            when (it) {
                is OverlayWithIW -> it.id = id
                is FeaturesOverlay -> it.id = id
            }
        }
    }

    fun createOverlay(geometryCollection: GeometryCollection,
                      layerStyle: LayerStyleSettings = LayerStyleSettings()): Overlay {
        return FeaturesOverlay().also { featuresOverlay ->
            if (geometryCollection.numGeometries > 0) {
                IntRange(0,
                         geometryCollection.numGeometries - 1).map {
                    createOverlay(geometryCollection.getGeometryN(it),
                                  layerStyle)
                }
                        .forEach { featuresOverlay.add(it) }
            }
        }
    }

    fun createOverlay(point: Point,
                      layerStyle: LayerStyleSettings = LayerStyleSettings()): Overlay {
        return PolygonOverlay().apply {
            points = PolygonOverlay.pointsAsCircle(fromPoint(point),
                                                   20.0)
            if (layerStyle.stroke) {
                strokeColor = layerStyle.color
                strokeWidth = layerStyle.weight.toFloat()
            }
            else {
                strokeColor = Color.TRANSPARENT
                strokeWidth = 0f
            }

            if (layerStyle.fill) {
                fillColor = layerStyle.fillColor
            }
        }
    }

    fun createOverlay(lineString: LineString,
                      layerStyle: LayerStyleSettings = LayerStyleSettings()): Overlay {
        return Polyline().apply {
            setPoints(fromCoordinateSequence(lineString.coordinateSequence))

            color = layerStyle.color
            width = layerStyle.weight.toFloat()
        }
    }

    fun createOverlay(polygon: Polygon,
                      layerStyle: LayerStyleSettings = LayerStyleSettings()): Overlay {
        return PolygonOverlay().apply {
            points = fromCoordinateSequence(polygon.exteriorRing.coordinateSequence)

            if (polygon.numInteriorRing > 0) {
                holes = IntRange(0,
                                 polygon.numInteriorRing - 1).map {
                    fromCoordinateSequence(polygon.getInteriorRingN(it).coordinateSequence)
                }
            }

            if (layerStyle.stroke) {
                strokeColor = layerStyle.color
                strokeWidth = layerStyle.weight.toFloat()
            }
            else {
                strokeColor = Color.TRANSPARENT
                strokeWidth = 0f
            }

            if (layerStyle.fill) {
                fillColor = layerStyle.fillColor
            }
        }
    }
}