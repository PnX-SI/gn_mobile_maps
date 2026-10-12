package fr.geonature.maps.ui.overlay

import org.osmdroid.util.BoundingBox

/**
 * Utility functions about BoundingBox.
 *
 * @author S. Grimault
 */

/**
 * Whether some given object is same to this one.
 */
fun BoundingBox.isSame(other: BoundingBox): Boolean =
    latNorth == other.latNorth && lonEast == other.lonEast && latSouth == other.latSouth && lonWest == other.lonWest