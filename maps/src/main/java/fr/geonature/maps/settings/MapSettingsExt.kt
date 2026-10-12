package fr.geonature.maps.settings
import fr.geonature.maps.jts.LatLng
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import fr.geonature.maps.jts.BoundingBox as SettingsBoundingBox

/**
 * Converts [fr.geonature.maps.jts.LatLng] to osmdroid [GeoPoint].
 */
fun LatLng.toGeoPoint(): GeoPoint = GeoPoint(
    latitude,
    longitude
)

/**
 * Converts osmdroid [GeoPoint] to [LatLng].
 */
fun GeoPoint.toLatLng(): LatLng = LatLng(
    latitude,
    longitude
)

/**
 * Converts [SettingsBoundingBox] to osmdroid [BoundingBox].
 */
fun SettingsBoundingBox.toOsmdroidBoundingBox(): BoundingBox = BoundingBox(
    latNorth,
    lonEast,
    latSouth,
    lonWest
)

/**
 * Converts osmdroid [BoundingBox] to [SettingsBoundingBox].
 */
fun BoundingBox.toSettingsBoundingBox(): SettingsBoundingBox = SettingsBoundingBox(
    latNorth,
    lonEast,
    latSouth,
    lonWest
)
