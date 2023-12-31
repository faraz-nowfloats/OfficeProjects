package com.appservice.offers

import android.view.View
import com.appservice.R
import com.appservice.databinding.BottomSheetOffersCreatedSuccessfullyBinding
import com.appservice.databinding.BottomSheetServiceCreatedSuccessfullyBinding
import com.appservice.ui.catalog.catalogService.listing.TypeSuccess
import com.framework.base.BaseBottomSheetDialog
import com.framework.models.BaseViewModel

class OfferCreatedSuccessFullyBottomSheet: BaseBottomSheetDialog<BottomSheetOffersCreatedSuccessfullyBinding, BaseViewModel>() {

    var onClicked: (value: String) -> Unit = { }
    var isEdit: Boolean = false
    override fun getLayout(): Int {
        return R.layout.bottom_sheet_offers_created_successfully
    }

    fun setData(isEdit: Boolean) {
        this.isEdit = isEdit
    }

    override fun getViewModelClass(): Class<BaseViewModel> {
        return BaseViewModel::class.java
    }

    override fun onCreateView() {
        setOnClickListener(binding?.civCancel, binding?.visitWebsite)
        isCancelable = false
        binding?.txtMessage?.text = getString(if (isEdit) R.string.successfully_update else R.string.successfully_saved)
        binding?.txtDesc?.text = getString(if (isEdit) R.string.offer_update_and_published_live_on_your_website_or_share else R.string.offer_saved_and_published_live_on_your_website_or_share)
    }

    override fun onClick(v: View) {
        super.onClick(v)
        when (v) {
            binding?.civCancel -> {
                dismiss()
                onClicked(TypeSuccess.CLOSE.name)
            }
//      binding?.visitWebsite -> onClicked(TypeSuccess.VISIT_WEBSITE.name)
        }
    }
}