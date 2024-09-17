package fr.geonature.maps.ui.overlay

import org.osmdroid.util.BoundingBox
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.PolyOverlayWithIW

/**
 * Helper class about [Overlay]s.
 *
 * @author S. Grimault
 */
object OverlayUtils {

    /**
     * Gets the bounds of the given [Overlay].
     */
    fun calculateBounds(overlay: Overlay): BoundingBox {
        return when (overlay) {
            is Marker -> BoundingBox.fromGeoPoints(listOf(overlay.position))
            is PolyOverlayWithIW -> BoundingBox.fromGeoPoints(overlay.actualPoints)
            is FolderOverlay -> overlay.items.map { calculateBounds(it) }
                .reduce { acc, boundingBox -> acc.concat(boundingBox) }

            else -> overlay.bounds
        }
    }
}