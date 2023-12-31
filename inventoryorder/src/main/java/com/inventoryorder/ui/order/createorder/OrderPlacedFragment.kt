package com.inventoryorder.ui.order.createorder

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.framework.extensions.invisible
import com.framework.extensions.observeOnce
import com.framework.extensions.visible
import com.framework.pref.UserSessionManager
import com.framework.utils.NumbersToWords
import com.inventoryorder.R
import com.inventoryorder.constant.AppConstant
import com.inventoryorder.constant.FragmentType
import com.inventoryorder.constant.IntentConstant
import com.inventoryorder.databinding.FragmentOrderPlacedBinding
import com.inventoryorder.model.ordersdetails.OrderItem
import com.inventoryorder.rest.response.order.OrderDetailResponse
import com.inventoryorder.ui.BaseInventoryFragment
import com.inventoryorder.ui.FragmentContainerOrderActivity
import com.inventoryorder.ui.order.INVOICE_URL
import com.inventoryorder.ui.startFragmentOrderActivity
import com.inventoryorder.utils.startOrderAptConsultList

class OrderPlacedFragment : BaseInventoryFragment<FragmentOrderPlacedBinding>() {

  private lateinit var session: UserSessionManager
  var shouldReInitiate = false
  var type: String? = null
  var orderId: String? = null
  var orderResponse: OrderItem? = null

  companion object {
    @JvmStatic
    fun newInstance(bundle: Bundle? = null): OrderPlacedFragment {
      val fragment = OrderPlacedFragment()
      fragment.arguments = bundle
      return fragment
    }
  }

  override fun onCreateView() {
    super.onCreateView()
    session = UserSessionManager(baseActivity)
    type = arguments?.getString(IntentConstant.TYPE_APPOINTMENT.name)
    orderId = arguments?.getString(IntentConstant.ORDER_ID.name)
    getOrderDetails()
    setOnClickListener(binding?.buttonInitiateNewOrder, binding?.textInvoice, binding?.buttonConfirmOrder)
  }

  private fun setData(orderItem: OrderItem) {
    if (type.equals(AppConstant.TYPE_APPOINTMENT, true)) {
      binding?.tvName?.text = getString(R.string.appointment_booked_and_confirmed_successfully)

      binding?.linearPaymentStatus?.visibility = View.VISIBLE
      binding?.textPaymentStatus?.text = orderItem.PaymentDetails?.statusValue() ?: ""
      binding?.textOrderIdValue?.text = "#${orderItem.ReferenceNumber}"
      binding?.textName?.text = orderItem.BuyerDetails?.ContactDetails?.FullName ?: ""
      binding?.textTotalAmount?.text = "${orderItem.BillingDetails?.getCurrencyCodeValue() ?: "INR"} ${orderItem.BillingDetails?.GrossAmount ?: 0.0}"

      binding?.linearItemQty?.visibility = View.GONE
      binding?.linearPaymentMode?.visibility = View.GONE
      binding?.linearDeliveryType?.visibility = View.GONE
      binding?.appointmentText?.visibility = View.VISIBLE

      binding?.buttonConfirmOrder?.text = getString(R.string.view_appointment_details)
      binding?.buttonInitiateNewOrder?.text = getString(R.string.view_appointment_dashboard)
    } else {
      binding?.textOrderIdValue?.text = "#${orderItem.ReferenceNumber}"
      binding?.textName?.text = orderItem.BuyerDetails?.ContactDetails?.FullName ?: ""
      val totalQuantity = orderItem.BillingDetails?.ExtraProperties?.get("TotalQuantity")
      binding?.textCount?.text = "${NumbersToWords.solution(totalQuantity.toString().toFloat().toInt())} (${totalQuantity.toString().toFloat().toInt()})"
      binding?.textPaymentLink?.text = orderItem.PaymentDetails?.methodValue() ?: ""
      binding?.textPaymentStatus?.text = orderItem.PaymentDetails?.statusValue() ?: ""
      binding?.textDeliveryType?.text = orderItem.LogisticsDetails?.DeliveryMode ?: ""
      binding?.textTotalAmount?.text = "${orderItem.BillingDetails?.getCurrencyCodeValue() ?: "INR"} ${orderItem?.BillingDetails?.AmountPayableToSeller ?: 0.0}"
    }
    if (orderItem.BillingDetails?.InvoiceUrl.isNullOrEmpty().not()) binding?.invoiceView?.visible()
    else binding?.invoiceView?.invisible()
  }

  fun getBundleData(): Bundle {
    val bundle = Bundle()
    if (type.equals(AppConstant.TYPE_APPOINTMENT, true))
      bundle.putBoolean(IntentConstant.IS_REFRESH.name, true)
    bundle.putBoolean(IntentConstant.SHOULD_RE_INITIATE.name, shouldReInitiate)
    if (!shouldReInitiate) bundle.putBoolean(IntentConstant.SHOULD_FINISH.name, true)
    return bundle
  }

  private fun getOrderDetails() {
    showProgress()
    viewModel?.assuredPurchaseGetOrderDetails(preferenceData?.clientId, orderId)?.observeOnce(viewLifecycleOwner) {
      if (it.isSuccess()) {
        orderResponse = (it as? OrderDetailResponse)?.Data
        orderResponse?.let { it1 -> setData(it1) }
      } else showLongToast(if (it.message().isNotEmpty()) it.message() else getString(R.string.unable_to_create_order))
      hideProgress()
    }
  }

  override fun onClick(v: View) {
    super.onClick(v)
    when (v) {
      binding?.buttonInitiateNewOrder -> {
        shouldReInitiate = true
        if (type.equals(AppConstant.TYPE_APPOINTMENT, true)) {
          baseActivity.startOrderAptConsultList(session, isConsult = false)
        } else {
          (activity as? FragmentContainerOrderActivity)?.onBackPressed()
        }
      }
      binding?.textInvoice -> {
        val bundle = Bundle()
        bundle.putSerializable(INVOICE_URL, orderResponse?.BillingDetails?.InvoiceUrl)
        startFragmentOrderActivity(FragmentType.ORDER_INVOICE_VIEW, bundle)
      }
      binding?.buttonConfirmOrder -> {
        val bundle = Bundle()
        bundle.putString(IntentConstant.ORDER_ID.name, orderResponse?._id)
        bundle.putSerializable(IntentConstant.PREFERENCE_DATA.name, preferenceData)
        if (type.equals(AppConstant.TYPE_APPOINTMENT, true)) {
          startFragmentOrderActivity(
            FragmentType.APPOINTMENT_SPA_DETAIL_VIEW,
            bundle,
            isResult = true
          )
        } else {
          startFragmentOrderActivity(FragmentType.ORDER_DETAIL_VIEW, bundle, isResult = true)
        }
      }
    }
  }
}