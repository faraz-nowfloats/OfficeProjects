package com.boost.presignin.ui.intro

import android.content.DialogInterface
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.viewpager2.widget.ViewPager2
import com.boost.presignin.R
import com.boost.presignin.adapter.IntroAdapter
import com.boost.presignin.databinding.ActivityIntroBinding
import com.boost.presignin.dialog.WebViewDialog
import com.boost.presignin.helper.WebEngageController
import com.boost.presignin.model.IntroItem
import com.boost.presignin.ui.mobileVerification.MobileVerificationActivity
import com.framework.base.BaseActivity
import com.framework.models.BaseViewModel
import com.framework.utils.RootUtil
import com.framework.utils.makeLinks
import com.framework.webengageconstant.*

class IntroActivity : BaseActivity<ActivityIntroBinding, BaseViewModel>() {

  private lateinit var items: List<IntroItem>
  private var isVideoPlaying = false
  private val handler = Handler(Looper.getMainLooper())

  override fun getLayout(): Int {
    return R.layout.activity_intro
  }

  override fun getViewModelClass(): Class<BaseViewModel> {
    return BaseViewModel::class.java
  }

  private val nextRunnable = Runnable {
    Log.i(TAG, "aut swipe runnable: ")
    binding?.introViewpager?.post {
      if (!isVideoPlaying) {
        val lastPosition: Int? = binding?.introViewpager?.adapter?.itemCount?.minus(1)
        val mCurrentPosition = binding?.introViewpager?.currentItem ?: 0
        val isLast = (mCurrentPosition == lastPosition)
        binding?.introViewpager?.setCurrentItem(
          if (isLast) 0 else mCurrentPosition + 1,
          isLast.not()
        )
        nextPageTimer()
      }
    }
  }

  private fun initTncString() {
    binding?.acceptTnc?.makeLinks(
      Pair("terms", View.OnClickListener {
        WebEngageController.trackEvent(BOOST_360_TERMS_CLICK, CLICKED, NO_EVENT_VALUE)
        openTNCDialog(
          "https://www.getboost360.com/tnc?src=android&stage=presignup",
          resources.getString(R.string.boost360_terms_conditions)
        )
      }),
      Pair("conditions", View.OnClickListener {
        WebEngageController.trackEvent(BOOST_360_CONDITIONS_CLICK, CLICKED, NO_EVENT_VALUE)
        openTNCDialog(
          "https://www.getboost360.com/tnc?src=android&stage=presignup",
          resources.getString(R.string.boost360_terms_conditions)
        )
      })
    )
  }

  private fun openTNCDialog(url: String, title: String) {
    WebViewDialog().apply {
      setData(false, url, title)
      onClickType = { }
      show(this@IntroActivity.supportFragmentManager, title)
    }
  }

  override fun onCreateView() {
    items = IntroItem().getData(this)
    initTncString()
    nextPageTimer()
    binding?.introViewpager?.apply {
      adapter = IntroAdapter(supportFragmentManager, lifecycle, items, { setNextPage() },
        {
          isVideoPlaying = it;
          Log.i(TAG, "is video playing changed: $it")
        }
      )
      orientation = ViewPager2.ORIENTATION_HORIZONTAL
      binding?.introIndicator?.setViewPager2(binding!!.introViewpager)
      binding?.introViewpager?.offscreenPageLimit = items.size
      binding?.introViewpager?.registerOnPageChangeCallback(object : CircularViewPagerHandler(this) {
        override fun onPageSelected(position: Int) {
          super.onPageSelected(position)
          Log.i(TAG, "onPageSelected: ")
          if (position != 0 && isVideoPlaying) {
            isVideoPlaying = false
            nextPageTimer()
          }
        }
      })
    }
//    binding?.introViewpager?.setPageTransformer(ViewPager2Transformation())
    binding?.btnCreate?.setOnClickListener {
      if (RootUtil.isDeviceRooted.not()) {
//    navigator?.startActivity(AccountNotFoundActivity::class.java, args = Bundle().apply { putString(IntentConstant.EXTRA_PHONE_NUMBER.name, "8097789896") })
        WebEngageController.trackEvent(PS_INTRO_SCREEN_START, GET_START_CLICKED, NO_EVENT_VALUE)
        startActivity(Intent(this@IntroActivity, MobileVerificationActivity::class.java))
      } else {
        dialogRootError()
      }
    }
  }

  private fun dialogRootError() {
    AlertDialog.Builder(ContextThemeWrapper(this, R.style.CustomAlertDialogTheme))
      .setCancelable(false)
      .setTitle("Boost 360 can't be used on this device!")
      .setMessage("Sorry, your device isn't passing JioOnline security checks. This may be because your device is rooted or is running an uncertified or custom OS build.")
      .setPositiveButton("Close") { dialog: DialogInterface, _: Int ->
        dialog.dismiss()
      }.show()
  }

  private fun setNextPage() {
    binding?.introViewpager?.currentItem = binding?.introViewpager?.currentItem ?: 0 + 1
  }

  fun slideNextPage() {
    binding?.introViewpager?.currentItem = binding?.introViewpager?.currentItem!! + 1

  }

  private fun nextPageTimer() {
    handler.postDelayed(nextRunnable, 3000)
  }
}