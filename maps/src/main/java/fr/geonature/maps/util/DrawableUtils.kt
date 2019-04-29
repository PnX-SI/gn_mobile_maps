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

    fun toBitmap(
        context: Context, @DrawableRes drawableResourceId: Int, @ColorInt tintColor: Int,
        scale: Float = 1.0f
    ): Bitmap {
        val drawable = context.resources.getDrawable(drawableResourceId, context.theme)
        drawable.setTint(tintColor)

        return Bitmap.createBitmap(
            (drawable.intrinsicWidth * scale).toInt(),
            (drawable.intrinsicHeight * scale).toInt(),
            Bitmap.Config.ARGB_8888
        ).also {
            val canvas = Canvas(it)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        }
    }

    fun createScaledDrawable(
        context: Context, @DrawableRes drawableResourceId: Int, @ColorInt tintColor: Int,
        scale: Float = 1.0f
    ): Drawable {

        return BitmapDrawable(
            context.resources, toBitmap(context, drawableResourceId, tintColor, scale)
        )
    }
}