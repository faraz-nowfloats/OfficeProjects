package com.appservice.ui.domainbooking

import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import com.appservice.R
import com.appservice.base.AppBaseFragment
import com.appservice.databinding.FragmentActiveNewDomainBinding
import com.appservice.model.domainBooking.DomainDetailsResponse
import com.appservice.utils.WebEngageController
import com.appservice.utils.getMillisecondsToDate
import com.appservice.viewmodel.DomainBookingViewModel
import com.framework.extensions.observeOnce
import com.framework.firebaseUtils.caplimit_feature.CapLimitFeatureResponseItem
import com.framework.pref.clientId
import com.framework.webengageconstant.DOMAIN_ACTIVE_NEW_DOMAIN_DETAILS_PAGE_LOAD
import com.framework.webengageconstant.NO_EVENT_VALUE
import com.framework.webengageconstant.PAGE_VIEW

class ActiveNewDomainFragment : AppBaseFragment<FragmentActiveNewDomainBinding, DomainBookingViewModel>() {

  companion object {
    @JvmStatic
    fun newInstance(bundle: Bundle? = null): ActiveNewDomainFragment {
      val fragment = ActiveNewDomainFragment()
      fragment.arguments = bundle
      return fragment
    }
  }

  override fun onCreateView() {
    WebEngageController.trackEvent(DOMAIN_ACTIVE_NEW_DOMAIN_DETAILS_PAGE_LOAD, PAGE_VIEW, NO_EVENT_VALUE)
    onBackPressHandler()
    domainDetailsApi()
  }

  private fun getFeatureDetailsAzureApi() {
    viewModel?.getFeatureDetails(sessionLocal.fPID, clientId)?.observeOnce(this) {
      if (it.isSuccess()) {
        getDomainValidDates(it.arrayResponse as? Array<CapLimitFeatureResponseItem>)
      } else showShortToast(it.errorFlowMessage() ?: getString(R.string.something_went_wrong))
      hideProgress()
    }
  }

  private fun getDomainValidDates(capLimitFeatureList: Array<CapLimitFeatureResponseItem>?) {
    Log.i("cgcg", "arrayOfCapLimitFeatureResponseItems?.toString()")
  }

  private fun onBackPressHandler() {
    baseActivity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
      override fun handleOnBackPressed() {
        requireActivity().finish()
      }
    }
    )
  }

  override fun getLayout(): Int {
    return R.layout.fragment_active_new_domain
  }

  override fun getViewModelClass(): Class<DomainBookingViewModel> {
    return DomainBookingViewModel::class.java
  }

  private fun domainDetailsApi() {
    showProgress()
    viewModel?.domainDetails(sessionLocal.fpTag, clientId)?.observeOnce(this) {
      if (it.isSuccess()) {
        getFeatureDetailsAzureApi()
        (it as? DomainDetailsResponse)?.let { it1 -> setAndParseData(it1) }
      } else showShortToast(it.errorFlowMessage() ?: getString(R.string.something_went_wrong))
      hideProgress()
    }
  }

  private fun setAndParseData(domainDetailsResponse: DomainDetailsResponse) {
    val domainName = if (!domainDetailsResponse.domainName.isNullOrEmpty() && domainDetailsResponse.domainName != "null") domainDetailsResponse.domainName else ""

    val domainType = if (domainDetailsResponse.domainType != null && domainDetailsResponse.domainType != "null") domainDetailsResponse.domainType else ""

    val domainFullName = if (domainName.isEmpty() && domainType.toString().isEmpty()) "N/A" else domainName + domainType

    binding?.layoutDomainDetails?.tvDomainValue?.text = domainFullName

    var activatedOn = domainDetailsResponse.activatedOn
    activatedOn = if (!activatedOn.isNullOrEmpty() && activatedOn.contains("/Date"))
      getMillisecondsToDate(activatedOn.replace("/Date(", "").replace(")/", "").toLong(), "dd-MM-YYYY")
    else "N/A"
    binding?.layoutDomainDetails?.tvBookedValue?.text = activatedOn

    var expiryOn = domainDetailsResponse.expiresOn
    expiryOn = if (!expiryOn.isNullOrEmpty() && expiryOn.contains("/Date"))
      getMillisecondsToDate(expiryOn.replace("/Date(", "").replace(")/", "").toLong(), "dd-MM-YYYY")
    else "N/A"
    binding?.layoutDomainDetails?.tvExpireValue?.text = expiryOn

    binding?.layoutDomainDetails?.tvDomainStatus?.text = if (domainDetailsResponse.isActive!!) getString(R.string.active) else getString(R.string.expired)

    binding?.layoutDomainDetails?.tvDomainStatus?.setBackgroundColor(if (domainDetailsResponse.isActive) getColor(R.color.green_light_1) else getColor(R.color.black_4a4a4a))
  }
}