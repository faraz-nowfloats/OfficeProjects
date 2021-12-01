package com.boost.presignin.model.newOnboarding

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import com.boost.presignin.R
import com.boost.presignin.constant.RecyclerViewItemType
import com.boost.presignin.recyclerView.AppBaseRecyclerViewItem
import java.io.Serializable

class IntroItemNew(
    val title: String? = null,
    @DrawableRes val imageResource: Int? = null,
    @RawRes val lottieRawResource:Int? = null,
    val isLottieRepeat:Boolean? = false,
    @ColorRes val slideBackgroundColor:Int? = null
) : Serializable, AppBaseRecyclerViewItem {

    fun getData(c: Context): ArrayList<IntroItemNew> {
        return arrayListOf(
            IntroItemNew(
                title = c.getString(R.string.new_into_1_title),
                imageResource = null,
                lottieRawResource = R.raw.lottie_intro_1_merchant,
                isLottieRepeat = false,
                slideBackgroundColor = R.color.white_F4F8FA
            ),
            IntroItemNew(
                title = c.getString(R.string.new_into_2_title),
                imageResource = null,
                lottieRawResource = R.raw.lottie_intro_2_marketing,
                isLottieRepeat = false,
                slideBackgroundColor = R.color.white_F4F8FA
            ),
            IntroItemNew(
                title = c.getString(R.string.new_into_3_title),
                imageResource = null,
                lottieRawResource = R.raw.lottie_intro_3_products,
                isLottieRepeat = true,
                slideBackgroundColor = R.color.white_F4F8FA
            ),
            IntroItemNew(
                title = c.getString(R.string.new_into_4_title),
                imageResource = null,
                lottieRawResource = R.raw.lottie_intro_4_collect,
                isLottieRepeat = true,
                slideBackgroundColor = R.color.white_F7F7F4
            ),
        )
    }

    override fun getViewType(): Int {
        return RecyclerViewItemType.INTRO_NEW_SLIDES.getLayout()
    }
}