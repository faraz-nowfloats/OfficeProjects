package com.inventoryorder.ui.appointment

import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import com.framework.exceptions.NoNetworkException
import com.framework.extensions.gone
import com.framework.extensions.observeOnce
import com.framework.extensions.visible
import com.framework.utils.DateUtils
import com.framework.views.customViews.CustomButton
import com.inventoryorder.R
import com.inventoryorder.constant.IntentConstant
import com.inventoryorder.constant.RecyclerViewItemType
import com.inventoryorder.databinding.FragmentAppointmentDetailsBinding
import com.inventoryorder.model.OrderConfirmStatus
import com.inventoryorder.model.bottomsheet.LocationsModel
import com.inventoryorder.model.ordersdetails.ItemN
import com.inventoryorder.model.ordersdetails.OrderItem
import com.inventoryorder.model.ordersdetails.PaymentDetailsN
import com.inventoryorder.model.ordersummary.OrderStatusValue
import com.inventoryorder.model.ordersummary.OrderSummaryModel
import com.inventoryorder.recyclerView.AppBaseRecyclerViewAdapter
import com.inventoryorder.rest.response.order.OrderDetailResponse
import com.inventoryorder.rest.response.order.ProductResponse
import com.inventoryorder.ui.BaseInventoryFragment
import java.util.*

class AppointmentDetailsFragment : BaseInventoryFragment<FragmentAppointmentDetailsBinding>() {

  private var locationsBottomSheetDialog: LocationBottomSheetDialog? = null
  private var orderItem: OrderItem? = null
  private var serviceLocationsList = LocationsModel().getData()
  private var isRefresh: Boolean? = null
  private var productList: ArrayList<ProductResponse>? = null

  companion object {
    @JvmStatic
    fun newInstance(bundle: Bundle? = null): AppointmentDetailsFragment {
      val fragment = AppointmentDetailsFragment()
      fragment.arguments = bundle
      return fragment
    }
  }

  override fun onCreateView() {
    super.onCreateView()
    arguments?.getString(IntentConstant.ORDER_ID.name)?.let { apiGetOrderDetails(it) }
    setOnClickListener(binding?.btnBusiness)
  }

  private fun apiGetOrderDetails(orderId: String) {
    showProgress()
    viewModel?.getOrderDetails(clientId, orderId)?.observeOnce(viewLifecycleOwner, Observer {
      if (it.error is NoNetworkException) {
        errorUi(resources.getString(R.string.internet_connection_not_available))
        return@Observer
      }
      if (it.status == 200 || it.status == 201 || it.status == 202) {
        orderItem = (it as? OrderDetailResponse)?.Data
        if (orderItem != null) {
          getProductAllDetails()
        } else errorUi("Order item null.")
      } else errorUi(it.message())
    })
  }

  private fun getProductAllDetails() {
    productList = ArrayList()
    var count = 0
    if (orderItem?.Items.isNullOrEmpty().not()) {
      orderItem?.Items?.forEach {
        viewModel?.getProductDetails(it.Product?._id)?.observeOnce(viewLifecycleOwner, Observer { it1 ->
          count += 1
          val product = it1 as? ProductResponse
          if (count == orderItem?.Items?.size) {
            product?.let { it2 -> productList?.add(it2) }
            addProductToOrder()
          } else product?.let { it2 -> productList?.add(it2) }
        })
      }
    } else addProductToOrder()
  }

  private fun addProductToOrder() {
    productList?.forEach { orderItem?.Items?.firstOrNull { it1 -> it1.Product?._id?.trim() == it.Product?._id?.trim() }?.product_detail = it.Product }
    hideProgress()
    binding?.mainView?.visible()
    binding?.error?.gone()
    setDetails(orderItem!!)
  }

  private fun errorUi(message: String) {
    hideProgress()
    binding?.mainView?.gone()
    binding?.error?.visible()
    binding?.error?.text = message
  }

  private fun setDetails(order: OrderItem) {
    setToolbarTitle("# ${order.ReferenceNumber}")
    checkStatusOrder(order)
    setOrderDetails(order)
    (order.Items?.map {
      it.recyclerViewType = RecyclerViewItemType.BOOKING_DETAILS.getLayout();it
    } as? ArrayList<ItemN>)?.let { setAdapter(it) }
  }

  private fun checkStatusOrder(order: OrderItem) {
    if (order.isConfirmActionBtn()) {
      binding?.bottomBtn?.visible()
      binding?.buttonConfirmOrder?.setOnClickListener(this)
    } else binding?.bottomBtn?.gone()
    if (order.isCancelActionBtn()) {
      binding?.tvCancelOrder?.visible()
      binding?.tvCancelOrder?.setOnClickListener(this)
    } else binding?.tvCancelOrder?.gone()
  }

  private fun setAdapter(orderItems: ArrayList<ItemN>) {
    binding?.recyclerViewBookingDetails?.post {
      val adapter = AppBaseRecyclerViewAdapter(baseActivity, orderItems)
      binding?.recyclerViewBookingDetails?.adapter = adapter
    }
  }


  private fun buttonDisable(color: Int) {
    activity?.let {
      val newDrawable: Drawable? = binding?.buttonConfirmOrder?.background
      newDrawable?.let { it1 -> DrawableCompat.setTint(it1, ContextCompat.getColor(it, color)) }
      binding?.buttonConfirmOrder?.background = newDrawable
    }
  }

  fun getBundleData(): Bundle? {
    isRefresh?.let {
      val bundle = Bundle()
      bundle.putBoolean(IntentConstant.IS_REFRESH.name, it)
      return bundle
    }
    return null
  }


  private fun setOrderDetails(order: OrderItem) {
    binding?.orderType?.text = getStatusText(order)
    binding?.tvOrderStatus?.text = order.PaymentDetails?.status()
    binding?.tvPaymentMode?.text = order.PaymentDetails?.methodValue()
    order.BillingDetails?.let { bill ->
      val currency = takeIf { bill.CurrencyCode.isNullOrEmpty().not() }?.let { bill.CurrencyCode?.trim() } ?: "INR"
      binding?.tvOrderAmount?.text = "$currency ${bill.AmountPayableByBuyer}"
    }
    binding?.bookingDate?.text = DateUtils.parseDate(order.CreatedOn, DateUtils.FORMAT_SERVER_DATE, DateUtils.FORMAT_SERVER_TO_LOCAL_2, timeZone = TimeZone.getTimeZone("IST"))

    // customer details
    binding?.tvCustomerName?.text = order.BuyerDetails?.ContactDetails?.FullName?.trim()
    binding?.tvCustomerAddress?.text = order.BuyerDetails?.getAddressFull()

    binding?.tvCustomerContactNumber?.paintFlags?.or(Paint.UNDERLINE_TEXT_FLAG)?.let { binding?.tvCustomerContactNumber?.setPaintFlags(it) }
    binding?.tvCustomerEmail?.paintFlags?.or(Paint.UNDERLINE_TEXT_FLAG)?.let { binding?.tvCustomerEmail?.setPaintFlags(it) }
    binding?.tvCustomerContactNumber?.text = order.BuyerDetails?.ContactDetails?.PrimaryContactNumber?.trim()
    binding?.tvCustomerEmail?.text = order.BuyerDetails?.ContactDetails?.EmailId?.trim()

    // shipping details
    var shippingCost = 0.0
    var salePrice = 0.0
    var currency = "INR"
    order.Items?.forEachIndexed { index, item ->
      shippingCost += item.Product?.ShippingCost ?: 0.0
      salePrice += item.product().price() - item.product().discountAmount()
      if (index == 0) currency = takeIf { item.Product?.CurrencyCode.isNullOrEmpty().not() }
          ?.let { item.Product?.CurrencyCode?.trim() } ?: "INR"
    }
    binding?.tvTotalOrderAmount?.text = "Total Amount: $currency $salePrice"

  }

  private fun getStatusText(order: OrderItem): String? {
    val statusValue = OrderStatusValue.fromStatusAppointment(order.status())?.value
    return when (OrderSummaryModel.OrderStatus.ORDER_CANCELLED.name) {
      order.status().toUpperCase(Locale.ROOT) -> {
        return if (order.PaymentDetails?.status()?.toUpperCase(Locale.ROOT) == PaymentDetailsN.STATUS.CANCELLED.name) {
          OrderStatusValue.ESCALATED_2.value
        } else statusValue.plus(order.cancelledText())
      }
      else -> statusValue
    }
  }

  override fun onClick(v: View) {
    super.onClick(v)
    when (v) {
      binding?.btnBusiness -> showBottomSheetDialog()
      binding?.buttonConfirmOrder -> apiConfirmOrder()
      binding?.tvCancelOrder -> apiCancelOrder()
    }
  }

  private fun apiCancelOrder() {
    showProgress()
    viewModel?.cancelOrder(clientId, orderItem?._id, OrderItem.CancellingEntity.SELLER.name)?.observeOnce(viewLifecycleOwner, Observer {
      hideProgress()
      if (it.error is NoNetworkException) {
        showShortToast(resources.getString(R.string.internet_connection_not_available))
        return@Observer
      }
      if (it.status == 200 || it.status == 201 || it.status == 202) {
        val data = it as? OrderConfirmStatus
        data?.let { d -> showLongToast(d.Message as String?) }
        refreshStatus(OrderSummaryModel.OrderStatus.ORDER_CANCELLED)
      } else showLongToast(it.message())
    })
  }

  private fun apiConfirmOrder() {
    showProgress()
    viewModel?.confirmOrder(clientId, orderItem?._id)?.observeOnce(viewLifecycleOwner, Observer {
      hideProgress()
      if (it.error is NoNetworkException) {
        showShortToast(resources.getString(R.string.internet_connection_not_available))
        return@Observer
      }
      if (it.status == 200 || it.status == 201 || it.status == 202) {
        val data = it as? OrderConfirmStatus
        data?.let { d -> showLongToast(d.Message as String?) }
        refreshStatus(OrderSummaryModel.OrderStatus.ORDER_CONFIRMED)
      } else showLongToast(it.message())
    })
  }

  private fun refreshStatus(statusOrder: OrderSummaryModel.OrderStatus) {
    isRefresh = true
    orderItem?.Status = statusOrder.name
    orderItem?.let { binding?.orderType?.text = getStatusText(it) }
    orderItem?.let { checkStatusOrder(it) }
  }

  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    super.onCreateOptionsMenu(menu, inflater)
    val item: MenuItem = menu.findItem(R.id.menu_item_share)
    item.actionView.findViewById<CustomButton>(R.id.button_share).setOnClickListener {
      showLongToast("Coming soon..")
    }
  }


  private fun showBottomSheetDialog() {
    showLongToast("Coming soon..")
//    locationsBottomSheetDialog = LocationBottomSheetDialog()
//    locationsBottomSheetDialog?.onDoneClicked = { clickDeliveryItem(it) }
//    locationsBottomSheetDialog?.setList(serviceLocationsList)
//    locationsBottomSheetDialog?.show(this.parentFragmentManager, DeliveryBottomSheetDialog::class.java.name)
  }

  private fun clickDeliveryItem(list: LocationsModel?) {
    serviceLocationsList.forEach { it.isSelected = (it.serviceOptionSelectedName == list?.serviceOptionSelectedName) }
  }
}