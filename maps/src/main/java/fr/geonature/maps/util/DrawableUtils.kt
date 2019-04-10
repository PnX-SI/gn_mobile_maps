package fr.geonature.maps.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

/**
 * Helper class about [Drawable]s.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
object DrawableUtils {

    fun createScaledDrawable(context: Context,
                             @DrawableRes drawableResourceId: Int,
                             @ColorInt tintColor: Int,
                             scale: Float = 1.0f): Drawable? {

        val drawable = context.resources.getDrawable(
            drawableResourceId,
            context.theme)
        drawable.setTint(tintColor)
        val bitmap = Bitmap.createBitmap(
            (drawable.intrinsicWidth * scale).toInt(),
            (drawable.intrinsicHeight * scale).toInt(),
            Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(
            0,
            0,
            canvas.width,
            canvas.height)
        drawable.draw(canvas)

        return BitmapDrawable(
            context.resources,
            bitmap)
    }
}