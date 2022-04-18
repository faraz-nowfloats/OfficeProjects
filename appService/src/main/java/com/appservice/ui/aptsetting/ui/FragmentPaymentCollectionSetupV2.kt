package com.appservice.ui.aptsetting.ui

import android.app.Activity
import android.content.Intent
import android.view.View
import com.appservice.R
import com.appservice.base.AppBaseFragment
import com.appservice.constant.FragmentType
import com.appservice.constant.IntentConstant
import com.appservice.databinding.FragmentPaymentCollectionSetupV2Binding
import com.appservice.model.accountDetails.KYCDetails
import com.appservice.model.aptsetting.AddPaymentAcceptProfileRequest
import com.appservice.model.aptsetting.PaymentProfileResponse
import com.appservice.model.aptsetting.PaymentResult
import com.appservice.ui.catalog.startFragmentActivity
import com.appservice.utils.WebEngageController
import com.appservice.viewmodel.AppointmentSettingsViewModel
import com.framework.extensions.gone
import com.framework.extensions.invisible
import com.framework.extensions.observeOnce
import com.framework.extensions.visible
import com.framework.pref.clientId
import com.framework.utils.fromHtml
import com.framework.utils.makeLinks
import com.framework.webengageconstant.*

class FragmentPaymentCollectionSetupV2 : AppBaseFragment<FragmentPaymentCollectionSetupV2Binding, AppointmentSettingsViewModel>() {

  private var isBankAdded: Boolean = false

  override fun getLayout(): Int {
    return R.layout.fragment_payment_collection_setup_v2
  }

  override fun getViewModelClass(): Class<AppointmentSettingsViewModel> {
    return AppointmentSettingsViewModel::class.java
  }

  companion object {
    fun newInstance(): FragmentPaymentCollectionSetupV2 {
      return FragmentPaymentCollectionSetupV2()
    }
  }

  override fun onCreateView() {
    super.onCreateView()
    WebEngageController.trackEvent(PAYMENT_COLLECTION_SETUP_PAGE_LOAD, PAGE_VIEW, NO_EVENT_VALUE)
    setOnClickListener(binding?.btnAddAccount, binding?.btnUpdateBankAccount, binding?.btnConfirm)
    getAccountDetails()
    binding?.togglePaymentAfter?.setOnToggledListener { _, isOn ->
      toggleViewAndTextChange(binding?.togglePaymentBooking?.isOn ?: false, isOn)
    }
    binding?.togglePaymentBooking?.setOnToggledListener { _, isOn ->
      toggleViewAndTextChange(isOn, binding?.togglePaymentAfter?.isOn ?: false)
    }
  }

  private fun getAccountDetails() {
    showProgress()
    viewModel?.getPaymentProfileDetails(sessionLocal.fPID, clientId)?.observeOnce(viewLifecycleOwner) {
      if (it.isSuccess()) {
        binding?.maimView?.visible()
        val paymentProfileResponse = it as? PaymentProfileResponse
        if (paymentProfileResponse != null) {
          isBankAdded = paymentProfileResponse.result?.bankAccountDetails != null && (paymentProfileResponse.result?.bankAccountDetails?.isValidAccount() == true)
          onBankAccountUpdateUI(paymentProfileResponse.result)
        } else onBankAccountUpdateUI(result = null)
      } else {
        binding?.maimView?.invisible()
        showShortToast(getString(R.string.error_getting_bank_details))
      }
      hideProgress()
    }
  }

  private fun onBankAccountUpdateUI(result: PaymentResult?, acceptDuringBooking: Boolean = false, acceptAfterBooking: Boolean = false) {
    if (isBankAdded) {
      val pendingStatus = result?.bankAccountDetails?.kYCDetails?.verificationStatus == KYCDetails.Status.PENDING.name
      binding?.accountStatusTxt?.text = getString(if (pendingStatus) R.string.pending_verification else R.string.verify_)
      binding?.bankActiveTnc?.text = fromHtml(getString(if (pendingStatus) R.string.note_bank_account_deposit_money_verification else R.string.note_bank_account_deposit_money))
      binding?.imgBankStatus?.setImageResource(if (pendingStatus) R.drawable.ic_verification_error else R.drawable.ic_verification_verify)
      binding?.accountNameTxt?.text = result?.bankAccountDetails?.accountName ?: "Unknown"
      binding?.backDetailTxt?.text = "${result?.bankAccountDetails?.bankName ?: "Unknown"} - ${result?.bankAccountDetails?.accountNumber}"
    }
    toggleViewAndTextChange(acceptDuringBooking, acceptAfterBooking)
  }

  private fun toggleViewAndTextChange(acceptDuringBooking: Boolean, acceptAfterBooking: Boolean) {
    binding?.togglePaymentBooking?.isOn = acceptDuringBooking
    binding?.togglePaymentAfter?.isOn = acceptAfterBooking
    binding?.txtAcceptPaymentDesc?.text = getString(if (acceptDuringBooking) R.string.customers_payments_website_appointment_booking else R.string.select_payments_website_appointment_booking)
    binding?.txtPaymentAfterDesc?.text = getString(if (acceptAfterBooking) R.string.customers_allowed_cash_qr_code_payment_time_service else R.string.select_payment_cash_qr_code_time_service)
    binding?.viewEnableSwitch?.visibility = if (acceptAfterBooking.not() && acceptDuringBooking.not()) View.VISIBLE else View.GONE
    when {
      acceptDuringBooking.not() -> {
        binding?.addViewBank?.gone()
        binding?.connectedViewBank?.gone()
      }
      isBankAdded -> {
        binding?.addViewBank?.gone()
        binding?.connectedViewBank?.visible()
      }
      else -> {
        binding?.addViewBank?.visible()
        binding?.connectedViewBank?.gone()
        binding?.bankAccountTnc?.makeLinks(Pair("Read TnC", View.OnClickListener { showShortToast(getString(R.string.coming_soon)) }))
      }
    }
  }

  override fun onClick(v: View) {
    super.onClick(v)
    when (v) {
      binding?.btnAddAccount -> {
        WebEngageController.trackEvent(PAYMENT_COLLECTION_BANK_ACCOUNT_CLICK, CLICK, NO_EVENT_VALUE)
        startFragmentActivity(FragmentType.APPOINTMENT_FRAGMENT_ACCOUNT_ADD_HOME)
      }
      binding?.btnUpdateBankAccount -> {
        WebEngageController.trackEvent(PAYMENT_COLLECTION_BANK_ACCOUNT_CLICK, CLICK, NO_EVENT_VALUE)
        startFragmentActivity(FragmentType.EDIT_ACCOUNT_DETAILS, isResult = true)
      }
      binding?.btnConfirm -> addDataPaymentProfile()
    }
  }

  private fun addDataPaymentProfile() {
    showProgress()
    val request = AddPaymentAcceptProfileRequest(binding?.togglePaymentAfter?.isOn, binding?.togglePaymentBooking?.isOn, clientId, sessionLocal.fPID)
    viewModel?.addUpdatePaymentProfile(request)?.observeOnce(viewLifecycleOwner) {
      if (it.isSuccess()) {
        showShortToast(getString(R.string.added_payment_setup))
      } else showShortToast(it.errorFlowMessage() ?: getString(R.string.something_went_wrong))
      hideProgress()
    }
  }


  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
      if (data?.extras?.getBoolean(IntentConstant.IS_UPDATED.name) == true) {
        getAccountDetails()
      }
    }
  }
}