package com.appservice.ui.catalog.widgets

import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.appservice.R
import com.appservice.databinding.BottomShettImagePickerBinding
import com.framework.base.BaseBottomSheetDialog
import com.framework.extensions.gone
import com.framework.models.BaseViewModel

enum class ClickType {
  CAMERA, GALLERY, GIF_IMAGE, PDF
}

class ImagePickerBottomSheet : BaseBottomSheetDialog<BottomShettImagePickerBinding, BaseViewModel>() {

  var onClicked: (type: ClickType) -> Unit = { }
  var isHidePdf = true
  var isGifHide = true
  override fun getLayout(): Int {
    return R.layout.bottom_shett_image_picker
  }

  fun isHidePdfOrGif(isHidePdf: Boolean? = true, isGifHide: Boolean? = true) {
    this.isHidePdf = isHidePdf ?: true
    this.isGifHide = isGifHide ?: true
  }

  override fun getViewModelClass(): Class<BaseViewModel> {
    return BaseViewModel::class.java
  }

  override fun onCreateView() {
    binding?.pdf?.isVisible = isHidePdf.not()
    binding?.galleryGif?.isVisible = isGifHide.not()
    setOnClickListener(binding?.camera, binding?.gallery, binding?.galleryGif, binding?.close, binding?.pdf)
  }

  override fun onClick(v: View) {
    super.onClick(v)
    when (v) {
      binding?.camera -> onClicked(ClickType.CAMERA)
      binding?.galleryGif -> onClicked(ClickType.GIF_IMAGE)
      binding?.gallery -> onClicked(ClickType.GALLERY)
      binding?.pdf -> onClicked(ClickType.PDF)
    }
    dismiss()
  }
}