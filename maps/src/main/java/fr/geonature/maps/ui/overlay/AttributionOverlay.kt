package fr.geonature.maps.ui.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.DisplayMetrics
import org.osmdroid.views.MapView
import org.osmdroid.views.Projection
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Overlay

/**
 * Kotlin backport of [CopyrightOverlay].
 *
 * @see CopyrightOverlay
 */
class AttributionOverlay(context: Context) : Overlay() {
    private var textPaint: TextPaint
    private var dm: DisplayMetrics = context.resources.displayMetrics

    private var xOffset = 8
    private var yOffset = 8
    private var alignBottom = true
    private var alignRight = false
    private var attribution: String? = null

    init {
        textPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = dm.density * 12
        }
    }

    fun setTextSize(fontSize: Int) {
        textPaint.textSize = dm.density * fontSize
    }

    fun setTextColor(color: Int) {
        textPaint.color = color
    }

    fun setAlignBottom(alignBottom: Boolean) {
        this.alignBottom = alignBottom
    }

    fun setAlignRight(alignRight: Boolean) {
        this.alignRight = alignRight
    }

    override fun draw(
        canvas: Canvas,
        map: MapView,
        shadow: Boolean
    ) {
        attribution = map.tileProvider.tileSource.copyrightNotice

        draw(
            canvas,
            map.projection
        )
    }

    override fun draw(
        canvas: Canvas,
        pProjection: Projection
    ) {
        val attribution = attribution?.takeIf { it.isNotBlank() } ?: return

        val width = canvas.width
        val height = canvas.height

        val textLayout =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) StaticLayout.Builder.obtain(
                attribution,
                0,
                attribution.length,
                textPaint,
                (canvas.width / 2 + yOffset)
            )
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setMaxLines(2)
                .setEllipsize(TextUtils.TruncateAt.END)
                .setEllipsizedWidth((canvas.width / 2 + yOffset))
                .build()
            else StaticLayout(
                attribution,
                0,
                attribution.length,
                textPaint,
                (canvas.width / 2 + yOffset),
                Layout.Alignment.ALIGN_NORMAL,
                1.0f,
                0.0f,
                true,
                TextUtils.TruncateAt.END,
                (canvas.width / 2 + yOffset),
            )

        val dx = if (alignRight) {
            textPaint.textAlign = Paint.Align.RIGHT
            width.toFloat() - xOffset
        } else {
            textPaint.textAlign = Paint.Align.LEFT
            xOffset.toFloat()
        }
        val dy = if (alignBottom) (height - yOffset - textLayout.height.toFloat())
        else yOffset.toFloat()

        // draw the text
        pProjection.save(
            canvas,
            false,
            true
        )
        canvas.translate(
            dx,
            dy
        )
        textLayout.draw(canvas)
        pProjection.restore(
            canvas,
            true
        )
    }
}