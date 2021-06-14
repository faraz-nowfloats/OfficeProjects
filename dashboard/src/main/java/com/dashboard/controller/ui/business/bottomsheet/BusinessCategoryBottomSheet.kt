package com.dashboard.controller.ui.business.bottomsheet

import android.view.View
import com.dashboard.R
import com.dashboard.databinding.BottomSheetBusinessCategoryBinding
import com.framework.base.BaseBottomSheetDialog
import com.framework.models.BaseViewModel

class BusinessCategoryBottomSheet : BaseBottomSheetDialog<BottomSheetBusinessCategoryBinding, BaseViewModel>() {

  override fun getLayout(): Int {
    return R.layout.bottom_sheet_business_category
  }

  override fun getViewModelClass(): Class<BaseViewModel> {
    return BaseViewModel::class.java
  }

  override fun onCreateView() {
    dialog.behavior.isDraggable = false
    setOnClickListener(
      binding?.btnContactSupport,
      binding?.btnUnderstood,
      binding?.rivCloseBottomSheet
    )
  }

  override fun onClick(v: View) {
    super.onClick(v)
    when (v) {
      binding?.btnContactSupport -> {
        //todo action not specified
      }
      binding?.btnUnderstood, binding?.rivCloseBottomSheet -> {
        dismiss()
      }
    }
  }
}