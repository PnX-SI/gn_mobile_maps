package fr.geonature.maps.maplibre.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import kotlin.math.log10
import kotlin.math.pow

/**
 * Simple scale bar showing the distance represented by a given width on the map.
 *
 * @author S. Grimault
 */
internal class ScaleBarView(context: Context) : View(context) {

    private val density = resources.displayMetrics.density
    private val maxBarWidth = MAX_BAR_WIDTH_DP * density

    private var barWidth = 0f
    private var label = ""

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f * density
        color = Color.DKGRAY
    }
    private val haloPaint = Paint(linePaint).apply {
        strokeWidth = 4f * density
        color = Color.WHITE
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 12f * density
        color = Color.DKGRAY
    }
    private val textHaloPaint = Paint(textPaint).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f * density
        color = Color.WHITE
    }

    /**
     * Updates the scale from the given resolution.
     *
     * @param metersPerDp the number of meters represented by one density-independent pixel, as
     * returned by `Projection.getMetersPerPixelAtLatitude()` (MapLibre works in logical pixels, not
     * physical ones)
     */
    fun update(metersPerDp: Double) {
        if (metersPerDp <= 0.0 || metersPerDp.isNaN()) return

        val metersPerPixel = metersPerDp / density

        val distance = niceDistance(maxBarWidth * metersPerPixel)

        barWidth = (distance / metersPerPixel).toFloat()
        label = if (distance >= 1000) "${(distance / 1000).toInt()} km" else "${distance.toInt()} m"

        invalidate()
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        setMeasuredDimension(
            (maxBarWidth + 8 * density).toInt(),
            (32 * density).toInt()
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (barWidth <= 0f) return

        val padding = 4 * density
        val y = height - padding
        val tick = 6 * density

        listOf(
            haloPaint,
            linePaint
        ).forEach {
            canvas.drawLine(
                padding,
                y,
                padding + barWidth,
                y,
                it
            )
            canvas.drawLine(
                padding,
                y,
                padding,
                y - tick,
                it
            )
            canvas.drawLine(
                padding + barWidth,
                y,
                padding + barWidth,
                y - tick,
                it
            )
        }

        val textY = y - tick - 2 * density
        canvas.drawText(
            label,
            padding,
            textY,
            textHaloPaint
        )
        canvas.drawText(
            label,
            padding,
            textY,
            textPaint
        )
    }

    companion object {
        private const val MAX_BAR_WIDTH_DP = 100

        /**
         * Rounds the given distance down to 1, 2 or 5 times a power of ten.
         */
        internal fun niceDistance(distance: Double): Double {
            if (distance <= 0.0) return 0.0

            val magnitude = 10.0.pow(log10(distance).toInt().toDouble())
                .let { if (it > distance) it / 10 else it }
            val fraction = distance / magnitude

            return magnitude * when {
                fraction >= 5 -> 5
                fraction >= 2 -> 2
                else -> 1
            }
        }
    }
}

