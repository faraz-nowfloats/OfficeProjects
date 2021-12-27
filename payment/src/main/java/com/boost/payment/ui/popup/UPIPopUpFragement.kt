package com.boost.payment.ui.popup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.boost.payment.R
import com.boost.payment.PaymentActivity
import com.boost.payment.interfaces.MoreBanksListener
import com.boost.payment.interfaces.UpiPayListener
import com.boost.payment.ui.payment.PaymentViewModel
import com.boost.payment.utils.Utils
import com.boost.payment.utils.WebEngageController
import com.framework.webengageconstant.*
import com.razorpay.Razorpay
import com.razorpay.ValidateVpaCallback
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.add_upi_popup.*
import org.json.JSONObject

class UPIPopUpFragement : DialogFragment() {

  lateinit var root: View
  private lateinit var viewModel: PaymentViewModel

  lateinit var razorpay: Razorpay

  var validatingStatus = false

  companion object {
    lateinit var listener: UpiPayListener
    fun newInstance(upiPayListener: UpiPayListener) = UPIPopUpFragement().apply {
      listener = upiPayListener
    }
  }

  override fun onStart() {
    super.onStart()
    val width = ViewGroup.LayoutParams.MATCH_PARENT
    val height = ViewGroup.LayoutParams.MATCH_PARENT
    dialog!!.window!!.setLayout(width, height)
    dialog!!.window!!.setBackgroundDrawableResource(R.color.fullscreen_color)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    root = inflater.inflate(R.layout.add_upi_popup, container, false)

    razorpay = (activity as PaymentActivity).getRazorpayObject()

    return root

  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    viewModel = ViewModelProviders.of(requireActivity()).get(PaymentViewModel::class.java)

    upi_popup_outer_layout.setOnClickListener {
      dialog!!.dismiss()
    }
    upi_popup_container_layout.setOnClickListener {}

    upi_popup_submit.setOnClickListener {
      if (!validatingStatus) {
        validatingStatus = true
        Utils.hideSoftKeyboard(requireActivity())
        upi_popup_submit.setText("Validating...")
        validateVPA()
      }
    }
    WebEngageController.trackEvent(ADDONS_MARKETPLACE_ADD_UPI_LOADED, ADD_UPI, NO_EVENT_VALUE)
  }

  fun validateVPA() {
    razorpay.isValidVpa(upi_popup_value.text.toString(), object : ValidateVpaCallback {
      override fun onFailure() {
        validatingStatus = false
        WebEngageController.trackEvent(
          ADDONS_MARKETPLACE_UPI_VALIDATION_FAILED,
          upi_popup_value.text.toString(),
          NO_EVENT_VALUE
        )
        upi_popup_submit.setText("VERIFY AND PAY")
        invalid_UPI.visibility = View.GONE
        Toasty.error(
          requireContext(),
          "Failed to validate your UPI Id. Please try again.",
          Toast.LENGTH_LONG
        ).show()
      }

      override fun onResponse(status: Boolean) {
        validatingStatus = false
        upi_popup_submit.setText("VERIFY AND PAY")
        if (status) {
          WebEngageController.trackEvent(
            ADDONS_MARKETPLACE_UPI_VALIDATION_SUCCESS,
            upi_popup_value.text.toString(),
            NO_EVENT_VALUE
          )
          upiPaymentRazorpay()
          invalid_UPI.visibility = View.GONE
        } else {
          WebEngageController.trackEvent(
            ADDONS_MARKETPLACE_UPI_VALIDATION_FAILED_2,
            upi_popup_value.text.toString(),
            NO_EVENT_VALUE
          )
          Toasty.warning(requireContext(), "Invalid UPI Id. Please try again.", Toast.LENGTH_LONG)
            .show()
          invalid_UPI.visibility = View.VISIBLE
        }
      }
    })
  }

  fun upiPaymentRazorpay() {
    val data = JSONObject()
    data.put("method", "upi")
    data.put("vpa", upi_popup_value.text.toString())
//        viewModel.UpdateUPIPaymentData(data)
    listener.upiSelected(data)
    dialog!!.dismiss()
    clearData()
  }

  fun clearData() {
    upi_popup_value.text.clear()
  }
}