package com.festive.poster.ui.promoUpdates

import android.graphics.BlendMode
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.festive.poster.R
import com.festive.poster.base.AppBaseFragment
import com.festive.poster.constant.PreferenceConstant
import com.festive.poster.databinding.FragmentPromoLandingPageBinding
import com.festive.poster.models.promoModele.SocialConnModel
import com.festive.poster.utils.WebEngageController
import com.festive.poster.utils.launchPostNewUpdate
import com.framework.extensions.gone
import com.framework.models.BaseViewModel
import com.framework.utils.*
import com.framework.views.BlankFragment
import com.framework.views.customViews.CustomImageView
import com.framework.views.customViews.CustomTextView
import com.framework.webengageconstant.*
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import kotlin.math.abs

class PromoLandingPageFragment : AppBaseFragment<FragmentPromoLandingPageBinding, BaseViewModel>() {

  var currentPage: Int = 0
  var isCreateClick: Boolean = false
  var isIsTodayTabClicked: Boolean = false

  enum class ToolTipType {
    FOR_TODAY, CREATE, READY_MADE, CREATE_NEW
  }

  override fun getLayout(): Int {
    return R.layout.fragment_promo_landing_page
  }

  override fun getViewModelClass(): Class<BaseViewModel> {
    return BaseViewModel::class.java
  }

  companion object {
    fun newInstance(bundle: Bundle = Bundle()): PromoLandingPageFragment {
      val fragment = PromoLandingPageFragment()
      fragment.arguments = bundle
      return fragment
    }


  }

  override fun onCreateView() {
    super.onCreateView()
    setupView()
    WebEngageController.trackEvent(Promotional_Update_Home_Loaded)
    setupViewPager()
    if (PreferencesUtils.instance.getData(PreferencesKey.UPDATE_STUDIO_FIRST_TIME.name, true)) {
      PreferencesUtils.instance.saveData(PreferencesKey.UPDATE_STUDIO_FIRST_TIME.name, false)
      setupTabBaloons(ToolTipType.READY_MADE)
    }
  }

  private fun setupView() {
    binding.appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
      if (abs(verticalOffset) - appBarLayout.totalScrollRange == 0) {
        //  Collapsed
        binding.tabParent.background = ContextCompat.getDrawable(
          requireActivity(), R.drawable.white_rect_background
        )
      } else {
        binding.tabParent.background = ContextCompat.getDrawable(
          requireActivity(), R.drawable.top_round_corner
        )
        //Expanded
      }
    })
    val soicalConnList = arrayListOf(
      SocialConnModel(
        content = "1000+ people are on twitter"
      ), SocialConnModel(
        content = "1000+ people are on facebook"
      )
    )

    /* val socialconnadapter = AppBaseRecyclerViewAdapter(requireActivity() as BaseActivity<*, *>,soicalConnList)
     binding?.vpSocialConn?.adapter = socialconnadapter
     binding?.ivLeftArrow?.setOnClickListener {
         binding?.vpSocialConn?.postDelayed(Runnable {
             binding?.vpSocialConn?.currentItem = binding?.vpSocialConn?.currentItem?:1 - 1;

         }, 100)
     }
     binding?.ivRightArrow?.setOnClickListener {
         binding?.vpSocialConn?.postDelayed(Runnable {
             binding?.vpSocialConn?.currentItem = binding?.vpSocialConn?.currentItem?:0 + 1;

         }, 100)
     }*/
    binding.fabBrowseAll!!.setOnClickListener {
      WebEngageController.trackEvent(PROMOTIONAL_UPDATE_CATEGORY_CLICK)
      addFragment(R.id.container, BrowseCategoriesFragment.newInstance(), true, true)
    }
  }

  private fun setupViewPager() {
    binding.tabLayout.tabGravity = TabLayout.GRAVITY_FILL
    binding.tabLayout.tabMode = TabLayout.MODE_FIXED
    val fragmentList = arrayListOf(TodaysPickFragment.newInstance(), BlankFragment())
    val viewPagerAdapter = TabAdapter(fragmentList, this)
    binding.viewPager.apply {
      isUserInputEnabled = false
      adapter = viewPagerAdapter
      offscreenPageLimit = 1
      registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
          when (position) {
            0 -> {
              currentPage = 0
            }
            1 -> {
              isCreateClick = true
              WebEngageController.trackEvent(Promotional_Update_Create_Update_Click)
              launchPostNewUpdate(requireActivity())
            }
          }
        }
      })
    }
    binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
      override fun onTabSelected(tab: TabLayout.Tab?) {
        //  changeTabFont(tab,Typeface.BOLD)
        when(tab?.position){
          0 -> {
            if (isIsTodayTabClicked){
              WebEngageController.trackEvent(Update_studio_today_tab_click)
            }
          }
          1 -> WebEngageController.trackEvent(Update_studio_Create_custom_update_click)
        }
        val tabIconColor: Int = ContextCompat.getColor(requireActivity(), R.color.color_studio_tab_selected)
        if (tab?.position == 1) {
          tab.icon = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_create_new_update_active)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          tab?.icon?.setColorFilterApiQ(tabIconColor, BlendMode.SRC_IN)
        } else {
          tab?.icon?.setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN)
        }
      }

      override fun onTabUnselected(tab: TabLayout.Tab?) {
        // changeTabFont(tab,Typeface.NORMAL)
        val tabIconColor: Int = ContextCompat.getColor(requireActivity(), R.color.colorB3B3B3)
        if (tab?.position == 1) {
          tab.icon = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_create_new_update_inactive)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          tab?.icon?.setColorFilterApiQ(tabIconColor, BlendMode.SRC_IN)
        } else {
          tab?.icon?.setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN)
        }
      }

      override fun onTabReselected(tab: TabLayout.Tab?) {
      }
    })
    // New way of interaction with TabLayout and page title setting
    TabLayoutMediator(binding.tabLayout, binding.viewPager) { currentTab, currentPosition ->
      currentTab.icon = when (currentPosition) {
        0 -> AppCompatResources.getDrawable(requireActivity(), R.drawable.ic_ready_made_update)
        1 -> AppCompatResources.getDrawable(requireActivity(), R.drawable.ic_create_new_update_inactive)
        else -> null
      }
      currentTab.text = when (currentPosition) {
        0 -> getString(R.string.ready_made)
        1 -> getString(R.string.create_new)
        else -> ""
      }
    }.attach()

    if (PreferencesUtils.instance.getData(PreferenceConstant.FIRST_LAUNCH_PROMO, true)) {
      //  setupTabBaloons(ToolTipType.READY_MADE)
      PreferencesUtils.instance.saveData(PreferenceConstant.FIRST_LAUNCH_PROMO, false)
    }

  }

  private fun changeTabFont(tab: TabLayout.Tab?, style: Int) {
    for (i in 0 until tab?.view?.childCount!!) {
      val tabViewChild = tab.view.getChildAt(i)
      if (tabViewChild is TextView) {
        tabViewChild.setTypeface(null, style)
      }
    }
  }

  private fun setupTabBaloons(type: ToolTipType) {
    val marginLeft = if (type == ToolTipType.READY_MADE) 16 else 0
    val marginRight = if (type == ToolTipType.CREATE_NEW) 16 else 0

    val arrowPos = when (type) {
      ToolTipType.READY_MADE -> 0.2F
      ToolTipType.CREATE_NEW -> 0.8F
      else -> 0.5F
    }

    val balloon =
      Balloon.Builder(requireActivity()).setLayout(R.layout.ballon_promo_tabs).setArrowSize(10).setArrowColor(ContextCompat.getColor(requireActivity(), R.color.black_4a4a4a)).setArrowOrientation(ArrowOrientation.TOP).setWidth(BalloonSizeSpec.WRAP).setHeight(BalloonSizeSpec.WRAP).setCornerRadius(8F)
        .setWidthRatio(1F).setArrowPosition(arrowPos).setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.black)).setBalloonAnimation(BalloonAnimation.CIRCULAR).setLifecycleOwner(viewLifecycleOwner).build()
    val text = balloon.getContentView().findViewById<CustomTextView>(R.id.tv_msg)
    val close = balloon.getContentView().findViewById<CustomImageView>(R.id.iv_close)
    val icon = balloon.getContentView().findViewById<CustomImageView>(R.id.iv_icon)

    when (type) {
      ToolTipType.READY_MADE -> {
        text.text = spanBoldNdColor(
          getString(R.string.premium_updates_custom_designed_for_you),
          R.color.colorPrimary,
          "Premium updates"
        )
        binding.tabLayout.getTabAt(0)?.view?.postDelayed({
          balloon.showAlignBottom(
            binding.tabLayout.getTabAt(0)!!.view, 0, 0
          )
        }, 500)
        balloon.setOnBalloonDismissListener {
          setupTabBaloons(ToolTipType.CREATE_NEW)
        }
        close.setOnClickListener {
          balloon.dismiss()
          setupTabBaloons(ToolTipType.CREATE_NEW)
        }
      }
      ToolTipType.CREATE_NEW -> {
        icon.gone()
        text.text = spanBoldNdColor(
          getString(R.string.free_updates_tap_on_create_to_post_like_before), R.color.green_78AF00, "FREE updates:"
        )
        binding.tabLayout.getTabAt(1)?.view?.postDelayed(Runnable {
          balloon.showAlignBottom(binding.tabLayout.getTabAt(1)!!.view)
        }, 500)

        close.setOnClickListener {
          balloon.dismiss()
        }
      }
    }
  }


  override fun onResume() {
    super.onResume()
    binding.viewPager.currentItem = currentPage
    isCreateClick = false
  }
}