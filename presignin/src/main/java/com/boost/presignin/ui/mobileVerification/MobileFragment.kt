package com.boost.presignin.ui.mobileVerification

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import com.boost.presignin.R
import com.boost.presignin.base.AppBaseFragment
import com.boost.presignin.databinding.FragmentMobileBinding
import com.boost.presignin.extensions.isPhoneValid
import com.boost.presignin.helper.WebEngageController
import com.boost.presignin.ui.intro.IntroActivity
import com.boost.presignin.viewmodel.LoginSignUpViewModel
import com.framework.extensions.observeOnce
import com.framework.extensions.onTextChanged
import com.framework.pref.clientId
import com.framework.utils.hideKeyBoard
import com.framework.webengageconstant.*

class MobileFragment : AppBaseFragment<FragmentMobileBinding, LoginSignUpViewModel>() {


  companion object {
    private val PHONE_NUMBER = "phone_number"

    @JvmStatic
    fun newInstance() = MobileFragment().apply {}
    fun newInstance(phoneNumber: String) = MobileFragment().apply {
      arguments = Bundle().apply {
        putString(PHONE_NUMBER, phoneNumber)
      }
    }
  }

  private val phoneNumber by lazy { requireArguments().getString(PHONE_NUMBER) }

  override fun getLayout(): Int {
    return R.layout.fragment_mobile
  }

  override fun getViewModelClass(): Class<LoginSignUpViewModel> {
    return LoginSignUpViewModel::class.java
  }

  override fun onCreateView() {
    WebEngageController.trackEvent(PS_LOGIN_NUMBER_PAGE_LOAD, PAGE_VIEW, NO_EVENT_VALUE)
    binding?.phoneEt?.onTextChanged { binding?.nextButton?.isEnabled = (it.isPhoneValid()) }
    activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
      override fun handleOnBackPressed() {
        goBack()
      }
    })

    val backButton = binding?.toolbar?.findViewById<ImageView>(R.id.back_iv)
    backButton?.setOnClickListener { goBack() }

    binding?.nextButton?.setOnClickListener {
      WebEngageController.trackEvent(PS_LOGIN_NUMBER_CLICK, NEXT_CLICK, NO_EVENT_VALUE)
      activity?.hideKeyBoard()
      sendOtp(binding?.phoneEt?.text.toString())
    }
  }

  private fun goBack() {
    startActivity(Intent(requireContext(), IntroActivity::class.java))
    requireActivity().finish()
  }

  private fun sendOtp(phoneNumber: String?) {
    showProgress(getString(R.string.sending_otp))
    viewModel?.sendOtpIndia(phoneNumber?.toLong(), clientId)?.observeOnce(viewLifecycleOwner, {
      if (it.isSuccess() && it.parseResponse()) {
        addFragmentReplace(com.framework.R.id.container, OtpVerificationFragment.newInstance(binding?.phoneEt?.text?.toString()!!), addToBackStack = true)
      } else showShortToast(getString(R.string.otp_not_sent))
      hideProgress()
    })
  }
}