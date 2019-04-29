package fr.geonature.maps.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fr.geonature.maps.R
import fr.geonature.maps.util.ThemeUtils
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.views.MapView
import kotlin.math.absoluteValue

/**
 * Show map compass as floating action button.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class RotateCompassButton(
    context: Context, attrs: AttributeSet
) : FloatingActionButton(
    context, attrs
), MapListener {

    init {
        updateImageDrawable()
    }

    fun setMapView(mapView: MapView) {
        mapView.addMapListener(this)
        setOnClickListener {

            val compassAnimator = ValueAnimator.ofFloat(mapView.mapOrientation, 0f)
            compassAnimator.addUpdateListener {
                updateImageDrawable(it.animatedValue as Float)
            }
            compassAnimator.duration = Configuration.getInstance().animationSpeedDefault.toLong()
            compassAnimator.start()

            mapView.controller.animateTo(mapView.mapCenter, mapView.zoomLevelDouble, null, 0f)
        }
    }

    override fun onScroll(event: ScrollEvent?): Boolean {
        if (event == null) return true

        updateImageDrawable(event.source.mapOrientation)

        return true
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
        // nothing to do...
        return true
    }

    private fun updateImageDrawable(mapOrientation: Float = 0f) {
        val drawable = context.resources.getDrawable(R.drawable.ic_compass, context.theme)
        drawable.setTint(ThemeUtils.getAccentColor(context))

        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        // show "N" if map orientation is near from north
        if (mapOrientation.absoluteValue * 100 / 360 < 2f) {
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            textPaint.style = Paint.Style.FILL
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textPaint.textSize = 32f

            canvas.drawText(
                context.getText(R.string.compass_north).toString(),
                (bitmap.width / 2).toFloat(),
                (bitmap.height - textPaint.descent() - textPaint.ascent()) / 2,
                textPaint
            )
        }

        canvas.rotate(mapOrientation, (bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat())

        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        setImageDrawable(BitmapDrawable(context.resources, bitmap))
    }
}