package com.appservice.ui.aptsetting.widgets

import android.view.View
import androidx.core.content.ContextCompat
import com.appservice.R
import com.appservice.constant.IntentConstant
import com.appservice.databinding.BottomSheetConfirmGstBinding
import com.appservice.viewmodel.AppointmentSettingsViewModel
import com.framework.base.BaseBottomSheetDialog
import com.framework.utils.fromHtml

class BottomSheetConfirmGST : BaseBottomSheetDialog<BottomSheetConfirmGstBinding, AppointmentSettingsViewModel>() {

  var isECommerce = false

  override fun getLayout(): Int {
    return R.layout.bottom_sheet_confirm_gst
  }

  fun setType(isECommerce: Boolean) {
    this.isECommerce = isECommerce
  }

  override fun getViewModelClass(): Class<AppointmentSettingsViewModel> {
    return AppointmentSettingsViewModel::class.java
  }

  var clickType: (name: ClickType?) -> Unit = { }

  enum class ClickType {
    SAVECHANGES, CANCEL
  }

  override fun onCreateView() {
    setOnClickListener(binding?.btnCancel, binding?.yesItsCorrect)
    val gstIn = arguments?.getString(IntentConstant.GSTIN.name)
    val businessName = arguments?.getString(IntentConstant.BUSINESSNAME.name)
    val gstConfirm = "Your GSTIN no. <b>$gstIn</b> is registered with the company name <b>$businessName.</b>"
    binding?.ctvConfirmGst?.text = fromHtml(gstConfirm)
    binding?.noteView?.setBackgroundColor(ContextCompat.getColor(baseActivity, if (this.isECommerce) R.color.view_background_1 else R.color.yellow_orange_22))
  }

  override fun onClick(v: View) {
    super.onClick(v)
    when (v) {
      binding?.btnCancel -> {
        dismiss()
        clickType(ClickType.CANCEL)
      }
      binding?.yesItsCorrect -> {
        dismiss()
        clickType(ClickType.SAVECHANGES)
      }
    }
  }
}