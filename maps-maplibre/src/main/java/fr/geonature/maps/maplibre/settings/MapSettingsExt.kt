package fr.geonature.maps.maplibre.settings

import fr.geonature.maps.jts.BoundingBox
import fr.geonature.maps.jts.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.geometry.LatLng as MapLibreLatLng

/**
 * Converts this [LatLng] to the corresponding MapLibre `LatLng`.
 */
fun LatLng.toMapLibreLatLng(): MapLibreLatLng = MapLibreLatLng(
    latitude,
    longitude
)

/**
 * Converts this [BoundingBox] to the corresponding MapLibre `LatLngBounds`.
 */
fun BoundingBox.toMapLibreLatLngBounds(): LatLngBounds = LatLngBounds.from(
    latNorth,
    lonEast,
    latSouth,
    lonWest
)
