package fr.geonature.maps.ui.overlay

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

/**
 * Overlay that draws a guide marker (cross) in the center of the map.
 *
 * @author S. Grimault
 */
class MarkerGuideOverlay : Overlay() {

    private val paintDark = Paint().apply {
        color = 0x80000000.toInt()
        strokeWidth = 2.0f
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }

    private val paintLight = Paint().apply {
        color = 0x80FFFFFF.toInt()
        strokeWidth = 2.0f
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }

    /**
     * Draws the cross overlay on the map.
     *
     * @param canvas The canvas to draw on.
     * @param mapView The MapView that the overlay is attached to.
     * @param shadow If true, the shadow layer is being drawn. This overlay does not draw a shadow.
     */
    override fun draw(
        canvas: Canvas?,
        mapView: MapView?,
        shadow: Boolean
    ) {
        if (shadow || canvas == null || mapView == null) {
            return
        }

        drawMarkerGuide(
            canvas,
            PointF(
                mapView.width / 2f,
                mapView.height / 2f
            )
        )
    }

    private fun drawMarkerGuide(
        canvas: Canvas,
        position: PointF,
        segmentSize: Float = 16.0f,
        numberOfSegments: Int = 3
    ) {
        for (i in 0..<numberOfSegments) {
            // draw horizontal line (left part)
            canvas.drawLine(
                position.x - segmentSize * (numberOfSegments + 1 - i),
                position.y,
                position.x - segmentSize * (numberOfSegments - i),
                position.y,
                if (i % 2 == 0) paintDark else paintLight
            )
            // draw horizontal line (right part)
            canvas.drawLine(
                position.x + segmentSize * (numberOfSegments + 1 - i),
                position.y,
                position.x + segmentSize * (numberOfSegments - i),
                position.y,
                if (i % 2 == 0) paintDark else paintLight
            )
            // draw vertical line (top part)
            canvas.drawLine(
                position.x,
                position.y - segmentSize * (numberOfSegments + 1 - i),
                position.x,
                position.y - segmentSize * (numberOfSegments - i),
                if (i % 2 == 0) paintDark else paintLight
            )
            // draw vertical line (bottom part)
            canvas.drawLine(
                position.x,
                position.y + segmentSize * (numberOfSegments + 1 - i),
                position.x,
                position.y + segmentSize * (numberOfSegments - i),
                if (i % 2 == 0) paintDark else paintLight
            )
        }

        // draw circle at the center
        val oval = RectF(
            position.x - segmentSize / 2f,
            position.y - segmentSize / 2f,
            position.x + segmentSize / 2f,
            position.y + segmentSize / 2f
        )
        canvas.drawArc(
            oval,
            0f,
            90f,
            false,
            paintDark
        )
        canvas.drawArc(
            oval,
            90f,
            90f,
            false,
            paintLight
        )
        canvas.drawArc(
            oval,
            180f,
            90f,
            false,
            paintDark
        )
        canvas.drawArc(
            oval,
            270f,
            90f,
            false,
            paintLight
        )
    }
}
