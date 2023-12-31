package com.boost.presignin.ui.newOnboarding

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.boost.presignin.BuildConfig
import com.boost.presignin.R
import com.boost.presignin.adapter.IntroNewAdapter
import com.boost.presignin.base.AppBaseFragment
import com.boost.presignin.constant.FragmentType
import com.boost.presignin.constant.IntentConstant
import com.boost.presignin.databinding.FragmentIntroSlideShowBinding
import com.boost.presignin.helper.WebEngageController
import com.boost.presignin.model.newOnboarding.IntroItemNew
import com.boost.presignin.ui.intro.CircularViewPagerHandler
import com.boost.presignin.ui.intro.dialogRootError
import com.boost.presignin.ui.login.LoginActivity
import com.framework.models.BaseViewModel
import com.framework.pref.APPLICATION_JIO_ID
import com.framework.utils.DeviceVerificationUtils
import com.framework.utils.RootUtil
import com.framework.webengageconstant.GET_START_CLICKED
import com.framework.webengageconstant.NO_EVENT_VALUE
import com.framework.webengageconstant.PS_INTRO_SCREEN_START

class IntroSlideShowFragment : AppBaseFragment<FragmentIntroSlideShowBinding, BaseViewModel>() {

  private var myState = 0
  private var currentPosition = 0
  private lateinit var introItems: ArrayList<IntroItemNew>
  private val handler = Handler(Looper.getMainLooper())
  private  val TAG = "IntroSlideShowFragment"

  private val nextRunnable = Runnable {
    binding?.viewpagerIntro?.post {
      changePageOnAnimationEnd()
      nextPageTimer()
    }
  }

  companion object {
    @JvmStatic
    fun newInstance(bundle: Bundle? = null): IntroSlideShowFragment {
      val fragment = IntroSlideShowFragment()
      fragment.arguments = bundle
      return fragment
    }
  }

  override fun getLayout(): Int {
    return R.layout.fragment_intro_slide_show
  }

  override fun getViewModelClass(): Class<BaseViewModel> {
    return BaseViewModel::class.java
  }

  override fun onCreateView() {
    setOnListeners()
    initUI()
  }

  private fun initUI() {
    introItems = IntroItemNew().getData(baseActivity)
    binding.viewpagerIntro.apply {
      adapter = IntroNewAdapter(childFragmentManager, lifecycle, introItems) { setNextPage(it) }
      orientation = ViewPager2.ORIENTATION_HORIZONTAL
      if (!BuildConfig.FLAVOR.equals("partone") || !BuildConfig.FLAVOR.equals("jioonline")) {
        binding.introIndicatorNew.selectedDotColor = getColor(R.color.buttonTint)
      }
      binding.introIndicatorNew.setViewPager2(this)
      this.offscreenPageLimit = 1
      this.registerOnPageChangeCallback(object : CircularViewPagerHandler(this) {
        override fun onPageSelected(position: Int) {
          currentPosition = position
          binding?.consWrapperIntro?.setBackgroundColor(ContextCompat.getColor(baseActivity, IntroItemNew().getData(baseActivity)[position].slideBackgroundColor ?: 0))
          super.onPageSelected(position)
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
          if (myState == ViewPager2.SCROLL_STATE_DRAGGING && currentPosition == position && currentPosition == 0) currentItem = 3
          else if (myState == ViewPager2.SCROLL_STATE_DRAGGING && currentPosition == position && currentPosition == 3) currentItem = 0
          super.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }

        override fun onPageScrollStateChanged(state: Int) {
          myState = state
          super.onPageScrollStateChanged(state)
        }
      })
    }
  }

  private fun setNextPage(pos: Int?) {
    if (currentPosition == pos) changePageOnAnimationEnd()
  }


  private fun setOnListeners() {

    binding.btnGetStarted.setOnClickListener {
      showProgress()
      DeviceVerificationUtils.isDeviceTrustable(requireActivity()){
        hideProgress()
        if (it) {
          WebEngageController.trackEvent(PS_INTRO_SCREEN_START, GET_START_CLICKED, NO_EVENT_VALUE)
          if (baseActivity.packageName.equals(APPLICATION_JIO_ID, ignoreCase = true).not()) {
            startFragmentFromNewOnBoardingActivity(
              activity = baseActivity, type = FragmentType.ENTER_PHONE_FRAGMENT,
              bundle = Bundle().apply { putString(IntentConstant.EXTRA_PHONE_NUMBER.name, "") }, clearTop = false
            )
          } else {
            startActivity(Intent(baseActivity, LoginActivity::class.java))
          }
        } else baseActivity.dialogRootError(requireContext())
      }

    }
  }

  /**
   * Strict Note: For Development purposes only (To Bypass Login > OTP Flow for testing)
   * @param enteredPhone : Enter any matching 10 digit indian number to test
   * */
  private fun moveToWelcomeScreen(enteredPhone: String?) {
    startFragmentFromNewOnBoardingActivity(
      activity = baseActivity, type = FragmentType.WELCOME_FRAGMENT,
      bundle = Bundle().apply {
        putString(IntentConstant.EXTRA_PHONE_NUMBER.name, enteredPhone)
        putBoolean(IntentConstant.WHATSAPP_CONSENT_FLAG.name, false)
      }
    )
  }


  private fun nextPageTimer() {
    handler.postDelayed(nextRunnable, 5000)
  }

  private fun changePageOnAnimationEnd() {
    val lastPosition: Int? = binding?.viewpagerIntro?.adapter?.itemCount?.minus(1)
    val mCurrentPosition = binding?.viewpagerIntro?.currentItem ?: 0
    val isLast = (mCurrentPosition == lastPosition)
    if (mCurrentPosition < 3){
      val setItemPosition = mCurrentPosition + 1
      binding?.viewpagerIntro?.currentItem = setItemPosition
    }else{
      val intent = Intent(baseActivity, NewOnBoardingContainerActivity::class.java)
      intent.setFragmentType(FragmentType.LOADING_ANIMATION_DASHBOARD_FRAGMENT)
      requireActivity().startActivity(intent)
    }
  }

}