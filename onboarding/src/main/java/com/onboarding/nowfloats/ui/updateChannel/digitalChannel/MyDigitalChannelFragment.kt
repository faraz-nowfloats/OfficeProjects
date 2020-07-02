package com.onboarding.nowfloats.ui.updateChannel.digitalChannel

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import com.framework.base.BaseResponse
import com.framework.exceptions.NoNetworkException
import com.framework.extensions.gone
import com.framework.extensions.observeOnce
import com.framework.extensions.visible
import com.onboarding.nowfloats.R
import com.onboarding.nowfloats.base.AppBaseFragment
import com.onboarding.nowfloats.constant.*
import com.onboarding.nowfloats.databinding.FragmentDigitalChannelBinding
import com.onboarding.nowfloats.extensions.addInt
import com.onboarding.nowfloats.extensions.fadeIn
import com.onboarding.nowfloats.managers.NavigatorManager
import com.onboarding.nowfloats.model.RequestFloatsModel
import com.onboarding.nowfloats.model.category.CategoryDataModel
import com.onboarding.nowfloats.model.channel.*
import com.onboarding.nowfloats.model.channel.request.ChannelAccessToken
import com.onboarding.nowfloats.model.channel.request.ChannelActionData
import com.onboarding.nowfloats.model.channel.request.UpdateChannelAccessTokenRequest
import com.onboarding.nowfloats.model.channel.request.UpdateChannelActionDataRequest
import com.onboarding.nowfloats.model.channel.respose.NFXAccessToken
import com.onboarding.nowfloats.model.navigator.ScreenModel
import com.onboarding.nowfloats.recyclerView.AppBaseRecyclerViewAdapter
import com.onboarding.nowfloats.recyclerView.BaseRecyclerViewItem
import com.onboarding.nowfloats.recyclerView.RecyclerItemClickListener
import com.onboarding.nowfloats.rest.response.category.ResponseDataCategory
import com.onboarding.nowfloats.rest.response.channel.ChannelWhatsappResponse
import com.onboarding.nowfloats.rest.response.channel.ChannelsAccessTokenResponse
import com.onboarding.nowfloats.ui.startFragmentActivity
import com.onboarding.nowfloats.ui.updateChannel.ContainerUpdateChannelActivity
import com.onboarding.nowfloats.viewmodel.category.CategoryViewModel
import io.reactivex.Completable
import java.util.*
import kotlin.collections.ArrayList

class MyDigitalChannelFragment : AppBaseFragment<FragmentDigitalChannelBinding, CategoryViewModel>(), RecyclerItemClickListener {

  private val pref: SharedPreferences?
    get() {
      return baseActivity.getSharedPreferences(PreferenceConstant.NOW_FLOATS_PREFS, 0)
    }
  private val auth: String?
    get() {
      return pref?.getString(PreferenceConstant.AUTHORIZATION, "58ede4d4ee786c1604f6c535")
    }
  private val clientId: String?
    get() {
      return pref?.getString(PreferenceConstant.CLIENT_ID, "2FA76D4AFCD84494BD609FDB4B3D76782F56AE790A3744198E6F517708CAAA21")
    }
  private var requestFloatsModel: RequestFloatsModel? = null
  private var adapterConnect: AppBaseRecyclerViewAdapter<ChannelModel>? = null
  private var adapterDisconnect: AppBaseRecyclerViewAdapter<ChannelModel>? = null
  private var listDisconnect: ArrayList<ChannelModel>? = null
  private var listConnect: ArrayList<ChannelModel>? = null
  var isStartActivity: Boolean = false
  var websiteUrl: String = ""
  private lateinit var progress: ProgressChannelDialog

  private val selectedChannels: ArrayList<ChannelModel>
    get() {
      return (listDisconnect?.filter { it -> it.isSelected == true } ?: ArrayList()) as ArrayList<ChannelModel>
    }

  companion object {
    @JvmStatic
    fun newInstance(bundle: Bundle? = null): MyDigitalChannelFragment {
      val fragment = MyDigitalChannelFragment()
      fragment.arguments = bundle
      return fragment
    }
  }

  override fun getLayout(): Int {
    return R.layout.fragment_digital_channel
  }

  override fun getViewModelClass(): Class<CategoryViewModel> {
    return CategoryViewModel::class.java
  }

  override fun onCreateView() {
    super.onCreateView()
    progress = ProgressChannelDialog.newInstance()
    updateRequestGetChannelData()
    binding?.syncBtn?.setOnClickListener { syncChannels() }
  }

  private fun updateRequestGetChannelData() {
    val bundle = arguments
    isStartActivity = bundle?.getBoolean(PreferenceConstant.IS_START_ACTIVITY) ?: false
    val isUpdate = bundle?.getBoolean(PreferenceConstant.IS_UPDATE)
    websiteUrl = bundle?.getString(PreferenceConstant.WEBSITE_URL) ?: ""
    if (isUpdate != null && isUpdate) {
      NavigatorManager.clearRequest()
      val experienceCode = bundle.getString(PreferenceConstant.GET_FP_EXPERIENCE_CODE)
      if (experienceCode.isNullOrEmpty().not()) {
        val floatingPoint = bundle.getString(PreferenceConstant.KEY_FP_ID)
        val fpTag = bundle.getString(PreferenceConstant.GET_FP_DETAILS_TAG)
        showProgress("Refreshing your channels...", false)
        viewModel?.getCategories(baseActivity)?.observeOnce(this, Observer {
          if (it?.error != null) errorMessage(it.error?.localizedMessage ?: resources.getString(R.string.error_getting_category_data))
          else {
            val categoryList = (it as? ResponseDataCategory)?.data
            val categoryData = categoryList?.singleOrNull { c -> c.experienceCode() == experienceCode }
            if (categoryData != null) {
              getChannelAccessToken(categoryData, floatingPoint, fpTag)
            } else errorMessage(resources.getString(R.string.error_getting_category_data))
          }
        })
      } else showShortToast(resources.getString(R.string.invalid_experience_code))
    }
  }

  private fun getChannelAccessToken(categoryData: CategoryDataModel?, floatingPoint: String?, fpTag: String?) {
    viewModel?.getChannelsAccessToken(floatingPoint)?.observeOnce(this, Observer { it1 ->
      if (it1.error is NoNetworkException) errorMessage(resources.getString(R.string.internet_connection_not_available))
      else if (it1.status == 200 || it1.status == 201 || it1.status == 202) {
        val channelsAccessToken = (it1 as? ChannelsAccessTokenResponse)?.NFXAccessTokens
        setDataRequestChannels(categoryData, channelsAccessToken, floatingPoint, fpTag)
      } else if (it1.status == 404) {
        setDataRequestChannels(categoryData, ArrayList(), floatingPoint, fpTag)
      } else errorMessage(it1.message())
    })
  }

  private fun setDataRequestChannels(categoryData: CategoryDataModel?, channelsAccessToken: List<NFXAccessToken>?, floatingPoint: String?, fpTag: String?) {
    val requestFloatsNew = RequestFloatsModel()
    requestFloatsNew.categoryDataModel = categoryData
    requestFloatsNew.isUpdate = true
    requestFloatsNew.floatingPointId = floatingPoint
    requestFloatsNew.fpTag = fpTag
    requestFloatsNew.websiteUrl = websiteUrl
    requestFloatsNew.categoryDataModel?.resetIsSelect()
    if (channelsAccessToken.isNullOrEmpty().not()) {
      channelsAccessToken?.forEach {
        var data: ChannelAccessToken? = null
        when (it.type()) {
          ChannelAccessToken.AccessTokenType.facebookpage.name,
          ChannelAccessToken.AccessTokenType.twitter.name -> {
            if (it.isValidType()) {
              data = ChannelAccessToken(type = it.type(), userAccessTokenKey = it.UserAccessTokenKey,
                  userAccountId = it.UserAccountId, userAccountName = it.UserAccountName)
              requestFloatsNew.channelAccessTokens?.add(data)
            }
          }
          ChannelAccessToken.AccessTokenType.facebookshop.name -> {
            if (it.isValidTypeShop()) {
              data = ChannelAccessToken(type = it.type(), userAccessTokenKey = it.UserAccessTokenKey,
                  userAccountId = it.UserAccountId, userAccountName = it.UserAccountName, pixelId = it.PixelId,
                  catalogId = it.CatalogId, merchantSettingsId = it.MerchantSettingsId)
              requestFloatsNew.channelAccessTokens?.add(data)
            }
          }
          ChannelAccessToken.AccessTokenType.googlemybusiness.name.toLowerCase(Locale.ROOT) -> {
            val tokenResponse = ChannelTokenResponse(it.token_response?.access_token, it.token_response?.token_type, it.token_response?.expires_in, it.token_response?.refresh_token)
            data = ChannelAccessToken(type = it.type(), token_expiry = it.token_expiry, invalid = it.invalid,
                token_response = tokenResponse, refresh_token = it.refresh_token, userAccountName = it.account_name, userAccountId = it.account_id,
                LocationId = it.location_id, LocationName = it.location_name, verified_location = it.verified_location)
            requestFloatsNew.channelAccessTokens?.add(data)
          }
        }
        requestFloatsNew.categoryDataModel?.channels?.forEach { it1 ->
          if (it1.getAccessTokenType() == it.type()) {
            it1.isSelected = true
            it1.channelAccessToken = data
          }
          if (it1.isGoogleSearch()) it1.websiteUrl = websiteUrl
        }
      }
    }
    getWhatsAppData(requestFloatsNew)
  }

  private fun getWhatsAppData(requestFloatsNew: RequestFloatsModel) {
    viewModel?.getWhatsappBusiness(requestFloatsNew.fpTag, auth!!)?.observeOnce(this, Observer {
      if ((it.error is NoNetworkException).not()) {
        if (it.status == 200 || it.status == 201 || it.status == 202) {
          val response = ((it as? ChannelWhatsappResponse)?.Data)?.firstOrNull()
          if (response != null && response.active_whatsapp_number.isNullOrEmpty().not()) {
            val channelActionData = ChannelActionData(response.active_whatsapp_number?.trim())
            requestFloatsNew.channelActionDatas?.add(channelActionData)
            requestFloatsNew.categoryDataModel?.channels?.forEach { it1 ->
              if (it1.isWhatsAppChannel()) {
                it1.isSelected = true
                it1.channelActionData = channelActionData
              }
            }
          }
        }
      }
      NavigatorManager.updateRequest(requestFloatsNew)
      setViewChannels()
      hideProgress()
    })
  }

  private fun errorMessage(message: String) {
    hideProgress()
    showLongToast(message)
  }

  @SuppressLint("SetTextI18n")
  private fun setViewChannels() {
    requestFloatsModel = NavigatorManager.getRequest()
    listDisconnect = requestFloatsModel?.categoryDataModel?.channels?.filter { it.isSelected == false } as? ArrayList<ChannelModel>
    listConnect = requestFloatsModel?.categoryDataModel?.channels?.filter { it.isSelected == true } as? ArrayList<ChannelModel>
    binding?.connectedTxt?.text = "Connected (${listConnect?.size}/${requestFloatsModel?.categoryDataModel?.channels?.size})"
    binding?.notConnectedTxt?.text = "Not Connected (${listDisconnect?.size}/${requestFloatsModel?.categoryDataModel?.channels?.size})"

    binding?.disconnectedBg?.post {
      var animObserver: Completable? = null
      animObserver = if (listDisconnect.isNullOrEmpty()) {
        changeView(true)
        binding?.connectedRiya?.fadeIn(200L)?.andThen(binding?.imageRiya?.fadeIn(200L))?.andThen(binding?.riyaConnectedTxt?.fadeIn(200L))
      } else {
        changeView(false)
        binding?.viewDisconnect?.fadeIn(200L)?.doOnComplete { setAdapterDisconnected(listDisconnect) }?.andThen(binding?.viewConnect?.fadeIn(500L))
      }
      animObserver?.doOnComplete { setAdapterConnected(listConnect) }?.andThen(binding?.noteTxt?.fadeIn(100L))?.subscribe()
    }
  }

  private fun changeView(isConnect: Boolean) {
    (baseActivity as? ContainerUpdateChannelActivity)?.changeTheme(if (isConnect) R.color.colorAccent else R.color.bg_dark_grey)
    binding?.disconnectedBg?.visibility = if (isConnect) View.GONE else View.VISIBLE
    binding?.viewConnect?.visibility = if (isConnect) View.GONE else View.VISIBLE
    binding?.connectedRiya?.visibility = if (isConnect) View.VISIBLE else View.GONE
    if (isConnect.not()) binding?.connectedBg?.visibility = View.VISIBLE
  }

  private fun setAdapterDisconnected(list: ArrayList<ChannelModel>?) {
    binding?.recycleDisconnect?.post {
      listDisconnect = list?.map { it.recyclerViewType = RecyclerViewItemType.CHANNEL_ITEM_DISCONNECT.getLayout(); it } as? ArrayList<ChannelModel>
      listDisconnect?.let {
        adapterDisconnect = AppBaseRecyclerViewAdapter(baseActivity, it, this)
        binding?.recycleDisconnect?.adapter = adapterDisconnect
        adapterDisconnect?.runLayoutAnimation(binding?.recycleDisconnect)
      }
    }

  }

  private fun setAdapterConnected(list: ArrayList<ChannelModel>?) {
    binding?.recycleConnect?.post {
      listConnect = list?.map { it.recyclerViewType = RecyclerViewItemType.CHANNEL_ITEM_CONNECT.getLayout(); it } as? ArrayList<ChannelModel>
      listConnect?.let {
        adapterConnect = AppBaseRecyclerViewAdapter(baseActivity, it, this)
        binding?.recycleConnect?.adapter = adapterConnect
        adapterConnect?.runLayoutAnimation(binding?.recycleConnect)
      }
    }
  }

  @SuppressLint("SetTextI18n")
  override fun onItemClick(position: Int, item: BaseRecyclerViewItem?, actionType: Int) {
    val channel = item as ChannelModel
    when (actionType) {
      RecyclerViewActionType.CHANNEL_DISCONNECT_CLICKED.ordinal -> {
        if (channel.isFacebookShop()) {
          showLongToast("You can't connect to Facebook shop using app.")
          return
        }
        listDisconnect?.map {
          if (channel.getType() == it.getType()) {
            it.isSelected = it.isSelected?.not()
          };it.recyclerViewType = RecyclerViewItemType.CHANNEL_ITEM_DISCONNECT.getLayout(); it
        }
        adapterDisconnect?.notify(listDisconnect)
        val count = listDisconnect?.filter { it.isSelected == true }?.size ?: 0
        if (count > 0) {
          binding?.syncBtn?.text = "Continue Syncing $count Channel"
          binding?.syncBtn?.visible()
        } else binding?.syncBtn?.gone()
      }
      RecyclerViewActionType.CHANNEL_DISCONNECT_WHY_CLICKED.ordinal -> openWhyChannelDialog(channel)
      RecyclerViewActionType.CHANNEL_CONNECT_CLICKED.ordinal -> {

      }
      RecyclerViewActionType.CHANNEL_CONNECT_INFO_CLICKED.ordinal -> openInfoChannelDialog(channel)
    }
  }

  private fun responseManage(it: BaseResponse) {
    if (it.status == 200 || it.status == 201 || it.status == 202) {
      getChannelAccessToken(requestFloatsModel?.categoryDataModel, requestFloatsModel?.floatingPointId, requestFloatsModel?.fpTag)
    } else {
      showLongToast("Failed to disconnecting!")
      hideProgress()
    }
  }

  private fun openWhyChannelDialog(channel: ChannelModel) {
    DigitalChannelWhyDialog().apply {
      setChannels(channel)
      show(this@MyDigitalChannelFragment.parentFragmentManager, "")
    }
  }

  private fun syncChannels() {
    if (selectedChannels.isNullOrEmpty().not()) {
      val bundle = Bundle()
      var totalPages = if (requestFloatsModel?.isUpdate == true) 0 else 2
      selectedChannels.let { channels ->
        if (channels.haveGoogleBusinessChannel()) totalPages++
        if (channels.haveFacebookShop()) totalPages++
        if (channels.haveFacebookPage()) totalPages++
        if (channels.haveTwitterChannels()) totalPages++
        if (channels.haveWhatsAppChannels()) totalPages++
      }
      requestFloatsModel?.channels = ArrayList(selectedChannels)
      NavigatorManager.pushToStackAndSaveRequest(ScreenModel(ScreenModel.Screen.CHANNEL_SELECT, getToolbarTitle()), requestFloatsModel)
      bundle.addInt(IntentConstant.TOTAL_PAGES, totalPages).addInt(IntentConstant.CURRENT_PAGES, 1)
      val channels = requestFloatsModel?.channels ?: return
      when {
        channels.haveGoogleBusinessChannel() -> startFragmentActivity(FragmentType.REGISTRATION_BUSINESS_GOOGLE_PAGE, bundle)
        channels.haveFacebookPage() -> startFragmentActivity(FragmentType.REGISTRATION_BUSINESS_FACEBOOK_PAGE, bundle)
        channels.haveFacebookShop() -> startFragmentActivity(FragmentType.REGISTRATION_BUSINESS_FACEBOOK_SHOP, bundle)
        channels.haveTwitterChannels() -> startFragmentActivity(FragmentType.REGISTRATION_BUSINESS_TWITTER_DETAILS, bundle)
        channels.haveWhatsAppChannels() -> startFragmentActivity(FragmentType.REGISTRATION_BUSINESS_WHATSAPP, bundle)
      }
    } else showShortToast(resources.getString(R.string.at_least_one_channel_selected))
  }

  override fun showProgress(title: String?, cancelable: Boolean?) {
    title?.let { progress.setTitle(it) }
    cancelable?.let { progress.isCancelable = it }
    activity?.let { progress.showProgress(it.supportFragmentManager) }
  }

  override fun hideProgress() {
    progress.hideProgress()
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    super.onCreateOptionsMenu(menu, inflater)
    inflater.inflate(R.menu.menu_alert_icon, menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.menu_info -> {
        digitalChannelBottomSheet()
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun digitalChannelBottomSheet() {
    DigitalChannelSheetDialog().show(this.parentFragmentManager, DigitalChannelSheetDialog::class.java.name)
  }

  private fun openInfoChannelDialog(channel: ChannelModel) {
    DigitalChannelInfoDialog().apply {
      setChannels(channel)
      onClickedDisconnect = { disConnectChannel(it) }
      show(this@MyDigitalChannelFragment.parentFragmentManager, "")
    }
  }

  private fun disConnectChannel(channel: ChannelModel) {
    showProgress("Disconnecting your channel...", false)
    if (channel.isWhatsAppChannel()) {
      val request = UpdateChannelActionDataRequest(ChannelActionData(), requestFloatsModel?.getWebSiteId())
      viewModel?.postUpdateWhatsappRequest(request, auth!!)?.observeOnce(viewLifecycleOwner, Observer { responseManage(it) })
    } else {
      val request = UpdateChannelAccessTokenRequest(ChannelAccessToken(type = channel.getAccessTokenType()), clientId!!, requestFloatsModel?.floatingPointId!!)
      viewModel?.updateChannelAccessToken(request)?.observeOnce(viewLifecycleOwner, Observer { responseManage(it) })
    }
  }

}