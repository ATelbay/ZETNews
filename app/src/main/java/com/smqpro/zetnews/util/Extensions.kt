package com.smqpro.zetnews.util

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Html
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.smqpro.zetnews.R
import org.ocpsoft.prettytime.PrettyTime
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

// Fragments' extensions


// Context's extensions
fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.sendShareIntent(text: String) {
    startActivity(Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    })
}

fun Context.getDrawableCompat(@DrawableRes drawableRes: Int): Drawable? {
    return AppCompatResources.getDrawable(this, drawableRes)
}

fun Context.dpToPixels(dp: Float) =
    dp * (resources
        .displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)


// Other extensions
val Any.TAG: String
    get() {
        val tag = javaClass.simpleName
        return if (tag.length <= 23) tag else tag.substring(0, 23)
    }

fun Any.prettyTime(dateStr: String): String {
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
    try {
        val date = format.parse(dateStr)
        val prettyTime = PrettyTime(Locale.getDefault())
        return prettyTime.format(date)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return ""
}

fun Any.htmlParse(str: String): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        Html.fromHtml(str, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
    else
        Html.fromHtml(str).toString()
}

fun Any.logE(tag: String?, msg: String?) {
    val stackTrace = Exception().stackTrace[1]
    var fileName = stackTrace.fileName
    if (fileName == null) fileName = "" // It is necessary if you want to use proguard obfuscation.
    val info = (stackTrace.methodName + " (" + fileName + ":"
            + stackTrace.lineNumber + ")")
    Log.e(tag, "$info: $msg")
}


// Activities' extensions

// View extensions
fun ImageView.load(
    url: String,
    loadOnlyFromCache: Boolean = false,
    onLoadingFinished: (px: Int) -> Unit = {}
) {
    val listener = object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            onLoadingFinished(0)
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            Log.d(TAG, "onResourceReady: drawable height - ${resource?.intrinsicHeight}")
            onLoadingFinished(resource?.intrinsicHeight ?: 0)
            return false
        }
    }


    val requestOptions = RequestOptions.placeholderOf(R.drawable.aaa)
        .dontTransform()
        .onlyRetrieveFromCache(loadOnlyFromCache)
    Glide.with(this)
        .load(url)
        .apply(requestOptions)
        .listener(listener)
        .into(this)

}

fun View.setColorAnimated(toColor: Int) {
    val from = FloatArray(3)
    val to = FloatArray(3)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Color.colorToHSV(this.solidColor, from)
        Color.colorToHSV(context.getColor(toColor), to)
    } else {
        Color.colorToHSV(this.solidColor, from)
        Color.colorToHSV(resources.getColor(toColor), to)
    }

    Color.colorToHSV(Color.parseColor("#FFFF0000"), to)


    val anim: ValueAnimator = ValueAnimator.ofFloat(0F, 1F) // animate from 0 to 1

    anim.duration = 300 // for 300 ms


    val hsv = FloatArray(3) // transition color

    anim.addUpdateListener { animation -> // Transition along each axis of HSV (hue, saturation, value)
        hsv[0] = from[0] + (to[0] - from[0]) * animation.animatedFraction
        hsv[1] = from[1] + (to[1] - from[1]) * animation.animatedFraction
        hsv[2] = from[2] + (to[2] - from[2]) * animation.animatedFraction
        this.setBackgroundColor(Color.HSVToColor(hsv))
    }

    anim.start()
}

