package fr.geonature.maps.ui.overlay.feature

import android.graphics.Canvas
import fr.geonature.maps.settings.LayerStyleSettings
import org.locationtech.jts.geom.Geometry
import org.osmdroid.views.Projection
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.OverlayWithIW

/**
 * Base Geometry Overlay.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
abstract class AbstractGeometryOverlay<G : Geometry, O : Overlay>(internal var backendOverlay: O) :
    OverlayWithIW() {

    internal var geometry: G? = null

    fun setGeometry(geometry: G,
                    layerStyle: LayerStyleSettings = LayerStyleSettings()) {
        this.geometry = geometry

        applyGeometry(geometry,
                      layerStyle)
    }

    /**
     * Draw given geometry through backend overlay.
     */
    abstract fun applyGeometry(geometry: G,
                               layerStyle: LayerStyleSettings = LayerStyleSettings())

    /**
     * Apply style to backend overlay.
     */
    abstract fun setStyle(layerStyle: LayerStyleSettings = LayerStyleSettings())

    override fun draw(pCanvas: Canvas?,
                      pProjection: Projection?) {
        super.draw(pCanvas,
                   pProjection)

        backendOverlay.draw(pCanvas,
                            pProjection)
    }
}