package fr.geonature.maps.ui.overlay

import android.location.Location
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer

/**
 * Callback used by [MyLocationOverlay].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 *
 * @see MyLocationOverlay
 */
interface MyLocationListener : IMyLocationConsumer {

    /**
     * Called when the current location is outside map boundaries.
     */
    fun onLocationOutsideBoundaries(location: Location?)
}
