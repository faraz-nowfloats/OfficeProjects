package com.inventoryorder.ui.order.sheetOrder

import android.view.View
import android.widget.CompoundButton
import android.widget.RadioButton
import android.widget.RadioGroup
import com.framework.base.BaseBottomSheetDialog
import com.framework.models.BaseViewModel
import com.inventoryorder.R
import com.inventoryorder.databinding.BottomSheetCancelOrderBinding
import com.inventoryorder.databinding.BottomSheetDeliveryTypeBinding
import com.inventoryorder.model.ordersdetails.OrderItem

class DeliveryTypeBottomSheetDialog : BaseBottomSheetDialog<BottomSheetDeliveryTypeBinding, BaseViewModel>() {

  private var cancellingEntity: String? = OrderItem.CancellingEntity.BUYER.name
  private var orderItem: OrderItem? = null
  var onClicked: (cancellingEntity: String,reasonText:String) -> Unit = { _: String, _: String -> }

  override fun getLayout(): Int {
    return R.layout.bottom_sheet_delivery_type
  }

  override fun getViewModelClass(): Class<BaseViewModel> {
    return BaseViewModel::class.java
  }

  fun setData(orderItem: OrderItem) {
    this.orderItem = orderItem
  }

  override fun onCreateView() {

    binding?.radioHome?.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
      override fun onCheckedChanged(p0: CompoundButton?, isChecked: Boolean) {
        if (isChecked) {
          binding?.radioStore?.isChecked = false
          binding?.radioHome?.isChecked = true
        }
      }
    })

    binding?.radioStore?.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
      override fun onCheckedChanged(p0: CompoundButton?, isChecked: Boolean) {
        if (isChecked) {
          binding?.radioStore?.isChecked = true
          binding?.radioHome?.isChecked = false
        }
      }
    })
  }

  override fun onClick(v: View) {
    super.onClick(v)
    dismiss()
    when (v) {
     // binding?.buttonDone -> onClicked(cancellingEntity?:"", (binding?.txtReason?.text?.toString()?:""))
    }
  }

}