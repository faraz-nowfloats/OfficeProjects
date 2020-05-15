package com.framework.glide.util

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.framework.R
import com.framework.views.CircularImageView
import com.framework.views.customViews.CustomImageView
import jp.wasabeef.glide.transformations.BlurTransformation

fun Context.glideLoad(mImageView: CustomImageView?, url: String) {
    if (mImageView == null) return
    Glide.with(this).load(url).skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.DATA).into(mImageView)
}

fun Context.glideLoad(mImageView: CircularImageView?, url: String) {
    if (mImageView == null) return
    Glide.with(this).load(url)
            .skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.DATA).into(mImageView)
}

fun Context.glideLoad(mImageView: CustomImageView, url: String?, placeholder: Int?) {
    if (url?.isEmpty() == true) return
    val glide = Glide.with(this).load(url).skipMemoryCache(true)
    placeholder?.let { glide.placeholder(it) }
    glide.diskCacheStrategy(DiskCacheStrategy.DATA)
            .into(mImageView)
}


fun Context.glideLoadCircle(mImageView: CustomImageView, url: String) {
    Glide.with(this).load(url)
            .apply(RequestOptions.circleCropTransform()).skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.DATA).into(mImageView)
}

fun Context.glideLoadBlur(mImageView: CustomImageView, url: String) {
    Glide.with(this).load(url).diskCacheStrategy(DiskCacheStrategy.DATA)
            .apply(RequestOptions.bitmapTransform(BlurTransformation(25, 3))).into(mImageView)
}

fun Context.glideLoadColor(mImageView: CustomImageView, url: String, view: View) {
    Glide.with(this)
            .asBitmap().load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any,
                        target: Target<Bitmap>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                ): Boolean {
                    if (resource != null) {
                        val p = Palette.from(resource).generate()
                        // Use generated instance
                        view.setUpInfoBackgroundColor(p)
                    }
                    return false
                }
            }).into(mImageView)
}


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
private fun View.setUpInfoBackgroundColor(palette: Palette) {
    val swatch = getMostPopulousSwatch(palette)
    if (swatch != null) {
        val startColor = ContextCompat.getColor(context, R.color.secondary_text)
        val endColor = swatch.rgb
        backgroundTintList = ColorStateList.valueOf(endColor)
    }
}

fun getMostPopulousSwatch(palette: Palette?): Palette.Swatch? {
    var mostPopulous: Palette.Swatch? = null
    if (palette != null) {
        for (swatch in palette.swatches) {
            if (mostPopulous == null || swatch.population > mostPopulous.population) {
                mostPopulous = swatch
            }
        }
    }
    return mostPopulous
}
