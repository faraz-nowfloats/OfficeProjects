package com.dashboard.controller.ui.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.viewpager2.widget.ViewPager2
import com.appservice.constant.IntentConstant
import com.appservice.staffs.ui.startStaffFragmentActivity
import com.dashboard.R
import com.dashboard.base.AppBaseFragment
import com.dashboard.constant.FragmentType
import com.dashboard.constant.PreferenceConstant.CHANNEL_SHARE_URL
import com.dashboard.constant.RecyclerViewActionType
import com.dashboard.constant.RecyclerViewItemType
import com.dashboard.controller.DashboardActivity
import com.dashboard.controller.getDomainName
import com.dashboard.controller.startFragmentDashboardActivity
import com.dashboard.controller.ui.dialog.ProgressDashboardDialog
import com.dashboard.databinding.FragmentDashboardBinding
import com.dashboard.model.*
import com.dashboard.model.live.addOns.ManageBusinessData
import com.dashboard.model.live.addOns.ManageBusinessDataResponse
import com.dashboard.model.live.dashboardBanner.*
import com.dashboard.model.live.drScore.DrScoreSetupData
import com.dashboard.model.live.drScore.DrScoreUiDataResponse
import com.dashboard.model.live.drScore.getDrScoreData
import com.dashboard.model.live.quickAction.QuickActionItem
import com.dashboard.model.live.quickAction.QuickActionResponse
import com.dashboard.model.live.shareUser.ShareUserDetailResponse
import com.dashboard.pref.*
import com.dashboard.pref.Key_Preferences.GET_FP_DETAILS_BUSINESS_NAME
import com.dashboard.pref.Key_Preferences.GET_FP_DETAILS_LogoUrl
import com.dashboard.recyclerView.AppBaseRecyclerViewAdapter
import com.dashboard.recyclerView.BaseRecyclerViewItem
import com.dashboard.recyclerView.RecyclerItemClickListener
import com.dashboard.utils.*
import com.dashboard.viewmodel.DashboardViewModel
import com.framework.extensions.gone
import com.framework.extensions.observeOnce
import com.framework.extensions.visible
import com.framework.glide.util.glideLoad
import com.framework.models.firestore.FirestoreManager
import com.framework.models.firestore.FirestoreManager.getDrScoreData
import com.framework.models.firestore.FirestoreManager.readDrScoreDocument
import com.framework.utils.*
import com.framework.views.dotsindicator.OffsetPageTransformer
import com.inventoryorder.model.mapDetail.TOTAL_MAP_VISIT
import com.inventoryorder.model.mapDetail.VisitsModelResponse
import com.inventoryorder.model.ordersummary.OrderSummaryModel
import com.inventoryorder.model.ordersummary.SELLER_BUSINESS_REPORT
import com.inventoryorder.model.ordersummary.TOTAL_SELLER_SUMMARY
import com.inventoryorder.model.summary.*
import com.inventoryorder.model.summary.request.FilterBy
import com.inventoryorder.model.summary.request.QueryObject
import com.inventoryorder.model.summary.request.SellerSummaryRequest
import com.inventoryorder.model.summaryCall.CALL_BUSINESS_REPORT
import com.inventoryorder.model.summaryCall.CallSummaryResponse
import com.inventoryorder.rest.response.OrderSummaryResponse
import com.onboarding.nowfloats.model.channel.*
import com.onboarding.nowfloats.model.channel.request.ChannelAccessToken
import com.onboarding.nowfloats.rest.response.channel.ChannelWhatsappResponse
import com.onboarding.nowfloats.rest.response.channel.ChannelsAccessTokenResponse
import com.onboarding.nowfloats.ui.updateChannel.digitalChannel.LocalSessionModel
import com.onboarding.nowfloats.ui.updateChannel.digitalChannel.VisitingCardSheet
import com.onboarding.nowfloats.ui.webview.WebViewBottomDialog
import java.util.*
import kotlin.collections.ArrayList

const val IS_FIRST_LOAD = "isFirsLoad"

class DashboardFragment : AppBaseFragment<FragmentDashboardBinding, DashboardViewModel>(), RecyclerItemClickListener {

  private var session: UserSessionManager? = null
  private var adapterBusinessContent: AppBaseRecyclerViewAdapter<DrScoreSetupData>? = null
  private var adapterPagerBusinessUpdate: AppBaseRecyclerViewAdapter<BusinessSetupHighData>? = null
  private var adapterRoi: AppBaseRecyclerViewAdapter<RoiSummaryData>? = null
  private var adapterGrowth: AppBaseRecyclerViewAdapter<GrowthStatsData>? = null
  private var adapterMarketBanner: AppBaseRecyclerViewAdapter<DashboardMarketplaceBanner>? = null
  private var adapterAcademy: AppBaseRecyclerViewAdapter<DashboardAcademyBanner>? = null
  private var adapterQuickAction: AppBaseRecyclerViewAdapter<QuickActionItem>? = null
  private var adapterBusinessData: AppBaseRecyclerViewAdapter<ManageBusinessData>? = null
  private var ctaFileLink: String? = null
  private var mCurrentPage: Int = 0

  private var handler = Handler()
  private var runnable = Runnable { showSimmerDrScore(isSimmer = false, isRetry = true) }

  override fun getLayout(): Int {
    return R.layout.fragment_dashboard
  }

  override fun getViewModelClass(): Class<DashboardViewModel> {
    return DashboardViewModel::class.java
  }

  override fun onCreateView() {
    super.onCreateView()
    if (isFirstLoad().not() || (baseActivity as? DashboardActivity)?.isLoadShimmer == true) showSimmer(true)
    session = UserSessionManager(baseActivity)
    setOnClickListener(binding?.btnBusinessLogo, binding?.btnNotofication, binding?.filterBusinessReport, binding?.filterWebsiteReport,
        binding?.btnVisitingCard, binding?.txtDomainName, binding?.btnShowDigitalScore, binding?.retryDrScore)
    val versionName: String = baseActivity.packageManager.getPackageInfo(baseActivity.packageName, 0).versionName
    binding?.txtVersion?.text = "Version $versionName"
    getAllDashboardSummary()
    getPremiumBanner()
    getChannelAccessToken()
    WebEngageController.trackEvent("Dashboard Home Page", "pageview", session?.fpTag)
  }

  override fun onResume() {
    super.onResume()
    if (getDrScoreData()?.drs_segment.isNullOrEmpty()) readDrScoreDocument()
    refreshData()
  }

  private fun getPremiumBanner() {
    setDataMarketBanner(getMarketPlaceBanners() ?: ArrayList())
    setDataRiaAcademy(getAcademyBanners() ?: ArrayList())
    viewModel?.getUpgradeDashboardBanner()?.observeOnce(viewLifecycleOwner, {
      val response = it as? DashboardPremiumBannerResponse
      if (response?.isSuccess() == true && response.data.isNullOrEmpty().not()) {
        val data = response.data?.get(0)
        if (data?.academyBanners.isNullOrEmpty().not()) {
          saveDataAcademy(data?.academyBanners!!)
          setDataRiaAcademy(data.academyBanners!!)
        }
        if (data?.marketplaceBanners.isNullOrEmpty().not()) {
          val marketBannerFilter = (data?.marketplaceBanners ?: ArrayList()).marketBannerFilter(session)
          saveDataMarketPlace(marketBannerFilter)
          setDataMarketBanner(marketBannerFilter)
        }
      }
    })
  }

  private fun refreshData() {
    setUserData()
    setBusinessManageTask()
    getNotificationCount()
    setSummaryAndDrScore((baseActivity as? DashboardActivity)?.isLoadShimmer ?: true)
  }

  private fun setSummaryAndDrScore(isLoadingShimmerDr: Boolean) {
    (baseActivity as? DashboardActivity)?.setPercentageData(getDrScoreData()?.getDrsTotal() ?: 0)
    refreshAllDashboardSummary()
    setDrScoreData(isLoadingShimmerDr)
  }

  private fun setDrScoreData(isLoadingShimmerDr: Boolean) {
    handler.removeCallbacks(runnable)
    viewModel?.getDrScoreUi(baseActivity)?.observeOnce(viewLifecycleOwner, {
      val response = it as? DrScoreUiDataResponse
      val drScoreData = getDrScoreData()
      val isHighDrScore = drScoreData != null && (drScoreData.getDrsTotal() >= 80)
      val drScoreSetupList = response?.data?.let { it1 -> drScoreData?.getDrScoreData(it1) }
      if (response?.isSuccess() == true && drScoreSetupList.isNullOrEmpty().not()) {
        if (isHighDrScore.not()) {
          binding?.highReadinessScoreView?.gone()
          binding?.lowReadinessScoreView?.visible()
          binding?.txtReadinessScore?.text = "${drScoreData!!.getDrsTotal()}"
          binding?.progressScore?.progress = drScoreData.getDrsTotal()
          drScoreSetupList?.map { it1 -> it1.recyclerViewItemType = RecyclerViewItemType.BUSINESS_SETUP_ITEM_VIEW.getLayout() }
          binding?.pagerBusinessSetupLow?.apply {
            binding?.motionOne?.transitionToStart()
            adapterBusinessContent = AppBaseRecyclerViewAdapter(baseActivity, drScoreSetupList!!, this@DashboardFragment)
            offscreenPageLimit = 3
            adapter = adapterBusinessContent
            binding?.dotIndicator?.setViewPager2(this)
            setPageTransformer { page, position -> OffsetPageTransformer().transformPage(page, position) }
            postInvalidateOnAnimation()
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
              override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mCurrentPage = position
              }

              override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                if (position == mCurrentPage) {
                  if (positionOffset > 0.5) setPage(position + 1) else setPage(position)
                } else {
                  if (positionOffset < 0.5) setPage(position) else setPage(position + 1)
                }
              }
            })
          }
          binding?.motionOne?.loadLayoutDescription(R.xml.fragment_dashboard_scene)
          binding?.motionOne?.transitionToStart()
          baseActivity.setGifAnim(binding?.missingDetailsGif!!, R.raw.ic_missing_setup_gif_d, R.drawable.ic_custom_page_d)
          baseActivity.setGifAnim(binding?.arrowLeftGif!!, R.raw.ic_arrow_left_gif_d, R.drawable.ic_arrow_right_14_d)
        } else {
          binding?.highReadinessScoreView?.visible()
          binding?.lowReadinessScoreView?.gone()
        }
        showSimmerDrScore(false)
      } else showSimmerDrScore(isLoadingShimmerDr, isLoadingShimmerDr.not())
    })
  }

  private fun setPage(position: Int) {
    binding?.motionOne?.loadLayoutDescription(takeIf { position == 0 }?.let { R.xml.fragment_dashboard_scene } ?: 0)
  }

  private fun setBusinessManageTask() {
    binding?.recommendedTask?.apply {
      viewModel?.getQuickActionData(baseActivity)?.observeOnce(viewLifecycleOwner, {
        val response = it as? QuickActionResponse
        val listAction = response?.data?.firstOrNull { it1 -> it1.type.equals(session?.fP_AppExperienceCode, ignoreCase = true) }
        if (response?.isSuccess() == true && listAction?.actionItem.isNullOrEmpty().not()) {
          if (adapterQuickAction == null) {
            rvQuickAction.apply {
              adapterQuickAction = AppBaseRecyclerViewAdapter(baseActivity, listAction?.actionItem!!, this@DashboardFragment)
              adapter = adapterQuickAction
            }
          } else adapterQuickAction?.notify(listAction?.actionItem!!)
        } else showShortToast(baseActivity.getString(R.string.quick_action_data_error))
      })

    }
    binding?.manageBusiness?.apply {
      title.text = if (getRoiSummaryType(session?.fP_AppExperienceCode) == "DOC") baseActivity.getString(R.string.manage_your_clinic) else baseActivity.getString(R.string.manage_your_business)

      viewModel?.getBoostAddOnsTop(baseActivity)?.observeOnce(viewLifecycleOwner, {
        val response = it as? ManageBusinessDataResponse
        val dataAction = response?.data?.firstOrNull { it1 -> it1.type.equals(session?.fP_AppExperienceCode, ignoreCase = true) }
        if (dataAction?.actionItem.isNullOrEmpty().not()) {
          dataAction!!.actionItem!!.map { it1 -> if (it1.premiumCode.isNullOrEmpty().not() && session.checkIsPremiumUnlock(it1.premiumCode).not()) it1.isLock = true }
          if (adapterBusinessData == null) {
            rvManageBusiness.apply {
              adapterBusinessData = AppBaseRecyclerViewAdapter(baseActivity, dataAction.actionItem!!, this@DashboardFragment)
              adapter = adapterBusinessData
            }
          } else adapterBusinessData?.notify(dataAction.actionItem!!)
        } else showShortToast(baseActivity.getString(R.string.manage_business_not_found))
      })
      btnShowAll.setOnClickListener {
        WebEngageController.trackEvent("Business Add-ons Page", "Add-ons", session?.fpTag)
        startFragmentDashboardActivity(FragmentType.ALL_BOOST_ADD_ONS)
      }
    }
  }

  private fun refreshAllDashboardSummary() {
    setBusinessSummary(getDrScoreData()?.getDrsTotal() ?: 0, OrderSummaryModel().getTotalOrder(TOTAL_SELLER_SUMMARY) ?: "0",
        SummaryEntity().getUserSummary(USER_BUSINESS_SUMMARY))
    setRoiBusinessReport(OrderSummaryModel().getSellerSummary(SELLER_BUSINESS_REPORT), SummaryEntity().getTotalUserMessage(TOTAL_USER_MESSAGE).toString(),
        CallSummaryResponse().getCallSummary(CALL_BUSINESS_REPORT) ?: "0")
    setWebsiteReport(SummaryEntity().getUserSummary(USER_WEBSITE_REPORT), VisitsModelResponse().getTotalOMapVisit(TOTAL_MAP_VISIT) ?: "0", null)
  }

  private fun getAllDashboardSummary() {
    apiBusinessSummary()
    var businessFilter = FilterDateModel().getDateFilter(FILTER_BUSINESS_REPORT)
    if (businessFilter == null) {
      businessFilter = FilterDateModel().getFilterDate().last()
      businessFilter.saveData(FILTER_BUSINESS_REPORT)
    }
    apiRoiBusinessReport(businessFilter)
    binding?.filterBusinessReport?.text = businessFilter.title
    var websiteFilter = FilterDateModel().getDateFilter(FILTER_WEBSITE_REPORT)
    if (websiteFilter == null) {
      websiteFilter = FilterDateModel().getFilterDate().last()
      websiteFilter.saveData(FILTER_WEBSITE_REPORT)
    }
    apiWebsiteReport(websiteFilter)
    binding?.filterWebsiteReport?.text = websiteFilter.title
  }

  private fun apiBusinessSummary() {
    viewModel?.getSellerSummaryV2_5(clientId_ORDER, session?.fpTag, getRequestSellerSummary(null))?.observeOnce(viewLifecycleOwner, {
      val response1 = it as? OrderSummaryResponse
      if (response1?.isSuccess() == true && response1.Data != null) response1.Data?.saveTotalOrder(TOTAL_SELLER_SUMMARY)
      val scope = if (session?.iSEnterprise == "true") "1" else "0"
      viewModel?.getUserSummary(clientId, session?.fPParentId, scope)?.observeOnce(viewLifecycleOwner, { it1 ->
        val response2 = it1 as? UserSummaryResponse
        response2?.getSummary()?.saveData(USER_BUSINESS_SUMMARY)
        setBusinessSummary(getDrScoreData()?.getDrsTotal() ?: 0, response1?.Data?.getTotalOrders() ?: "0", response2?.getSummary())
      })
    })
  }

  private fun setBusinessSummary(drTotal: Int, totalOrder: String, summary: SummaryEntity?) {
    val data = BusinessSetupHighData().getData(drTotal, summary?.getNoOfUniqueViews() ?: "0", totalOrder, getCustomerTypeFromServiceCode(session?.fP_AppExperienceCode),
        summary?.getNoOfMessages() ?: "0")
    data.map { it.recyclerViewItemType = RecyclerViewItemType.BUSINESS_SETUP_HIGH_ITEM_VIEW.getLayout() }
    if (adapterPagerBusinessUpdate == null) {
      binding?.pagerBusinessSetupHigh?.apply {
        adapterPagerBusinessUpdate = AppBaseRecyclerViewAdapter(baseActivity, data, this@DashboardFragment)
        offscreenPageLimit = 3
        adapter = adapterPagerBusinessUpdate
        binding?.dotIndicatorBusinessHigh?.setViewPager2(this)
        setPageTransformer { page, position -> OffsetPageTransformer().transformPage(page, position) }
      }
    } else adapterPagerBusinessUpdate?.notify(data)
  }

  private fun apiRoiBusinessReport(filterDate: FilterDateModel, isLoader: Boolean = false) {
    if (isLoader) showProgress()
    viewModel?.getSellerSummaryV2_5(clientId_ORDER, session?.fpTag, getRequestSellerSummary(filterDate))?.observeOnce(viewLifecycleOwner, {
      val response1 = it as? OrderSummaryResponse
      if (response1?.isSuccess() == true && response1.Data != null) response1.Data?.saveData(SELLER_BUSINESS_REPORT)
      val scope = if (session?.iSEnterprise == "true") "1" else "0"
      viewModel?.getUserSummary(clientId, session?.fPParentId, scope, filterDate.startDate, filterDate.endDate)?.observeOnce(viewLifecycleOwner, { it1 ->
        val response2 = it1 as? UserSummaryResponse
        response2?.getSummary()?.saveTotalMessage(TOTAL_USER_MESSAGE)
        val identifierType = if (session?.iSEnterprise == "true") "MULTI" else "SINGLE"
        viewModel?.getUserCallSummary(clientId, session?.fPParentId, identifierType, filterDate.startDate, filterDate.endDate)?.observeOnce(viewLifecycleOwner, { it2 ->
          val response3 = it2 as? CallSummaryResponse
          response3?.saveData(CALL_BUSINESS_REPORT)
          setRoiBusinessReport(response1?.Data, response2?.getSummary()?.getNoOfMessages() ?: "0", response3?.getTotalCalls() ?: "0")
          if (isLoader) hideProgress()
        })
      })
    })
  }

  private fun setRoiBusinessReport(sellerOrder: OrderSummaryModel?, noOfMessage: String, totlaCalls: String) {
    val roiData = RoiSummaryData().getData(noOfMessage, totlaCalls, sellerOrder, getRoiSummaryType(session?.fP_AppExperienceCode))
    roiData.map { it.recyclerViewItemType = RecyclerViewItemType.ROI_SUMMARY_ITEM_VIEW.getLayout() }
    if (adapterRoi == null) {
      binding?.rvRoiSummary?.apply {
        adapterRoi = AppBaseRecyclerViewAdapter(baseActivity, roiData, this@DashboardFragment)
        adapter = adapterRoi
      }
    } else adapterRoi?.notify(roiData)
  }

  private fun apiWebsiteReport(filterDate: FilterDateModel, isLoader: Boolean = false) {
    if (isLoader) showProgress()
    val scope = if (session?.iSEnterprise == "true") "1" else "0"
    viewModel?.getUserSummary(clientId, session?.fPParentId, scope, filterDate.startDate, filterDate.endDate)?.observeOnce(viewLifecycleOwner, { it1 ->
      val response1 = it1 as? UserSummaryResponse
      viewModel?.getSubscriberCount(session?.fpTag, clientId, filterDate.startDate, filterDate.endDate)?.observeOnce(viewLifecycleOwner, { it2 ->
        val subscriberCount = (it2.anyResponse as? Double)?.toInt() ?: 0
        val summary = response1?.getSummary()
        summary?.noOfSubscribers = subscriberCount
        summary?.saveData(USER_WEBSITE_REPORT)
        viewModel?.getMapVisits(session?.fpTag, session?.getRequestMap(filterDate.startDate ?: "", filterDate.endDate ?: ""))?.observeOnce(viewLifecycleOwner, { it3 ->
          val response3 = it3 as? VisitsModelResponse
          response3?.saveMapVisit(TOTAL_MAP_VISIT)
          viewModel?.getSearchAnalytics(session?.fPID, filterDate.startDate, filterDate.endDate)?.observeOnce(viewLifecycleOwner, {
            val countSearch = (it?.anyResponse as? String)?.toIntOrNull()
            setWebsiteReport(summary, response3?.getTotalCount() ?: "0", countSearch)
            if (isLoader) hideProgress()
            (baseActivity as? DashboardActivity)?.isLoadShimmer = false
            showSimmer(false)
            saveFirstLoad()
          })
        })
      })
    })
  }


  private fun setWebsiteReport(summary: SummaryEntity?, mapVisitCount: String, countSearch: Int?) {
    val countS = countSearch?.toString() ?: getNumberFormat((session?.searchCount?.toIntOrNull() ?: 0).toString())
    val growthStatsList = GrowthStatsData().getData(summary, mapVisitCount, countS)
    growthStatsList.map { it.recyclerViewItemType = RecyclerViewItemType.GROWTH_STATE_ITEM_VIEW.getLayout() }
    if (adapterGrowth == null) {
      binding?.rvGrowthState?.apply {
        adapterGrowth = AppBaseRecyclerViewAdapter(baseActivity, growthStatsList, this@DashboardFragment)
        adapter = adapterGrowth
      }
    } else adapterGrowth?.notify(growthStatsList)
  }

  private fun setUserData() {
    binding?.txtBusinessName?.text = session?.getFPDetails(GET_FP_DETAILS_BUSINESS_NAME)
    binding?.txtDomainName?.text = fromHtml("<u>${session!!.getDomainName()}</u>")
    var imageLogoUri = session?.getFPDetails(GET_FP_DETAILS_LogoUrl)
    if (imageLogoUri.isNullOrEmpty().not() && imageLogoUri!!.contains("http").not()) {
      imageLogoUri = BASE_IMAGE_URL + imageLogoUri
    }

    binding?.imgBusinessLogo?.let {
      if (imageLogoUri.isNullOrEmpty().not()) {
        baseActivity.glideLoad(mImageView = it, url = imageLogoUri!!, placeholder = R.drawable.gradient_white, isLoadBitmap = true)
      } else it.setImageResource(R.drawable.ic_add_logo_d)
    }
  }

  private fun setDataRiaAcademy(academyBanner: ArrayList<DashboardAcademyBanner>) {
    binding?.pagerRiaAcademy?.apply {
      if (academyBanner.isNotEmpty()) {
        academyBanner.map { it.recyclerViewItemType = RecyclerViewItemType.RIA_ACADEMY_ITEM_VIEW.getLayout() }
        binding?.riaAcademyView?.visible()
        if (adapterAcademy == null) {
          adapterAcademy = AppBaseRecyclerViewAdapter(baseActivity, academyBanner, this@DashboardFragment)
          offscreenPageLimit = 3
          adapter = adapterAcademy
          binding?.dotIndicatorAcademy?.setViewPager2(this)
          setPageTransformer { page, position -> OffsetPageTransformer().transformPage(page, position) }
        } else adapterAcademy?.notify(academyBanner)
      } else binding?.riaAcademyView?.gone()
    }
  }

  private fun getNotificationCount() {
    viewModel?.getNotificationCount(clientId, session?.fPID)?.observeOnce(viewLifecycleOwner, {
      val value = (it.anyResponse as? Double)?.toInt()
      if (it.isSuccess() && (value != null && value != 0)) {
        binding?.txtNotification?.visibility = View.VISIBLE
        binding?.txtNotification?.text = "$value+"
      } else binding?.txtNotification?.visibility = View.INVISIBLE
    })
  }

  private fun setDataMarketBanner(marketBannerFilter: ArrayList<DashboardMarketplaceBanner>) {
    binding?.pagerBoostPremium?.apply {
      if (marketBannerFilter.isNotEmpty()) {
        marketBannerFilter.map { it.recyclerViewItemType = RecyclerViewItemType.BOOST_PREMIUM_ITEM_VIEW.getLayout() }
        binding?.boostPremiumView?.visible()
        if (adapterMarketBanner == null) {
          adapterMarketBanner = AppBaseRecyclerViewAdapter(baseActivity, marketBannerFilter, this@DashboardFragment)
          offscreenPageLimit = 3
          adapter = adapterMarketBanner
          binding?.dotIndicatorPremium?.setViewPager2(this)
          setPageTransformer { page, position -> OffsetPageTransformer().transformPage(page, position) }
        } else adapterMarketBanner?.notify(marketBannerFilter)
      } else binding?.boostPremiumView?.gone()
    }
  }

  override fun onItemClick(position: Int, item: BaseRecyclerViewItem?, actionType: Int) {
    when (actionType) {
      RecyclerViewActionType.READING_SCORE_CLICK.ordinal -> {
        WebEngageController.trackEvent("SITE HEALTH Page", "SITE_HEALTH", session?.fpTag);
//        session?.let { baseActivity.startOldSiteMeter(it) }
        baseActivity.startReadinessScoreView(session, 0)
      }
      RecyclerViewActionType.BUSINESS_SETUP_SCORE_CLICK.ordinal -> baseActivity.startReadinessScoreView(session, position)
      RecyclerViewActionType.QUICK_ACTION_ITEM_CLICK.ordinal -> {
        val data = item as? QuickActionItem ?: return
        QuickActionItem.QuickActionType.from(data.quickActionType)?.let { quickActionClick(it) }
      }
      RecyclerViewActionType.BUSINESS_ADD_ONS_CLICK.ordinal -> {
        val data = item as? ManageBusinessData ?: return
        ManageBusinessData.BusinessType.fromName(data.businessType)?.let {
          com.dashboard.controller.ui.allAddOns.businessAddOnsClick(it, baseActivity, session)
        }
      }
      RecyclerViewActionType.BUSINESS_UPDATE_CLICK.ordinal -> {
        val data = item as? Specification ?: return
        BusinessSetupHighData.BusinessClickEvent.fromName(data.clickType)?.let { clickBusinessUpdate(it) }
      }
      RecyclerViewActionType.ROI_SUMMARY_CLICK.ordinal -> {
        val data = item as? RoiSummaryData ?: return
        RoiSummaryData.RoiType.fromName(data.type)?.let { clickRoiSummary(it) }
      }
      RecyclerViewActionType.GROWTH_STATS_CLICK.ordinal -> {
        val data = item as? GrowthStatsData ?: return
        GrowthStatsData.GrowthType.fromName(data.type)?.let { clickGrowthStats(it) }
      }
      RecyclerViewActionType.CHANNEL_ITEM_CLICK.ordinal -> {
        val data = (item as? ChannelData)?.channelData ?: return
        actionChannelClick(data)
      }
      RecyclerViewActionType.PROMO_BANNER_CLICK.ordinal -> {
        val data = item as? DashboardMarketplaceBanner ?: return
        if (data.ctaFeatureKey.isNullOrEmpty().not()) {
          session?.let {
            baseActivity.initiateAddonMarketplace(it, false, "", data.ctaFeatureKey)
          }
        }
      }
      RecyclerViewActionType.PROMO_BOOST_ACADEMY_CLICK.ordinal -> {
        val data = item as? DashboardAcademyBanner ?: return
        academyBannerBoostClick(data)
      }
    }
  }

  override fun onClick(v: View) {
    super.onClick(v)
    //TODO: Track the share_business_card_initiated even in Firebase & Webengage
    when (v) {
      binding?.filterBusinessReport -> bottomSheetFilter(BUSINESS_REPORT, FilterDateModel().getDateFilter(FILTER_BUSINESS_REPORT))
      binding?.filterWebsiteReport -> bottomSheetFilter(WEBSITE_REPORT, FilterDateModel().getDateFilter(FILTER_WEBSITE_REPORT))
      binding?.btnNotofication -> session?.let { baseActivity.startNotification(it) }
      binding?.btnBusinessLogo -> baseActivity.startBusinessLogo(session)
      binding?.btnShowDigitalScore -> baseActivity.startReadinessScoreView(session, 0)
      binding?.btnVisitingCard -> {
        val messageChannelUrl = PreferencesUtils.instance.getData(CHANNEL_SHARE_URL, "")
        if (messageChannelUrl.isNullOrEmpty().not()) visitingCardDetailText(messageChannelUrl)
        else getChannelAccessToken(true)
      }
      binding?.retryDrScore -> setSummaryAndDrScore(true)
      binding?.txtDomainName -> baseActivity.startWebViewPageLoad(session, session!!.getDomainName(false))
    }
  }

  private fun visitingCardDetailText(shareChannelText: String?) {
    viewModel?.getBoostVisitingMessage(baseActivity)?.observeOnce(viewLifecycleOwner, {
      val response = it as? ShareUserDetailResponse
      if (response?.isSuccess() == true && response.data.isNullOrEmpty().not()) {
        val messageDetail = response.data?.firstOrNull { it1 -> it1.type.equals(session?.fP_AppExperienceCode, ignoreCase = true) }?.message
        if (messageDetail.isNullOrEmpty().not()) {
          val lat = session?.getFPDetails(Key_Preferences.LATITUDE)
          val long = session?.getFPDetails(Key_Preferences.LONGITUDE)
          var location = ""
          if (lat != null && long != null) location = "${if (shareChannelText.isNullOrEmpty().not()) "\n\n" else ""}\uD83D\uDCCD *Find us on map: http://www.google.com/maps/place/$lat,$long*\n\n"
          val txt = String.format(messageDetail!!, session?.getFPDetails(GET_FP_DETAILS_BUSINESS_NAME) ?: "", session!!.getDomainName(false), shareChannelText, location)
          visitingCard(txt)
        }
      } else visitingCard("My Business Card")
    })
  }

  private fun visitingCard(shareChannelText: String) {
    session?.let {
      val dialogCard = VisitingCardSheet()
      dialogCard.setData(getLocalSession(it), shareChannelText)
      dialogCard.show(this@DashboardFragment.parentFragmentManager, VisitingCardSheet::class.java.name)
    }
  }

  private fun bottomSheetWebView(title: String, domainUrl: String) {
    val webViewBottomDialog = WebViewBottomDialog()
    webViewBottomDialog.setData(title, domainUrl)
    webViewBottomDialog.show(this@DashboardFragment.parentFragmentManager, WebViewBottomDialog::class.java.name)
  }


  private fun quickActionClick(type: QuickActionItem.QuickActionType) {
    when (type) {
      QuickActionItem.QuickActionType.POST_NEW_UPDATE -> baseActivity.startPostUpdate(session)
      QuickActionItem.QuickActionType.ADD_PHOTO_GALLERY -> baseActivity.startAddImageGallery(session)
      QuickActionItem.QuickActionType.ADD_TESTIMONIAL -> baseActivity.startTestimonial(session, true)
      QuickActionItem.QuickActionType.ADD_CUSTOM_PAGE -> baseActivity.startCustomPage(session, true)
      QuickActionItem.QuickActionType.LIST_STAFF -> {
        val bundle = Bundle()
        bundle.putString(IntentConstant.FP_TAG.name, session?.fpTag)
        bundle.putString(IntentConstant.FP_ID.name, session?.fPID)
        bundle.putString(IntentConstant.CLIENT_ID.name, clientId)
        startStaffFragmentActivity(com.appservice.constant.FragmentType.STAFF_PROFILE_LISTING_FRAGMENT, bundle = bundle)
      }
      QuickActionItem.QuickActionType.LIST_SERVICES,
      QuickActionItem.QuickActionType.LIST_PRODUCT,
      QuickActionItem.QuickActionType.LIST_DRUG_MEDICINE,
      -> baseActivity.startListServiceProduct(session)
      QuickActionItem.QuickActionType.ADD_SERVICE,
      QuickActionItem.QuickActionType.ADD_PRODUCT,
      QuickActionItem.QuickActionType.ADD_COURSE,
      QuickActionItem.QuickActionType.ADD_MENU,
      QuickActionItem.QuickActionType.ADD_ROOM_TYPE,
      -> baseActivity.startAddServiceProduct(session)
      QuickActionItem.QuickActionType.PLACE_APPOINTMENT -> baseActivity.startBookAppointmentConsult(session, false)
      QuickActionItem.QuickActionType.PLACE_CONSULT -> baseActivity.startBookAppointmentConsult(session, true)
      QuickActionItem.QuickActionType.ADD_PROJECT -> {
        if (session?.getStoreWidgets()?.equals(PremiumCode.PROJECTTEAM.value) == true) {
          baseActivity.startListProject(session)
        } else baseActivity.startListProjectAndTeams(session)
      }
      QuickActionItem.QuickActionType.ADD_TEAM_MEMBER -> {
        if (session?.getStoreWidgets()?.equals(PremiumCode.PROJECTTEAM.value) == true) {
          baseActivity.startListTeams(session)
        } else baseActivity.startListProjectAndTeams(session)
      }
      QuickActionItem.QuickActionType.UPLOAD_BROCHURE -> {
        if (session?.getStoreWidgets()?.equals(PremiumCode.BROCHURE.value) == true) {
          baseActivity.startAddDigitalBrochure(session)
        } else baseActivity.startListDigitalBrochure(session)
      }
      QuickActionItem.QuickActionType.POST_SEASONAL_OFFER -> baseActivity.startAddSeasonalOffer(session)
      QuickActionItem.QuickActionType.LIST_TOPPER -> baseActivity.startListToppers(session)
      QuickActionItem.QuickActionType.ADD_UPCOMING_BATCH -> baseActivity.startListBatches(session)
      QuickActionItem.QuickActionType.ADD_NEARBY_ATTRACTION -> baseActivity.startNearByView(session)
      QuickActionItem.QuickActionType.ADD_FACULTY_MEMBER -> baseActivity.startFacultyMember(session)

      QuickActionItem.QuickActionType.POST_STATUS_STORY,
      QuickActionItem.QuickActionType.ADD_SLIDER_BANNER,
      QuickActionItem.QuickActionType.PLACE_ORDER_BOOKING,
      QuickActionItem.QuickActionType.ADD_TABLE_BOOKING,
      QuickActionItem.QuickActionType.ADD_STAFF_MEMBER,
      QuickActionItem.QuickActionType.MAKE_ANNOUNCEMENT,
      -> {
        showShortToast("Coming soon...")
      }
    }
  }

  private fun clickBusinessUpdate(type: BusinessSetupHighData.BusinessClickEvent) {
    when (type) {
      BusinessSetupHighData.BusinessClickEvent.WEBSITE_VISITOR -> baseActivity.startSiteViewAnalytic(session, "UNIQUE")
      BusinessSetupHighData.BusinessClickEvent.ENQUIRIES -> baseActivity.startBusinessEnquiry(session)
      BusinessSetupHighData.BusinessClickEvent.ODER_APT -> baseActivity.startAptOrderSummary(session)
    }
  }

  private fun clickRoiSummary(type: RoiSummaryData.RoiType) {
    when (type) {
      RoiSummaryData.RoiType.ENQUIRY -> baseActivity.startBusinessEnquiry(session)
      RoiSummaryData.RoiType.TRACK_CALL -> baseActivity.startVmnCallCard(session)
      RoiSummaryData.RoiType.APT_ORDER -> baseActivity.startAptOrderSummary(session)
      RoiSummaryData.RoiType.CONSULTATION -> {
        showShortToast("Video Consultation analytics coming soon...")
      }
      RoiSummaryData.RoiType.APT_ORDER_WORTH -> {
//        baseActivity.startRevenueSummary(session)
        showShortToast("Collection analytics coming soon...")
      }
      RoiSummaryData.RoiType.COLLECTION_WORTH -> {
        showShortToast("Collection analytics coming soon...")
      }
    }
  }

  private fun clickGrowthStats(type: GrowthStatsData.GrowthType) {
    when (type) {
      GrowthStatsData.GrowthType.ALL_VISITS -> baseActivity.startSiteViewAnalytic(session, "TOTAL")
      GrowthStatsData.GrowthType.UNIQUE_VISITS -> baseActivity.startSiteViewAnalytic(session, "UNIQUE")
      GrowthStatsData.GrowthType.ADDRESS_NEWS -> baseActivity.startSiteViewAnalytic(session, "MAP_VISITS")
      GrowthStatsData.GrowthType.NEWSLETTER_SUBSCRIPTION -> baseActivity.startSubscriber(session)
      GrowthStatsData.GrowthType.SEARCH_QUERIES -> baseActivity.startSearchQuery(session)
    }
  }

  private fun actionChannelClick(data: ChannelModel) {
    val title: String
    var website = ""
    if (data.isWhatsAppChannel().not()) {
      if (data.isGoogleSearch()) {
        title = "Website"
        website = session?.getDomainName(false) ?: ""
      } else {
        title = data.channelAccessToken?.userAccountName?.takeIf { it.isNotEmpty() } ?: data.getName()
        if (data.isTwitterChannel()) website = "https://twitter.com/${data.channelAccessToken?.userAccountName?.trim()}"
        else if (data.isFacebookPage()) website = "https://www.facebook.com/${data.channelAccessToken?.userAccountId?.trim()}"
      }
      bottomSheetWebView(title, website)
    }
  }

  private fun academyBannerBoostClick(data: DashboardAcademyBanner) {
    val loader = ProgressDashboardDialog.newInstance()
    when {
      data.ctaFileLink.isNullOrEmpty().not() -> {
        this.ctaFileLink = data.ctaFileLink
        if (ActivityCompat.checkSelfPermission(baseActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ||
            ActivityCompat.checkSelfPermission(baseActivity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
          requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
        } else {
          loader.setData(R.raw.download_gif, resources.getString(R.string.download_file_banner))
          loader.showProgress(baseActivity.supportFragmentManager)
          baseActivity.startDownloadUri(data.ctaFileLink?.trim()!!)
          Handler().postDelayed({ loader.hideProgress() }, 1000)
        }
      }
      data.ctaWebLink.isNullOrEmpty().not() -> {
        loader.setData(R.raw.activity_browser_gif, resources.getString(R.string.opening_browser_banner))
        loader.showProgress(baseActivity.supportFragmentManager)
        Handler().postDelayed({
          baseActivity.startWebViewPageLoad(session, data.ctaWebLink?.trim()!!)
          loader.hideProgress()

        }, 1000)
      }
      data.ctaYoutubeLink.isNullOrEmpty().not() -> {
        loader.setData(R.raw.video_gif, resources.getString(R.string.taking_video_banner))
        loader.showProgress(baseActivity.supportFragmentManager)
        Handler().postDelayed({
          baseActivity.startYouTube(session, data.ctaYoutubeLink?.trim()!!)
          loader.hideProgress()
        }, 1000)
      }
    }
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    when (requestCode) {
      100 -> {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          if (ctaFileLink.isNullOrEmpty().not()) {
            val loader = ProgressDashboardDialog.newInstance()
            loader.setData(R.raw.download_gif, resources.getString(R.string.download_file_banner))
            loader.showProgress(baseActivity.supportFragmentManager)
            baseActivity.startDownloadUri(ctaFileLink?.trim()!!)
            Handler().postDelayed({ loader.hideProgress() }, 1000)
          }
        } else showShortToast("Permission denied to read your External storage")
        return
      }
    }
  }

  private fun showSimmerDrScore(isSimmer: Boolean, isRetry: Boolean = false) {
    binding?.mainContent?.post {
      if (isSimmer && isRetry.not()) handler.postDelayed(runnable, 4000)
      binding?.shimmerLoadDrScoreCard?.visibility = if (isSimmer || isRetry) View.VISIBLE else View.GONE
      binding?.shimmerLoadDrView?.setBackgroundColor(getColor(if (isSimmer) R.color.placeholder_bg else android.R.color.transparent))
      binding?.shimmerLoadDrView?.apply { if (isSimmer) startShimmer() else stopShimmer() }
      binding?.retryDrScore?.visibility = if (isSimmer.not() && isRetry) View.VISIBLE else View.GONE
      binding?.lowHighViewDrScore?.visibility = if (isSimmer && isRetry) View.GONE else View.VISIBLE
    }
  }

  private fun showSimmer(isSimmer: Boolean) {
    binding?.mainContent?.apply {
      if (isSimmer) {
        binding?.progressSimmer?.parentShimmerLayout?.visible()
        binding?.progressSimmer?.parentShimmerLayout?.startShimmer()
        binding?.nestedScrollView?.gone()
      } else {
        binding?.nestedScrollView?.visible()
        binding?.progressSimmer?.parentShimmerLayout?.gone()
        binding?.progressSimmer?.parentShimmerLayout?.stopShimmer()
      }
    }
  }

  private fun getChannelAccessToken(isShowLoader: Boolean = false) {
    if (isShowLoader) showProgress()
    viewModel?.getChannelsAccessToken(session?.fPID)?.observeOnce(this, {
      var urlString = ""
      if (it.isSuccess()) {
        val channelsAccessToken = (it as? ChannelsAccessTokenResponse)?.NFXAccessTokens
        channelsAccessToken?.forEach { it1 ->
          when (it1.type()) {
            ChannelAccessToken.AccessTokenType.facebookpage.name ->
              if (it1.UserAccountId.isNullOrEmpty().not()) urlString = "\n⚡ *Facebook: https://www.facebook.com/${it1.UserAccountId}*"
            ChannelAccessToken.AccessTokenType.twitter.name ->
              if (it1.UserAccountName.isNullOrEmpty().not()) urlString += "\n⚡ *Twitter: https://twitter.com/${it1.UserAccountName?.trim()}*"
          }
        }
      }
      getWhatsAppData(urlString, isShowLoader)
    })
  }

  private fun getWhatsAppData(urlString: String, isShowLoader: Boolean = false) {
    var urlStringN = urlString
    viewModel?.getWhatsappBusiness(session?.fpTag, WA_KEY)?.observeOnce(this, {
      if (isShowLoader) hideProgress()
      if (it.isSuccess()) {
        val response = ((it as? ChannelWhatsappResponse)?.Data)?.firstOrNull()
        if (response != null && response.active_whatsapp_number.isNullOrEmpty().not()) {
          urlStringN += "\n⚡ *WhatsApp: https://wa.me/${response.active_whatsapp_number}*"
        }
      }
      PreferencesUtils.instance.saveData(CHANNEL_SHARE_URL, urlStringN)
      if (isShowLoader) visitingCardDetailText(urlStringN)
    })
  }

  private fun bottomSheetFilter(type: String, dateFilter: FilterDateModel?) {
    val filterBottomSheet = FilterBottomSheet()
    filterBottomSheet.setData(dateFilter)
    filterBottomSheet.onClicked = { apiFilterCall(it, type) }
    filterBottomSheet.show(this@DashboardFragment.parentFragmentManager, FilterBottomSheet::class.java.name)
  }

  private fun apiFilterCall(filterDate: FilterDateModel, type: String) {
    when (type) {
      BUSINESS_REPORT -> {
        apiRoiBusinessReport(filterDate, true)
        filterDate.saveData(FILTER_BUSINESS_REPORT)
        binding?.filterBusinessReport?.text = filterDate.title
      }
      WEBSITE_REPORT -> {
        apiWebsiteReport(filterDate, true)
        filterDate.saveData(FILTER_WEBSITE_REPORT)
        binding?.filterWebsiteReport?.text = filterDate.title
      }
    }
  }

  override fun onStop() {
    super.onStop()
    handler.removeCallbacks(runnable)
    FirestoreManager.listener = null
  }

  override fun onStart() {
    super.onStart()
    FirestoreManager.listener = { setSummaryAndDrScore(true) }
  }
}

fun UserSessionManager.getRequestMap(startDate: String, endDate: String): Map<String, String> {
  val map = HashMap<String, String>()
  map["batchType"] = VisitsModelResponse.BatchType.yy.name
  map["startDate"] = startDate
  map["endDate"] = endDate
  map["clientId"] = clientId
  map["scope"] = if (this.iSEnterprise == "true") "Enterprise" else "Store"
  return map
}

fun UserSessionManager?.checkIsPremiumUnlock(value: String?): Boolean {
  return (this?.getStoreWidgets()?.firstOrNull { it == value } != null)
}

fun getLocalSession(session: UserSessionManager): LocalSessionModel {
  var imageUri = session.getFPDetails(GET_FP_DETAILS_LogoUrl)
  if (imageUri.isNullOrEmpty().not() && imageUri!!.contains("http").not()) imageUri = BASE_IMAGE_URL + imageUri
  val city = session.getFPDetails(Key_Preferences.GET_FP_DETAILS_CITY)
  val country = session.getFPDetails(Key_Preferences.GET_FP_DETAILS_COUNTRY)
  val location = if (city.isNullOrEmpty().not() && country.isNullOrEmpty().not()) "$city, $country" else "$city$country"
  return LocalSessionModel(floatingPoint = session.fPID, contactName = session.getFPDetails(Key_Preferences.GET_FP_DETAILS_CONTACTNAME), businessName = session.getFPDetails(GET_FP_DETAILS_BUSINESS_NAME),
      businessImage = imageUri, location = location, websiteUrl = session.getDomainName(false),
      businessType = session.getFPDetails(Key_Preferences.GET_FP_DETAILS_CATEGORY), primaryNumber = session.userPrimaryMobile,
      primaryEmail = session.fPEmail, fpTag = session.fpTag, experienceCode = session.fP_AppExperienceCode)
}

fun saveFirstLoad() {
  PreferencesUtils.instance.saveData(IS_FIRST_LOAD, true)
}

fun isFirstLoad(): Boolean {
  return PreferencesUtils.instance.getData(IS_FIRST_LOAD, false)
}

fun getRequestSellerSummary(filterDate: FilterDateModel?): SellerSummaryRequest {
  if (filterDate?.startDate.isNullOrEmpty()) return SellerSummaryRequest(filterBy = ArrayList())
  val request = SellerSummaryRequest()
  val queryObject = arrayListOf(
      QueryObject(key = QueryObject.keys.CreatedOn.name, value = filterDate?.startDate, queryOperator = QueryObject.Operator.GTE.name),
      QueryObject(key = QueryObject.keys.CreatedOn.name, value = filterDate?.endDate, queryOperator = QueryObject.Operator.LTE.name)
  )
  request.filterBy = arrayListOf(FilterBy(queryConditionType = FilterBy.ConditionType.AND.name, queryObject))
  return request
}