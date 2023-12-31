package com.festive.poster.recyclerView.viewholders

import com.festive.poster.R
import com.festive.poster.databinding.SocialPreviewEmailNoImageBinding
import com.festive.poster.models.promoModele.SocialPreviewModel
import com.festive.poster.recyclerView.AppBaseRecyclerViewHolder
import com.festive.poster.recyclerView.BaseRecyclerViewItem
import com.framework.utils.highlightHashTag

class EmailPreviewNoImageViewHolder(binding: SocialPreviewEmailNoImageBinding) :
    AppBaseRecyclerViewHolder<SocialPreviewEmailNoImageBinding>(binding) {

    override fun bind(position: Int, item: BaseRecyclerViewItem) {
        val model = item as SocialPreviewModel
        binding.imageExist = model.posterImg.isNullOrEmpty().not()

        binding.tvCaption.text = highlightHashTag(model.desc, R.color.color395996,R.font.regular)
        binding.tvTitle.text = model.title

        super.bind(position, item)
    }

}
