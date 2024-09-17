package fr.geonature.maps.ui.overlay.feature

import android.graphics.Canvas
import fr.geonature.maps.settings.LayerStyleSettings
import fr.geonature.maps.ui.overlay.OverlayUtils.calculateBounds
import org.locationtech.jts.geom.Geometry
import org.osmdroid.util.BoundingBox
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

    override fun getBounds(): BoundingBox {
        return calculateBounds(backendOverlay)
    }

    fun setGeometry(
        geometry: G,
        layerStyle: LayerStyleSettings = LayerStyleSettings()
    ) {
        this.geometry = geometry

        applyGeometry(
            geometry,
            layerStyle
        )
    }

    /**
     * Draw given geometry through backend overlay.
     */
    abstract fun applyGeometry(
        geometry: G,
        layerStyle: LayerStyleSettings = LayerStyleSettings()
    )

    /**
     * Apply style to backend overlay.
     */
    abstract fun setStyle(layerStyle: LayerStyleSettings = LayerStyleSettings())

    override fun draw(
        pCanvas: Canvas?,
        pProjection: Projection?
    ) {
        super.draw(
            pCanvas,
            pProjection
        )

        if (isEnabled) {
            backendOverlay.draw(
                pCanvas,
                pProjection
            )
        }
    }
}
